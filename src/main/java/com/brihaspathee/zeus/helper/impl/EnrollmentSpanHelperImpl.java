package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.broker.producer.AccountProcessingValidationProducer;
import com.brihaspathee.zeus.constants.AdditionalMaintenanceReasonCode;
import com.brihaspathee.zeus.constants.EnrollmentSpanStatus;
import com.brihaspathee.zeus.constants.EnrollmentType;
import com.brihaspathee.zeus.constants.PremiumSpanStatus;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.domain.repository.EnrollmentSpanRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.MemberPremiumDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.*;
import com.brihaspathee.zeus.exception.NoMatchingEnrollmentSpanException;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.helper.interfaces.PremiumSpanHelper;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.brihaspathee.zeus.mapper.interfaces.EnrollmentSpanMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.core.env.Environment;
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
     * Processing validation producer to send the transaction for validation
     */
    private final AccountProcessingValidationProducer accountProcessingValidationProducer;

    /**
     * The spring environment instance
     */
    private final Environment environment;


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
                .source(transactionDto.getSource())
                .stateTypeCode(transactionDto.getTradingPartnerDto().getStateTypeCode())
                .marketplaceTypeCode(transactionDto.getTradingPartnerDto().getMarketplaceTypeCode())
                .businessUnitTypeCode(transactionDto.getTradingPartnerDto().getBusinessTypeCode())
                .coverageTypeCode(transactionDto.getTransactionDetail().getCoverageTypeCode())
                .startDate(transactionDto.getTransactionDetail().getEffectiveDate())
                // get the exchange subscriber id and set it
                .exchangeSubscriberId(getExchangeSubscriberId(primarySubscriber))
                // determine the effectuation date
                .effectuationDate(determineEffectuationDate(transactionDto, priorEnrollmentSpans))
                // determine the enrollment type
                .enrollmentType(determineEnrollmentType(transactionDto.getTransactionAttributes()))
                .planId(transactionDto.getTransactionDetail().getPlanId())
                .productTypeCode("HMO")
                .groupPolicyId(transactionDto.getTransactionDetail().getGroupPolicyId())
                .effectiveReason(transactionDto.getTransactionDetail().getMaintenanceReasonCode())
                .changed(true)
                .build();
        // Determine the end date of the enrollment span
        LocalDate enrollmentSpanEndDate = determineEndDate(
                transactionDto.getTransactionDetail().getEffectiveDate(),
                transactionDto.getTransactionDetail().getEndDate());
        enrollmentSpan.setEndDate(enrollmentSpanEndDate);
        // If the end date of the enrollment span being added is less than 12/31
        // then set the term reason
        if (enrollmentSpanEndDate.isBefore(LocalDate.of(transactionDto.getTransactionDetail().getEffectiveDate().getYear(),
                12, 31))){
            enrollmentSpan.setTermReason("VOLWITH");
        }
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
     * @param transactionDto the transaction that is being processed
     * @return return the enrollment spans that are overlapping with the dates that are passed
     */
    @Override
    public List<EnrollmentSpanDto> getOverlappingEnrollmentSpans(AccountDto accountDto,
                                                                 TransactionDto transactionDto) {
        if(accountDto.getEnrollmentSpans() == null || accountDto.getEnrollmentSpans().isEmpty()){
            return null;
        }
        // the start date that is to be used
        LocalDate effectiveStartDate = transactionDto.getTransactionDetail().getEffectiveDate();
        // the end date that is to be used
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEndDate();
        if(effectiveEndDate == null){
            effectiveEndDate = LocalDate.of(effectiveStartDate.getYear(), 12, 31);
        }
        //  identifies the type of coverage "FAM" or "DEP"
        String coverageTypeCode = transactionDto.getTransactionDetail().getCoverageTypeCode();
        // The list of members present in the transaction
        List<TransactionMemberDto> transactionMemberDtos = transactionDto.getMembers();

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
        LocalDate finalEffectiveEndDate = effectiveEndDate;
        overlappingEnrollmentSpans = overlappingEnrollmentSpans.stream()
                .filter(enrollmentSpanDto -> {
                    if(enrollmentSpanDto.getStatusTypeCode().equals("CANCELED")){
                        return false;
                    }
                    LocalDate enrollmentSpanStartDate = enrollmentSpanDto.getStartDate();
                    return enrollmentSpanStartDate.isBefore(finalEffectiveEndDate);
                })
                .collect(Collectors.toSet());
        if(coverageTypeCode.equals("DEP") && !overlappingEnrollmentSpans.isEmpty()){
            // If the coverage type code is "DEP"
            // There can be overlapping enrollment spans in the same account with different set
            // of members than those that is being added in the transaction
            // Such enrollment spans should not be termed or canceled because of the
            // addition of the new enrollment span
            // So fileOverlappingSpans will check for such enrollment spans and filter them out
            // keeping only enrollment spans that have one or more members that exists in the transaction
            overlappingEnrollmentSpans = filterOverlappingSpans(overlappingEnrollmentSpans, transactionMemberDtos, effectiveStartDate);
        }
        // Return null if there are no overlapping enrollment spans
        if(overlappingEnrollmentSpans == null || overlappingEnrollmentSpans.isEmpty()){
            return null;
        }else {

            return overlappingEnrollmentSpans.stream().toList();


        }
    }

    /**
     * This method will filter out the enrollment spans that does not belong to the members
     * in the transaction so that such enrollment spans don't get termed or canceled.
     * @param overlappingSpans
     * @param dependents
     * @param effectiveDate
     * @return
     */
    private Set<EnrollmentSpanDto> filterOverlappingSpans(Set<EnrollmentSpanDto> overlappingSpans,
                                                        List<TransactionMemberDto> dependents,
                                                        LocalDate effectiveDate){
        // return null if overlapping spans passed in the input is null or is empty
        if(overlappingSpans == null || overlappingSpans.isEmpty()){
            return null;
        }
//        log.info("Overlapping Spans before filtering:{}", overlappingSpans);
        // Get all the account member codes that were matched with the members
        // in the transaction. If the member in the transaction was not matched with
        // any member in the account, then they will obviously be not present in the enrollment span
        Set<String> transactionMembers = dependents.stream()
                        .map(TransactionMemberDto::getMmsMemberCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
//        log.info("Transaction Effective Date:{}", effectiveDate);
//        log.info("Transaction Members:{}", transactionMembers);
        // Filter out only the enrollment span that contains members who are in the transaction
        // because only those enrollment spans are the ones that are truly overlapping
        // and needs to be termed or canceled
        overlappingSpans = overlappingSpans.stream().filter(enrollmentSpanDto -> {
            // Retrieve the premium spans associated with the enrollment span
            // that are active and have start date that is equal or greater than the
            // effective date of the transaction
            List<PremiumSpanDto> premiumSpanDtos = enrollmentSpanDto.getPremiumSpans().stream()
                    .filter(premiumSpanDto -> (premiumSpanDto.getStartDate().equals(effectiveDate) ||
                            premiumSpanDto.getStartDate().isAfter(effectiveDate)) &&
                            premiumSpanDto.getStatusTypeCode().equals(PremiumSpanStatus.ACTIVE.toString()))
                    .toList();
//            log.info("Premium Span Dtos:{}", premiumSpanDtos);
            // Add all the member in these premium spans to the below set.
            // Set is used instead of List to avoid duplicating the member codes
            Set<String> enrollmentSpanMembers = new HashSet<>();
            premiumSpanDtos.forEach(premiumSpanDto ->
                    enrollmentSpanMembers.addAll(
                            premiumSpanDto
                                    .getMemberPremiumSpans()
                                    .stream()
                                    .map(MemberPremiumDto::getMemberCode)
                                    .collect(Collectors.toSet())));
//            log.info("Enrollment Span Members: {}", enrollmentSpanMembers);
            // Return true if any of the member in the enrollment span are present in the transaction
            return enrollmentSpanMembers.stream().anyMatch(transactionMembers::contains);
        }).collect(Collectors.toSet());
        // Return the overlapping enrollment spans that contain one or more member who are present in the
        // transaction
        return overlappingSpans;
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
                            enrollmentSpanDto.setTermReason("VOLWITH");
                            enrollmentSpanDto.setChanged(new AtomicBoolean(true));
                            // Identify the premium spans that need to be termed or canceled
                            enrollmentSpanDto.getPremiumSpans().forEach(premiumSpanDto -> {
                                // Premiums spans where the start date is before the term date but the end date is
                                // after the term date, then the premium span has to be termed
                                if (premiumSpanDto.getStartDate().isBefore(termDate) &&
                                        premiumSpanDto.getEndDate().isAfter(termDate)) {
                                    premiumSpanDto.setEndDate(termDate);
                                    premiumSpanDto.setChanged(new AtomicBoolean(true));
                                }// If the premium span start date is after the term date then it has to be canceled
                                else if (premiumSpanDto.getStartDate().isAfter(termDate)) {
                                    cancelPremiumSpan(premiumSpanDto);
                                }
                            });
                        } else {
                            enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                            enrollmentSpanDto.setStatusTypeCode("CANCELED");
                            enrollmentSpanDto.setTermReason("VOLWITH");
                            enrollmentSpanDto.setChanged(new AtomicBoolean(true));
                            cancelPremiumSpans(enrollmentSpanDto.getPremiumSpans());
                        }
                    } else { // The rest of the enrollment spans should be canceled
                        enrollmentSpanDto.setEndDate(enrollmentSpanDto.getStartDate());
                        enrollmentSpanDto.setStatusTypeCode("CANCELED");
                        enrollmentSpanDto.setTermReason("VOLWITH");
                        enrollmentSpanDto.setChanged(new AtomicBoolean(true));
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
        // get all the enrollment spans from the account
        List<EnrollmentSpanDto> enrollmentSpanDtos = accountDto.getEnrollmentSpans().stream().toList();
        // Sort the enrollment spans by the ascending order of the date
        enrollmentSpanDtos =
                enrollmentSpanDtos.stream()
                        .sorted(Comparator.comparing(EnrollmentSpanDto::getStartDate))
                        .collect(Collectors.toList());
        // Get all the enrollment spans that is prior to the start date provided in the input
        enrollmentSpanDtos =
                enrollmentSpanDtos.stream()
                        .takeWhile(
                                enrollmentSpanDto ->
                                        enrollmentSpanDto.getStartDate().isBefore(startDate))
                        .collect(Collectors.toList());
        if(!matchCancelSpans){
            // Remove canceled spans if match cancel spans is "FALSE"
            enrollmentSpanDtos = removeCanceledSpans(enrollmentSpanDtos);
        }
        if(enrollmentSpanDtos != null && !enrollmentSpanDtos.isEmpty()){
            // Retrieve the maximum end date of all the enrollment spans
            LocalDate maxEndDate =
                    enrollmentSpanDtos.stream()
                            .map(EnrollmentSpanDto::getEndDate)
                            .max(Comparator.naturalOrder())
                            .get();
            // Get all the enrollment spans with the end date that matches the max end date
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
     * @param overlappingEnrollmentSpans
     */
    @Override
    public void updateEnrollmentSpans(AccountDto accountDto,
                                      TransactionDto transactionDto,
                                      Account account,
                                      List<EnrollmentSpanDto> overlappingEnrollmentSpans) throws JsonProcessingException {
        LocalDate effectiveStartDate = transactionDto.getTransactionDetail().getEffectiveDate();
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEndDate();
        if(effectiveEndDate == null){
            effectiveEndDate = LocalDate.of(effectiveStartDate.getYear(), 12, 31);
        }
        log.info("Effective start date of the new enrollment span:{}", effectiveStartDate);
        log.info("Effective end date of the new enrollment span:{}", effectiveEndDate);
        log.info("Existing overlapping spans are:{}", overlappingEnrollmentSpans);
        // Get the overlapping enrollment spans updated appropriately.
        // Note this will just update within the DTO and not in the DB
        overlappingEnrollmentSpans = updateOverlappingEnrollmentSpans(
                overlappingEnrollmentSpans,
                effectiveStartDate,
                effectiveEndDate);
        log.info("Overlapping spans once the updates are made:{}", overlappingEnrollmentSpans);
        updateAccountDtoWithOverlappingSpans(accountDto, overlappingEnrollmentSpans);
        List<EnrollmentSpan> updatedEnrollmentSpans = saveUpdatedEnrollmentSpans(overlappingEnrollmentSpans,
                account);
        if(updatedEnrollmentSpans == null){
            updatedEnrollmentSpans = new ArrayList<>();
        }
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
//            log.info("Saved Enrollment span code before :{}", enrollmentSpan.getEnrollmentSpanCode());
//            log.info("Saved Enrollment span ztcn before:{}", enrollmentSpan.getZtcn());
        });
        EnrollmentSpan newEnrollmentSpan = createEnrollmentSpan(transactionDto,
                account,
                getPriorEnrollmentSpans(accountDto, effectiveStartDate, false));
        updatedEnrollmentSpans.add(newEnrollmentSpan);
        updatedEnrollmentSpans.forEach(enrollmentSpan -> {
//            log.info("Saved Enrollment span code after :{}", enrollmentSpan.getEnrollmentSpanCode());
//            log.info("Saved Enrollment span ztcn after:{}", enrollmentSpan.getZtcn());
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
     * Cancel or term the requested enrollment span
     * @param matchedEnrollmentSpanDto
     * @param transactionDto
     * @param account
     */
    @Override
    public void cancelTermEnrollmentSpan(EnrollmentSpanDto matchedEnrollmentSpanDto, TransactionDto transactionDto, Account account) {
        LocalDate effectiveEndDate = transactionDto.getTransactionDetail().getEffectiveDate();
        boolean isTermRequested = isTermRequested(transactionDto);
        if(effectiveEndDate.isBefore(matchedEnrollmentSpanDto.getStartDate()) ||
                (effectiveEndDate.equals(matchedEnrollmentSpanDto.getStartDate()) && !isTermRequested)
            ){
            // The request is to cancel the span if
            // end date in the transaction is before the start date of the matched enrollment span
            // OR
            // end date in the transaction is equal to start date of the matched enrollment span and
            // either term is not requested in the transaction

            // Note: If the end date in the transaction is equal to the start date and a termination of the span
            // is requested in the transaction, then it means tha the span has to be active for One Day
            EnrollmentSpan enrollmentSpan = termCancelEnrollmentSpan(account, matchedEnrollmentSpanDto,
                    transactionDto.getTransactionDetail().getEffectiveDate(),
                    transactionDto.getTransactionDetail().getMaintenanceReasonCode(),
                    EnrollmentSpanStatus.CANCELED.toString());
            premiumSpanHelper.cancelPremiumSpans(matchedEnrollmentSpanDto, enrollmentSpan);
        }else{
            // Term the enrollment span if the request is to term the span
            EnrollmentSpan enrollmentSpan = termCancelEnrollmentSpan(account, matchedEnrollmentSpanDto,
                    transactionDto.getTransactionDetail().getEffectiveDate(),
                    transactionDto.getTransactionDetail().getMaintenanceReasonCode(),
                    null);
            premiumSpanHelper.termPremiumSpans(matchedEnrollmentSpanDto, enrollmentSpan);
        }
    }

    /**
     * Check if termination is requested in the transaction
     * @param transactionDto
     * @return
     */
    private boolean isTermRequested(TransactionDto transactionDto){
        String maintenanceReasonCode = transactionDto.getTransactionDetail().getMaintenanceReasonCode();
        String amrcValue = getAMRCValue(transactionDto.getTransactionAttributes());
        // If maintenance reason code is "Termination of Benefits" or
        // if the AMRC Value in the transaction is "TERM" then
        // the transaction is requesting the enrollment span to be termed
        return maintenanceReasonCode.equals("TERMOFBEN") ||
                (amrcValue != null && amrcValue.equals("TERM"));
    }

    /**
     * Get the amrc value if received in the transaction
     * @param transactionAttributeDtos
     * @return
     */
    private String getAMRCValue(List<TransactionAttributeDto> transactionAttributeDtos){
        if(transactionAttributeDtos == null || transactionAttributeDtos.isEmpty()){
            return null;
        }
        Optional<TransactionAttributeDto> optionalAttributeDto = transactionAttributeDtos.stream()
                .filter(transactionAttributeDto ->
                        transactionAttributeDto.getTransactionAttributeTypeCode()
                                .equals("AMRC")).findFirst();
        return optionalAttributeDto.map(TransactionAttributeDto::getTransactionAttributeValue)
                .orElse(null);
    }

    /**
     * Reinstate enrollment span received in the transaction
     * @param accountDto
     * @param transactionDto
     * @param account
     */
    @Override
    public void reinstateEnrollmentSpan(AccountDto accountDto, TransactionDto transactionDto, Account account) {
        // Identify the enrollment span that needs to be reinstated
        EnrollmentSpanDto matchedEnrollmentSpanDto = getMatchedEnrollmentSpan(accountDto.getEnrollmentSpans(),
                transactionDto.getTransactionDetail().getGroupPolicyId());
        LocalDate effectiveDate = transactionDto.getTransactionDetail().getEffectiveDate();
        int year = effectiveDate.getYear();
        matchedEnrollmentSpanDto.setEndDate(LocalDate.of(year, 12, 31));
        List<EnrollmentSpanDto> priorEnrollmentSpans = getPriorEnrollmentSpans(accountDto,
                effectiveDate, false);
        EnrollmentSpanStatusDto enrollmentSpanStatusDto = EnrollmentSpanStatusDto.builder()
                .currentEnrollmentSpan(matchedEnrollmentSpanDto)
                .priorEnrollmentSpans(priorEnrollmentSpans)
                .build();
        String status = determineStatus(enrollmentSpanStatusDto);
        matchedEnrollmentSpanDto.setStatusTypeCode(status);
        matchedEnrollmentSpanDto.setChanged(new AtomicBoolean(true));
        matchedEnrollmentSpanDto.setTermReason(null);
        EnrollmentSpan enrollmentSpan = enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(matchedEnrollmentSpanDto);
        enrollmentSpan.setAccount(account);
        enrollmentSpan = enrollmentSpanRepository.save(enrollmentSpan);
        account.setEnrollmentSpan(List.of(enrollmentSpan));
        premiumSpanHelper.reinstatePremiumSpans(matchedEnrollmentSpanDto, enrollmentSpan);
    }

    /**
     * Term or cancel the enrollment span
     * @param account
     * @param matchedEnrollmentSpanDto
     * @param endDate
     * @param termReasonCode
     * @param spanStatus
     * @return
     */
    private EnrollmentSpan termCancelEnrollmentSpan(Account account,
                                                    EnrollmentSpanDto matchedEnrollmentSpanDto,
                                                    LocalDate endDate,
                                                    String termReasonCode,
                                                    String spanStatus){
        matchedEnrollmentSpanDto.setChanged(new AtomicBoolean(true));
        matchedEnrollmentSpanDto.setEndDate(endDate);
        matchedEnrollmentSpanDto.setTermReason(termReasonCode);
        if(spanStatus != null){
            matchedEnrollmentSpanDto.setStatusTypeCode(spanStatus);
        }
        EnrollmentSpan enrollmentSpan = enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(matchedEnrollmentSpanDto);
        enrollmentSpan.setAccount(account);
        enrollmentSpan = enrollmentSpanRepository.save(enrollmentSpan);
        account.setEnrollmentSpan(List.of(enrollmentSpan));
        return enrollmentSpan;
    }

    /**
     * Identify if there is an enrollment span that matches the group policy id received as input
     * @param enrollmentSpanDtos
     * @param groupPolicyId
     * @return
     */
    @Override
    public EnrollmentSpanDto getMatchedEnrollmentSpan(Set<EnrollmentSpanDto> enrollmentSpanDtos, String groupPolicyId){
        return enrollmentSpanDtos.stream().filter(
                enrollmentSpanDto1 -> enrollmentSpanDto1.getGroupPolicyId()
                        .equals
                                (groupPolicyId)
        ).findFirst().orElseThrow(() ->
                new NoMatchingEnrollmentSpanException("No enrollment span matched group policy id " +
                        groupPolicyId));
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
//        log.info("Prior Enrollment Spans - effectuation date determination:{}", priorEnrollmentSpans);
//        Optional<TransactionRateDto> optionalTotResAmt = transactionDto.getTransactionRates().stream().filter(rateDto -> {
//            return rateDto.getRateTypeCode().equals("TOTRESAMT");
//        }).findFirst();

        // Determine the member total responsibility amount
        BigDecimal totalResponsibilityAmount = determineMemberResponsibility(transactionDto.getTransactionRates());
        if(totalResponsibilityAmount.compareTo(BigDecimal.valueOf(0)) == 0){
//                return LocalDate.now();
            return transactionDto.getTransactionReceivedDate().toLocalDate();
        }
//        if(optionalTotResAmt.isPresent()){
//            TransactionRateDto rateDto = optionalTotResAmt.get();
//            BigDecimal totResAmt = rateDto.getTransactionRate();
//            if(totResAmt.compareTo(BigDecimal.valueOf(0)) == 0){
////                return LocalDate.now();
//                return transactionDto.getTransactionReceivedDate().toLocalDate();
//            }
//        }
        if(priorEnrollmentSpans!=null){
            Optional<EnrollmentSpanDto> optionalPriorEnrollmentSpan = priorEnrollmentSpans.stream()
                    .filter(
                            enrollmentSpanDto -> enrollmentSpanDto.getStatusTypeCode().equals("ENROLLED")).findFirst();
            if(optionalPriorEnrollmentSpan.isPresent()){
                EnrollmentSpanDto priorEnrollmentSpan = optionalPriorEnrollmentSpan.get();
                boolean samePlan = isSamePlan(transactionDto.getTransactionDetail().getPlanId(), priorEnrollmentSpan.getPlanId());
                boolean gapInCoverage = isThereGapInCoverage(transactionDto.getTransactionDetail().getEffectiveDate(), priorEnrollmentSpan);
//                log.info("Same Plan:{}", samePlan);
//                log.info("Gap In Coverage:{}", gapInCoverage);
                if(samePlan && !gapInCoverage){
//                    return LocalDate.now();
                    return transactionDto.getTransactionReceivedDate().toLocalDate();
                }
            }
        }
        return null;
    }

    /**
     * Determine the members total responsibility amount
     * @param rateDtos
     * @return
     */
    private BigDecimal determineMemberResponsibility(List<TransactionRateDto> rateDtos){
        List<TransactionRateDto> resAmtRates = rateDtos.stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("TOTRESAMT");
        }).toList();
        Optional<TransactionRateDto> optionalResAmt = resAmtRates.stream()
                .min(Comparator.comparing(TransactionRateDto::getRateStartDate));
        if(optionalResAmt.isPresent()){
            return optionalResAmt.get().getTransactionRate();
        }else{
            return BigDecimal.ZERO;
        }
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

    /**
     * Determine the enrollment type of the enrollment span
     * @param transactionAttributeDtos
     * @return
     */
    private String determineEnrollmentType(List<TransactionAttributeDto> transactionAttributeDtos){
        Optional<TransactionAttributeDto> optionalAMRC = transactionAttributeDtos.stream().filter(
                transactionAttribute -> transactionAttribute.getTransactionAttributeTypeCode().equals("AMRC")
        ).findFirst();
        if(optionalAMRC.isPresent()){
            TransactionAttributeDto transactionAttributeDto = optionalAMRC.get();
            String attributeValue = transactionAttributeDto.getTransactionAttributeValue();
            if(attributeValue.equals(AdditionalMaintenanceReasonCode.PASSIVE_ENROLLMENT.toString()) ||
                    attributeValue.equals(AdditionalMaintenanceReasonCode.PASSIVE.toString())){
                return EnrollmentType.PASSIVE.toString();
            }
        }
        return EnrollmentType.ACTIVE.toString();
    }
}
