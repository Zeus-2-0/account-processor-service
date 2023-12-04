package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.constants.EnrollmentSpanStatus;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.domain.repository.EnrollmentSpanRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.dto.transaction.TransactionRateDto;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.helper.interfaces.PremiumSpanHelper;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.brihaspathee.zeus.mapper.interfaces.EnrollmentSpanMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 24, November 2022
 * Time: 6:24 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 * Nuclino:<a href="https://app.nuclino.com/Balaji-Inc/Engineering-Wiki/Enrollment-Span-Helper-ad46155f-d182-4f92-b4ce-e835ad20c38f">Nuclino</a>
 * Confluence: <a href="https://vbalaji.atlassian.net/wiki/spaces/ZEUS/pages/99745829/APS+-+Enrollment+Span+Helper">Confluence</a>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentSpanHelperImpl implements EnrollmentSpanHelper {

    /**
     * Enrollment span mapper instance
     */
    private final EnrollmentSpanMapper enrollmentSpanMapper;

    /**
     * Enrollment span repository instance to perform CRUD operations
     */
    private final EnrollmentSpanRepository enrollmentSpanRepository;

    /**
     * Premium span helper instance
     */
    private final PremiumSpanHelper premiumSpanHelper;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create an enrollment span from the transaction data
     * @param transactionDto The transaction detail
     * @param account the account to which the enrollment span should be associated
     * @param priorEnrollmentSpans enrollment spans that is immediately prior to the effective date in the transaction
     * @return return the created enrollment span
     */
    @Override
    public EnrollmentSpan createEnrollmentSpan(TransactionDto transactionDto,
                                               Account account,
                                               List<EnrollmentSpanDto> priorEnrollmentSpans) {
        // Get the primary member from the transaction dto, need to get the
        // exchange subscriber id from the primary subscriber
        TransactionMemberDto primarySubscriber = getPrimaryMember(transactionDto);
        // Create the enrollment span entity
        String enrollmentSpanCode = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                "enrollmentSpanCode");
        EnrollmentSpan enrollmentSpan = EnrollmentSpan.builder()
                .acctEnrollmentSpanSK(null)
                .enrollmentSpanCode(enrollmentSpanCode)
                .account(account)
                .ztcn(transactionDto.getZtcn())
                .stateTypeCode(transactionDto.getTradingPartnerDto().getStateTypeCode())
                .marketplaceTypeCode(transactionDto.getTradingPartnerDto().getMarketplaceTypeCode())
                .businessUnitTypeCode(transactionDto.getTradingPartnerDto().getBusinessTypeCode())
                .coverageTypeCode(transactionDto.getTransactionDetail().getCoverageTypeCode())
                .startDate(transactionDto.getTransactionDetail().getEffectiveDate())
                // Determine and populate the end for the enrollment span
                .endDate(determineEndDate(
                        transactionDto.getTransactionDetail().getEffectiveDate(),
                        transactionDto.getTransactionDetail().getEndDate()
                ))
                // get the exchange subscriber id and set it
                .exchangeSubscriberId(getExchangeSubscriberId(primarySubscriber))
                // determine the effectuation date
                .effectuationDate(determineEffectuationDate(transactionDto, priorEnrollmentSpans))
                .planId(transactionDto.getTransactionDetail().getPlanId())
                .productTypeCode("HMO")
                .groupPolicyId(transactionDto.getTransactionDetail().getGroupPolicyId())
                .changed(true)
                .build();
        // Determine the enrollment span status
        String spanStatus = determineEnrollmentSpanStatus(enrollmentSpan, priorEnrollmentSpans);
        enrollmentSpan.setStatusTypeCode(spanStatus);
        enrollmentSpan = enrollmentSpanRepository.save(enrollmentSpan);
        // Create the premium span
        premiumSpanHelper.createPremiumSpans(transactionDto,
                enrollmentSpan,
                account);
        return enrollmentSpan;
    }

    /**
     * Set the enrollment span to send to MMS
     * @param accountDto
     * @param account
     * @param ztcn
     */
    @Override
    public void setEnrollmentSpan(AccountDto accountDto,
                                  Account account,
                                  String ztcn) {
        if(account.getEnrollmentSpan() != null && account.getEnrollmentSpan().size() > 0){
            List<EnrollmentSpanDto> enrollmentSpanDtos = new ArrayList<>();
            account.getEnrollmentSpan().forEach(enrollmentSpan -> {
                EnrollmentSpanDto enrollmentSpanDto = enrollmentSpanMapper.enrollmentSpanToEnrollmentSpanDto(enrollmentSpan);
//                enrollmentSpanDto.setZtcn(ztcn);
                premiumSpanHelper.setPremiumSpan(enrollmentSpanDto, enrollmentSpan, ztcn);
                enrollmentSpanDtos.add(enrollmentSpanDto);
            });
            accountDto.setEnrollmentSpans(enrollmentSpanDtos.stream().collect(Collectors.toSet()));
        }

    }

    /**
     * Determine the status of an enrollment span
     * @param enrollmentSpanStatusDto
     * @return
     */
    @Override
    public String determineStatus(EnrollmentSpanStatusDto enrollmentSpanStatusDto) {
        return determineEnrollmentSpanStatus(enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(enrollmentSpanStatusDto.getCurrentEnrollmentSpan()),
                enrollmentSpanStatusDto.getPriorEnrollmentSpans());
    }

    /**
     * Get enrollment spans that are overlapping
     * @param accountDto The account from which the overlapping enrollment spans are to be identfied
     * @param effectiveStartDate the start date that is to be used
     * @param effectiveEndDate the end date that is to be used
     * @param coverageTypeCode identifies the type of coverage "FAM" or "DEP"
     * @return return the enrollment spans that are overlapping with the dates that are passed
     */
    private List<EnrollmentSpanDto> getOverlappingEnrollmentSpans(AccountDto accountDto,
                                                                 LocalDate effectiveStartDate,
                                                                 LocalDate effectiveEndDate,
                                                                 String coverageTypeCode) {
        if(accountDto.getEnrollmentSpans() == null || accountDto.getEnrollmentSpans().isEmpty()){
            return null;
        }
        Set<EnrollmentSpanDto> overlappingEnrollmentSpans = new HashSet<>();
        Set<EnrollmentSpanDto> enrollmentSpanDtos = accountDto.getEnrollmentSpans();
        int effectiveYear = effectiveStartDate.getYear();
        // Get all the enrollment spans that are present for the year for which the effective date is received and
        // has the same coverage type code that is passed in the input
        // i.e. if the effective date is 2/1/2023 get all the enrollment spans that belong to the year 2023 and also
        // has the same coverage type code "FAM" or "DEP"
        overlappingEnrollmentSpans = enrollmentSpanDtos.stream()
                .filter(enrollmentSpanDto -> enrollmentSpanDto.getStartDate().getYear() == effectiveYear &&
                                             enrollmentSpanDto.getCoverageTypeCode().equals(coverageTypeCode))
                .collect(Collectors.toSet());
        // Get all the enrollment spans that have the end date that is greater than the effective start date
        // This eliminates all the enrollment spans that are termed prior to the effective start date of the transaction
        overlappingEnrollmentSpans = overlappingEnrollmentSpans.stream()
                .filter(enrollmentSpanDto -> enrollmentSpanDto.getEndDate().isAfter(effectiveStartDate))
                .collect(Collectors.toSet());
        // Get all enrollment spans that have the start date that is before the effective end date received in the
        // transaction
        overlappingEnrollmentSpans = overlappingEnrollmentSpans.stream()
                .filter(enrollmentSpanDto -> {
                    if(enrollmentSpanDto.getStatusTypeCode().equals("CANCELED")){
                        return false;
                    }
                    LocalDate enrollmentSpanStartDate = enrollmentSpanDto.getStartDate();
                    return enrollmentSpanStartDate.isBefore(effectiveEndDate);
                })
                .collect(Collectors.toSet());
        // Return null if there are no overlapping enrollment spans
        if(overlappingEnrollmentSpans.isEmpty()){
            return null;
        }else {
            return overlappingEnrollmentSpans.stream().toList();
        }
    }

    /**
     * Identify the enrollment spans that overlap the dates and term or cancel them appropriately
     * @param overlappingEnrollmentSpans List of enrollment spans that overlap
     * @param effectiveStartDate the dates when the enrollment spans are overlapping
     * @param effectiveEndDate the dates when the enrollment spans are overlapping
     * @return return the enrollment spans the need to be termed or canceled to avoid overlapping issues
     */
    private List<EnrollmentSpanDto> updateOverlappingEnrollmentSpans(List<EnrollmentSpanDto> overlappingEnrollmentSpans,
                                                                    LocalDate effectiveStartDate,
                                                                    LocalDate effectiveEndDate) {
        // Check if there are any enrollment spans that need to be termed or canceled
        if(overlappingEnrollmentSpans != null && !overlappingEnrollmentSpans.isEmpty()){
            // Get the enrollment span in which the effective date is falling in between its start date and end date
            Set<EnrollmentSpanDto> termCancelEnrollmentSpans = overlappingEnrollmentSpans.stream().filter(enrollmentSpanDto ->
                    enrollmentSpanDto.getStartDate().isEqual(effectiveStartDate) ||
                            (enrollmentSpanDto.getStartDate().isBefore(effectiveStartDate) &&
                                    enrollmentSpanDto.getEndDate().isAfter(effectiveStartDate))).collect(Collectors.toSet());
            if(termCancelEnrollmentSpans.size() > 1){
                log.info("More than one enrollment spans to be termed");
                // todo generate an exception when this is true
            }else if (termCancelEnrollmentSpans.size() == 1) {
                // Get the first element from the set to term
                final EnrollmentSpanDto termCancelEnrollmentSpan = termCancelEnrollmentSpans.stream().findFirst().get();
                // Identify the enrollment span that needs to be termed/canceled from the overlapping enrollment span list
                // The rest of the enrollment spans should be canceled
                overlappingEnrollmentSpans.forEach(enrollmentSpanDto -> {
                    if (enrollmentSpanDto.getEnrollmentSpanCode().equals(termCancelEnrollmentSpan.getEnrollmentSpanCode())) {
                        if (enrollmentSpanDto.getStartDate().isBefore(effectiveStartDate)) {
                            LocalDate termDate = effectiveStartDate.minusDays(1);
                            enrollmentSpanDto.setEndDate(termDate);
                            // Identify the premium spans that need to be termed or canceled
                            enrollmentSpanDto.getPremiumSpans().forEach(premiumSpanDto -> {
                                // Premiums spans where the start date is before the term date but the end date is
                                // after the term date, then the premium span has to be termed
                                if (premiumSpanDto.getStartDate().isBefore(termDate) &&
                                        premiumSpanDto.getEndDate().isAfter(termDate)) {
                                    premiumSpanDto.setEndDate(termDate);
                                }// If the premium span start date is after the term date then it has to be canceled
                                else if (premiumSpanDto.getStartDate().isAfter(termDate)) {
                                    cancelPremiumSpan(premiumSpanDto);
                                }
                            });
                        } else {
                            enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                            enrollmentSpanDto.setStatusTypeCode("CANCELED");
                            cancelPremiumSpans(enrollmentSpanDto.getPremiumSpans());
                        }
                    } else { // The rest of the enrollment spans should be canceled
                        enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                        enrollmentSpanDto.setStatusTypeCode("CANCELED");
                        cancelPremiumSpans(enrollmentSpanDto.getPremiumSpans());
                    }
                });
            }
            // This contains the list of enrollment span dtos that are updated depending on if they need to be
            // canceled or termed
            return overlappingEnrollmentSpans;
        }
        return null;
    }

    /**
     * Save the updated enrollment spans
     * @param enrollmentSpanDtos enrollment spans that need to be saved
     * @param account the account to which the enrollment spans belong
     * @return saved enrollment spans
     */
    private List<EnrollmentSpan> saveUpdatedEnrollmentSpans(List<EnrollmentSpanDto> enrollmentSpanDtos, Account account) {
        if(enrollmentSpanDtos != null && !enrollmentSpanDtos.isEmpty()){
            List<EnrollmentSpan> savedEnrollmentSpans = new ArrayList<>();
            enrollmentSpanDtos.forEach(enrollmentSpanDto -> {

                EnrollmentSpan enrollmentSpan =
                        enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(enrollmentSpanDto);
                enrollmentSpan.setAcctEnrollmentSpanSK(enrollmentSpanDto.getEnrollmentSpanSK());
                enrollmentSpan.setChanged(true);
                enrollmentSpan.setAccount(account);
                enrollmentSpan = enrollmentSpanRepository.save(enrollmentSpan);
                List<PremiumSpan> updatedPremiumSpans = premiumSpanHelper
                        .saveUpdatedPremiumSpans(enrollmentSpanDto.getPremiumSpans().stream().toList(),
                                enrollmentSpan);
                enrollmentSpan.setPremiumSpans(updatedPremiumSpans);
                savedEnrollmentSpans.add(enrollmentSpan);

            });
            return savedEnrollmentSpans;
        }

        return null;
    }

    /**
     * Get the enrollment spans that are immediately before the start date provided in the input
     * @param accountDto the account dto that contains the enrollment spans
     * @param startDate the start date before which the enrollment spans are requested
     * @param matchCancelSpans boolean to indicate of cancel spans should be considered a match
     * @return return the list of matched enrollment spans
     */
    private List<EnrollmentSpanDto> getPriorEnrollmentSpans(AccountDto accountDto, LocalDate startDate, boolean matchCancelSpans) {
        List<EnrollmentSpanDto> enrollmentSpanDtos = accountDto.getEnrollmentSpans().stream().toList();
        enrollmentSpanDtos =
                enrollmentSpanDtos.stream()
                        .sorted(Comparator.comparing(EnrollmentSpanDto::getStartDate))
                        .collect(Collectors.toList());
        enrollmentSpanDtos =
                enrollmentSpanDtos.stream()
                        .takeWhile(
                                enrollmentSpanDto ->
                                        enrollmentSpanDto.getEndDate().isBefore(startDate))
                        .collect(Collectors.toList());
        if(!matchCancelSpans){
            enrollmentSpanDtos = removeCanceledSpans(enrollmentSpanDtos);
        }
        if(enrollmentSpanDtos != null && !enrollmentSpanDtos.isEmpty()){
            LocalDate maxEndDate =
                    enrollmentSpanDtos.stream()
                            .map(EnrollmentSpanDto::getEndDate)
                            .max(Comparator.naturalOrder())
                            .get();
            List<EnrollmentSpanDto> priorYearEnrollmentSpans =
                    enrollmentSpanDtos.stream()
                            .takeWhile(
                                    enrollmentSpanDto -> enrollmentSpanDto.getEndDate().isEqual(maxEndDate))
                            .toList();
            return priorYearEnrollmentSpans;
        }
        return null;
    }

    /**
     * Update the impacted enrollment spans and create ones as needed
     * @param accountDto
     * @param transactionDto
     * @param account
     */
    @Override
    public void updateEnrollmentSpans(AccountDto accountDto, TransactionDto transactionDto, Account account) {
        LocalDate effectiveStartDate = transactionDto.getTransactionDetail().getEffectiveDate();
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEndDate();
        if(effectiveEndDate == null){
            effectiveEndDate = LocalDate.of(effectiveStartDate.getYear(), 12, 31);
        }
        // Get the enrollment span if any are affected
        List<EnrollmentSpanDto> overlappingEnrollmentSpans = getOverlappingEnrollmentSpans(accountDto,
                effectiveStartDate,
                effectiveEndDate, transactionDto.getTransactionDetail().getCoverageTypeCode());
        // Get the overlapping enrollment spans updated appropriately.
        // Note this will just update within the DTO and not in the DB
        overlappingEnrollmentSpans = updateOverlappingEnrollmentSpans(
                overlappingEnrollmentSpans,
                effectiveStartDate,
                effectiveEndDate);
        updateAccountDtoWithOverlappingSpans(accountDto, overlappingEnrollmentSpans);
        List<EnrollmentSpan> updatedEnrollmentSpans = saveUpdatedEnrollmentSpans(overlappingEnrollmentSpans,
                account);
        if(updatedEnrollmentSpans == null){
            updatedEnrollmentSpans = new ArrayList<>();
        }
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
            log.info("Saved Enrollment span code before :{}", enrollmentSpan.getEnrollmentSpanCode());
            log.info("Saved Enrollment span ztcn before:{}", enrollmentSpan.getZtcn());
        });
        EnrollmentSpan newEnrollmentSpan = createEnrollmentSpan(transactionDto,
                account,
                getPriorEnrollmentSpans(accountDto, effectiveStartDate, false));
        updatedEnrollmentSpans.add(newEnrollmentSpan);
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
            log.info("Saved Enrollment span code after :{}", enrollmentSpan.getEnrollmentSpanCode());
            log.info("Saved Enrollment span ztcn after:{}", enrollmentSpan.getZtcn());
        });
        account.setEnrollmentSpan(updatedEnrollmentSpans);
    }

    /**
     * Process the financial change for the enrollment span
     * @param changeTransactionInfo - Details associated with the change transaction
     * @param transactionDto - Change transaction data
     * @param account - The account entity
     * @param accountDto - the account for which the transaction was received
     * @param matchedEnrollmentSpanDto - The matched enrollment span
     */
    @Override
    public void processFinancialChange(ChangeTransactionInfo changeTransactionInfo,
                                       TransactionDto transactionDto,
                                       Account account,
                                       AccountDto accountDto,
                                       EnrollmentSpanDto matchedEnrollmentSpanDto) {
        EnrollmentSpan enrollmentSpan = enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(matchedEnrollmentSpanDto);
        enrollmentSpan.setAccount(account);
        enrollmentSpan.setChanged(false);
        enrollmentSpan = enrollmentSpanRepository.save(enrollmentSpan);
        account.setEnrollmentSpan(List.of(enrollmentSpan));
        premiumSpanHelper.processFinancialChange(changeTransactionInfo, transactionDto,
                account, accountDto, enrollmentSpan, matchedEnrollmentSpanDto);
    }

    /**
     * Determine what should be end date
     * @param startDate
     * @param endDate
     * @return
     */
    private LocalDate determineEndDate(LocalDate startDate, LocalDate endDate){
        // if the end date is present in the transaction use that end date
        if(endDate != null){
            return endDate;
        }else{
            // if it is not present then the 12/31 of the same year as the start date should be
            int year = startDate.getYear();
            return LocalDate.of(year, 12, 31);
        }
    }

    /**
     * Get the primay subscriber in the transaction
     * @param transactionDto
     * @return
     */
    private TransactionMemberDto    getPrimaryMember(TransactionDto transactionDto){
        TransactionMemberDto primaryMember = transactionDto.getMembers().stream().filter(memberDto -> {
            return memberDto.getRelationshipTypeCode().equals("HOH");
        }).findFirst().get();
        return primaryMember;
    }

    /**
     * Get the exchange subscriber id
     * @param primarySubscriber
     * @return
     */
    private String getExchangeSubscriberId(TransactionMemberDto primarySubscriber){
        Optional<TransactionMemberIdentifierDto> optionalIdentifier = primarySubscriber.getIdentifiers().stream().filter(memberIdentifierDto -> {
            return memberIdentifierDto.getIdentifierTypeCode().equals("EXCHSUBID");
        }).findFirst();
        if(optionalIdentifier.isPresent()){
            return optionalIdentifier.get().getIdentifierValue();
        }else {
            return null;
        }
    }

    /**
     * Determine the effectuation date of the enrollment span
     * @param transactionDto
     * @param priorEnrollmentSpans
     * @return
     */
    private LocalDate determineEffectuationDate(TransactionDto transactionDto,
                                                List<EnrollmentSpanDto> priorEnrollmentSpans){
        Optional<TransactionRateDto> optionalTotResAmt = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("TOTRESAMT");
        }).findFirst();

        if(optionalTotResAmt.isPresent()){
            TransactionRateDto rateDto = optionalTotResAmt.get();
            BigDecimal totResAmt = rateDto.getTransactionRate();
            if(totResAmt.compareTo(BigDecimal.valueOf(0)) == 0){
                return LocalDate.now();
            }
        }
        Optional<EnrollmentSpanDto> optionalPriorEnrollmentSpan = priorEnrollmentSpans.stream()
                .filter(
                        enrollmentSpanDto -> enrollmentSpanDto.getStatusTypeCode().equals("ENROLLED")).findFirst();
        if(optionalPriorEnrollmentSpan.isPresent()){
            EnrollmentSpanDto priorEnrollmentSpan = optionalPriorEnrollmentSpan.get();
            boolean samePlan = isSamePlan(transactionDto.getTransactionDetail().getPlanId(), priorEnrollmentSpan.getPlanId());
            boolean gapInCoverage = isThereGapInCoverage(transactionDto.getTransactionDetail().getEffectiveDate(), priorEnrollmentSpan);
            if(samePlan && !gapInCoverage){
                return LocalDate.now();
            }
        }
        return null;
    }

    /**
     * Determine the enrollment span status
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpans
     * @return
     */
    @Override
    public String determineEnrollmentSpanStatus(EnrollmentSpan currentEnrollmentSpan,
                                                 List<EnrollmentSpanDto> priorEnrollmentSpans){
        boolean isEndDateGreaterThanStartDate = currentEnrollmentSpan.getEndDate().isAfter(currentEnrollmentSpan.getStartDate());
        boolean isEndDateLessThanStartDate = currentEnrollmentSpan.getEndDate().isBefore(currentEnrollmentSpan.getStartDate());
        boolean isEndDateEqualToStartDate =currentEnrollmentSpan.getEndDate().isEqual(currentEnrollmentSpan.getStartDate());
        if(isDelinquent(currentEnrollmentSpan, priorEnrollmentSpans) == 1){
            return EnrollmentSpanStatus.DELINQUENT.name();
        }else if(isDelinquent(currentEnrollmentSpan, priorEnrollmentSpans) == -1){
            return EnrollmentSpanStatus.SUSPENDED.name();
        }
        if (isEndDateLessThanStartDate) {
            return EnrollmentSpanStatus.CANCELED.name();
        }
        if(currentEnrollmentSpan.getEffectuationDate() != null &&
                (isEndDateGreaterThanStartDate || isEndDateEqualToStartDate) &&
                !currentEnrollmentSpan.isDelinqInd()){
            return EnrollmentSpanStatus.ENROLLED.name();
        }
        if(currentEnrollmentSpan.getEffectuationDate() == null &&
                (isEndDateGreaterThanStartDate || isEndDateEqualToStartDate) &&
                !currentEnrollmentSpan.isDelinqInd()){
            return EnrollmentSpanStatus.PRE_MEMBER.name();
        }
        return EnrollmentSpanStatus.NO_VALID_STATUS.name();
    }

    /**
     * Determine if the current enrollment span is delinquent or suspended or neither
     * This method will return
     * 1 - Delinquent
     * -1 - Suspended
     * 0 - Neither Suspended nor Delinquent
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpans
     * @return
     */
    private int isDelinquent(EnrollmentSpan currentEnrollmentSpan,
                                 List<EnrollmentSpanDto> priorEnrollmentSpans){
        // Check if the effectuation date is not NULL and the delinquency flag is set to true and
        // the end date is not before the start date of the enrollment span
        log.info("currentEnrollmentSpan.getEffectuationDate() != null:{}", currentEnrollmentSpan.getEffectuationDate() != null);
        log.info("currentEnrollmentSpan.isDelinqInd():{}",currentEnrollmentSpan.isDelinqInd());
        log.info("!currentEnrollmentSpan.getStartDate()\n" +
                "                        .isAfter(currentEnrollmentSpan.getEndDate():{}",!currentEnrollmentSpan.getStartDate()
                .isAfter(currentEnrollmentSpan.getEndDate()));
        if(currentEnrollmentSpan.getEffectuationDate() != null &&
            currentEnrollmentSpan.isDelinqInd() &&
                !currentEnrollmentSpan.getStartDate()
                        .isAfter(currentEnrollmentSpan.getEndDate())){
            // if the above conditions are satisfied check if the claim paid through date is not null
            if(currentEnrollmentSpan.getClaimPaidThroughDate() != null){
                // If the claim paid through date is not null then
                // Check if the claim paid through date is greater than the current system date
                // and if the plan of the current and the prior enrollment spans are same
                // and check if there is any gap in coverage between the prior and current enrollment spans
                // and if the prior enrollment span is delinquent status
                boolean localDateIsBefore = LocalDate.now().isBefore(currentEnrollmentSpan.getClaimPaidThroughDate());
                boolean localDateIsAfter = LocalDate.now().isAfter(currentEnrollmentSpan.getClaimPaidThroughDate());
                boolean localDateIsEqual = LocalDate.now().isEqual(currentEnrollmentSpan.getClaimPaidThroughDate());
                AtomicBoolean samePlan = new AtomicBoolean(false);
                AtomicBoolean gapInCoverage = new AtomicBoolean(false);
                AtomicBoolean priorSpanStatus = new AtomicBoolean(false);
                boolean delinquent = false;
                if(priorEnrollmentSpans != null && !priorEnrollmentSpans.isEmpty()){
                    delinquent = priorEnrollmentSpans.stream().anyMatch(priorEnrollmentSpan -> {
                        samePlan.set(priorEnrollmentSpan != null &&
                                isSamePlan(currentEnrollmentSpan.getPlanId(), priorEnrollmentSpan.getPlanId()));
                        gapInCoverage.set(priorEnrollmentSpan != null &&
                                isThereGapInCoverage(currentEnrollmentSpan.getStartDate(), priorEnrollmentSpan));
                        priorSpanStatus.set(priorEnrollmentSpan != null &&
                                priorEnrollmentSpan.getStatusTypeCode().equals(EnrollmentSpanStatus.DELINQUENT.name()));
                        return localDateIsBefore || localDateIsEqual ||
                                (samePlan.get() &&
                                        !gapInCoverage.get() &&
                                        priorSpanStatus.get());
                    });
                }

//                boolean delinquent = localDateIsBefore || localDateIsEqual ||
//                        (samePlan.get() &&
//                                !gapInCoverage.get() &&
//                                priorSpanStatus.get());

                log.info("Delinquent:{}", delinquent);
                if(delinquent){
                    return 1;
                }else{
                    boolean suspended = localDateIsAfter || localDateIsEqual;
                    return -1;
                }
            }else{
                // If the claim paid through date is null then
                // set the enrollment span status should be SUSPENDED
                // At this point the delinquency flag is yes and the effectuation date is not null
                return -1;
            }
        }else {
            // if effectuation date is not present or delinquency flag is not set to true or if the start date of the
            // enrollment span is greater than the end date of the enrollment span, then the enrollment span is not
            // delinquent
            return 0;
        }
    }

    /**
     * Determine if the planIds are same
     * @param currentPlanId
     * @param priorPlanId
     * @return
     */
    private boolean isSamePlan(String currentPlanId,
                               String priorPlanId){
        return currentPlanId.equals(priorPlanId);
    }

    /**
     * Determine if there is a gap between the current and the prior enrollment span
     * @param effectiveStartDate of the enrollment span to be created
     * @param priorEnrollmentSpan the prior year enrollment span
     * @return return true if there is a gap in coverage
     */
    private boolean isThereGapInCoverage(LocalDate effectiveStartDate,
                                         EnrollmentSpanDto priorEnrollmentSpan){
        if(priorEnrollmentSpan.getStatusTypeCode().equals(EnrollmentSpanStatus.CANCELED)){
            return true;
        }else{
            long numOfDays = ChronoUnit.DAYS.between(priorEnrollmentSpan.getEndDate(),
                    effectiveStartDate);
            // numOfDays will be 1 if the end date of the prior enrollment span is the day prior to the
            // start of the current enrollment span

            // num of days will be greater than one if there is a gap between the end of the prior enrollment span
            // and start of the current enrollment span

            // num of days will be 0 if the end date of the prior enrollment span is same as that of the start date of the
            // current enrollment span

            // number of days will be -ve if the end date of the prior enrollment span is greater than the start date
            // of the current enrollment span
            if(numOfDays > 1){
                // This means that there is a gap between the end of the prior enrollment span
                // and start of the current enrollment span hence return tru
                return true;
            }else{
                // in all other cases there is no gap, hence return false
                return false;
            }
        }
    }

    /**
     * Cancel the premium span and set the status
     * @param premiumSpanDto cancel the premium span
     */
    private void cancelPremiumSpan(PremiumSpanDto premiumSpanDto){
        // Check if the premium span is not already canceled before setting it to cancel
        if(!premiumSpanDto.getStartDate().isEqual(premiumSpanDto.getEndDate()) ||
                (premiumSpanDto.getStartDate().isEqual(premiumSpanDto.getEndDate()) &&
                        premiumSpanDto.getStatusTypeCode().equals("ACTIVE"))){
            premiumSpanDto.setEndDate(premiumSpanDto.getStartDate());
            premiumSpanDto.setStatusTypeCode("CANCELED");
            premiumSpanDto.setChanged(new AtomicBoolean(true));
        }

    }

    /**
     * Cancel all the premium spans
     * @param premiumSpanDtos premium spans to be canceled
     */
    private void cancelPremiumSpans(Set<PremiumSpanDto> premiumSpanDtos){
        premiumSpanDtos.forEach(this::cancelPremiumSpan);
    }

    /**
     * Remove canceled enrollment span from the list
     * @param enrollmentSpanDtos
     * @return
     */
    private List<EnrollmentSpanDto> removeCanceledSpans(List<EnrollmentSpanDto> enrollmentSpanDtos){
        List<EnrollmentSpanDto> nonCanceledEnrollmentSpans = enrollmentSpanDtos.stream()
                .filter(
                        enrollmentSpanDto ->
                                !enrollmentSpanDto.getStatusTypeCode()
                                        .equals(EnrollmentSpanStatus.CANCELED))
                .collect(Collectors.toList());
        return nonCanceledEnrollmentSpans;
    }

    /**
     * The account dto object will be updated with the overlapping enrollment spans
     * @param accountDto The account dto that is to be updated
     * @param overlappingEnrollmentSpans the overlapping enrollment spans that needs to be added back to the account
     */
    private void updateAccountDtoWithOverlappingSpans(AccountDto accountDto,
                                                      List<EnrollmentSpanDto> overlappingEnrollmentSpans) {
        if(overlappingEnrollmentSpans == null || overlappingEnrollmentSpans.isEmpty()){
            return;
        }else{
            // todo add the overlapping enrollment spans
            Set<EnrollmentSpanDto> accountEnrollmentSpans = accountDto.getEnrollmentSpans();
            overlappingEnrollmentSpans.forEach(enrollmentSpanDto -> {
                Optional<EnrollmentSpanDto> optionalEnrollmentSpan = accountEnrollmentSpans.stream()
                        .filter(
                                accountEnrollmentSpan ->
                                        accountEnrollmentSpan.getEnrollmentSpanCode().equals(
                                                enrollmentSpanDto.getEnrollmentSpanCode()))
                        .findFirst();
                optionalEnrollmentSpan.ifPresent(accountEnrollmentSpans::remove);

            });
            accountEnrollmentSpans.addAll(overlappingEnrollmentSpans);
        }
    }
}
