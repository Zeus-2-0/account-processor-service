package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.constants.EnrollmentSpanStatus;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.repository.EnrollmentSpanRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
import com.brihaspathee.zeus.dto.transaction.TransactionRateDto;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.helper.interfaces.PremiumSpanHelper;
import com.brihaspathee.zeus.mapper.interfaces.EnrollmentSpanMapper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import com.brihaspathee.zeus.web.model.EnrollmentSpanStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 24, November 2022
 * Time: 6:24 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
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
     * Create an enrollment span from the transaction data
     * @param transactionDto
     * @param account
     * @return
     */
    @Override
    public EnrollmentSpan createEnrollmentSpan(TransactionDto transactionDto,
                                               Account account) {
        // Get the primary member from the transaction dto, need to get the
        // exchange subscriber if from the primary subscriber
        TransactionMemberDto primarySubscriber = getPrimaryMember(transactionDto);
        // Create the enrollment span entity
        EnrollmentSpan enrollmentSpan = EnrollmentSpan.builder()
                .acctEnrollmentSpanSK(null)
                .enrollmentSpanCode(ZeusRandomStringGenerator.randomString(15))
                .account(account)
                .stateTypeCode(transactionDto.getTradingPartnerDto().getStateTypeCode())
                .marketplaceTypeCode(transactionDto.getTradingPartnerDto().getMarketplaceTypeCode())
                .businessUnitTypeCode(transactionDto.getTradingPartnerDto().getBusinessTypeCode())
                .startDate(transactionDto.getTransactionDetail().getEffectiveDate())
                // Determine and populate the end for the enrollment span
                .endDate(determineEndDate(
                        transactionDto.getTransactionDetail().getEffectiveDate(),
                        transactionDto.getTransactionDetail().getEndDate()
                ))
                // get the exchange subscriber id and set it
                .exchangeSubscriberId(getExchangeSubscriberId(primarySubscriber))
                // determine the effectuation date
                .effectuationDate(determineEffectuationDate(transactionDto, null))
                .planId(transactionDto.getTransactionDetail().getPlanId())
                .productTypeCode("HMO")
                .groupPolicyId(transactionDto.getTransactionDetail().getGroupPolicyId())
                .build();
        // Determine the enrollment span status
        String spanStatus = determineEnrollmentSpanStatus(enrollmentSpan, null);
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
            account.getEnrollmentSpan().stream().forEach(enrollmentSpan -> {
                EnrollmentSpanDto enrollmentSpanDto = enrollmentSpanMapper.enrollmentSpanToEnrollmentSpanDto(enrollmentSpan);
                enrollmentSpanDto.setZtcn(ztcn);
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
                enrollmentSpanMapper.enrollmentSpanDtoToEnrollmentSpan(enrollmentSpanStatusDto.getPriorEnrollmentSpan()));
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
    private TransactionMemberDto getPrimaryMember(TransactionDto transactionDto){
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
     * @param priorEnrollmentSpan
     * @return
     */
    private LocalDate determineEffectuationDate(TransactionDto transactionDto,
                                                EnrollmentSpan priorEnrollmentSpan){
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
        return null;
    }

    /**
     * Determine the enrollment span status
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpan
     * @return
     */
    private String determineEnrollmentSpanStatus(EnrollmentSpan currentEnrollmentSpan,
                                                 EnrollmentSpan priorEnrollmentSpan){
        boolean isEndDateGreaterThanStartDate = currentEnrollmentSpan.getEndDate().isAfter(currentEnrollmentSpan.getStartDate());
        boolean isEndDateLessThanStartDate = currentEnrollmentSpan.getEndDate().isBefore(currentEnrollmentSpan.getStartDate());
        boolean isEndDateEqualToStartDate =currentEnrollmentSpan.getEndDate().isEqual(currentEnrollmentSpan.getStartDate());
        if(isDelinquent(currentEnrollmentSpan, priorEnrollmentSpan)){
            return EnrollmentSpanStatus.DELINQUENT.name();
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
     * Determine if the current enrollment span is delinquent
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpan
     * @return
     */
    private boolean isDelinquent(EnrollmentSpan currentEnrollmentSpan,
                                 EnrollmentSpan priorEnrollmentSpan){
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
                boolean localDateIsAfter = LocalDate.now().isBefore(currentEnrollmentSpan.getClaimPaidThroughDate());
                boolean localDateIsEqual = LocalDate.now().isEqual(currentEnrollmentSpan.getClaimPaidThroughDate());
                boolean samePlan = isSamePlan(currentEnrollmentSpan.getPlanId(), priorEnrollmentSpan.getPlanId());
                boolean gapInCoverage = isThereGapInCoverage(currentEnrollmentSpan, priorEnrollmentSpan);
                boolean priorSpanStatus = priorEnrollmentSpan.getStatusTypeCode().equals(EnrollmentSpanStatus.DELINQUENT.name());
                boolean delinquent = localDateIsAfter || localDateIsEqual ||
                        (samePlan &&
                                !gapInCoverage &&
                                priorSpanStatus);
                log.info("Delinquent:{}", delinquent);
                if(delinquent){
                    return true;
                }else{
                    return false;
                }
            }else{
                // If the claim paid through date is null then
                // Check if the plan of the current and the prior enrollment spans are same
                // and check if there is any gap in coverage between the prior and current enrollment spans
                // and if the prior enrollment span is delinquent status
                if(((isSamePlan(currentEnrollmentSpan.getPlanId(), priorEnrollmentSpan.getPlanId()) &&
                        isThereGapInCoverage(currentEnrollmentSpan, priorEnrollmentSpan)) &&
                        priorEnrollmentSpan.getStatusTypeCode().equals(EnrollmentSpanStatus.DELINQUENT))){
                    return true;
                }else{
                    return false;
                }
            }
        }else {
            // if effectuation date is not present or delinquency flag is not set to true or if the start date of the
            // enrollment span is greater than the end date of the enrollment span, then the enrollment span is not
            // delinquent
            return false;
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
     * @param currentEnrollmentSpan
     * @param priorEnrollmentSpan
     * @return
     */
    private boolean isThereGapInCoverage(EnrollmentSpan currentEnrollmentSpan,
                                         EnrollmentSpan priorEnrollmentSpan){
        if(priorEnrollmentSpan.getStatusTypeCode().equals(EnrollmentSpanStatus.CANCELED)){
            return true;
        }else{
            long numOfDays = ChronoUnit.DAYS.between(priorEnrollmentSpan.getEndDate(),
                    currentEnrollmentSpan.getStartDate());
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
}
