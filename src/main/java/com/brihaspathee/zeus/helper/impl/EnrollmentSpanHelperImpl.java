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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        if(currentEnrollmentSpan.getEffectuationDate() != null &&
                !currentEnrollmentSpan.getStartDate()
                        .equals(currentEnrollmentSpan.getEndDate())){
            return EnrollmentSpanStatus.ENROLL.name();
        }
        if(currentEnrollmentSpan.getEffectuationDate() == null &&
                !currentEnrollmentSpan.getStartDate()
                        .equals(currentEnrollmentSpan.getEndDate())){
            return EnrollmentSpanStatus.PREMEM.name();
        }
        return EnrollmentSpanStatus.NO_VALID_STATUS.name();
    }
}
