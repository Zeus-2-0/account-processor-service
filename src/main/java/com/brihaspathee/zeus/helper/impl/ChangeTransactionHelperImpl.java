package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.broker.producer.AccountProcessingValidationProducer;
import com.brihaspathee.zeus.constants.PremiumSpanStatus;
import com.brihaspathee.zeus.constants.ProcessFlowType;
import com.brihaspathee.zeus.constants.RateType;
import com.brihaspathee.zeus.constants.TransactionTypes;
import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import com.brihaspathee.zeus.domain.repository.AccountRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionRateDto;
import com.brihaspathee.zeus.helper.interfaces.ChangeTransactionHelper;
import com.brihaspathee.zeus.helper.interfaces.EnrollmentSpanHelper;
import com.brihaspathee.zeus.helper.interfaces.MemberHelper;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.brihaspathee.zeus.info.PremiumSpanUpdateInfo;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.validator.request.ProcessingValidationRequest;
import com.brihaspathee.zeus.validator.result.ProcessingValidationResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2023
 * Time: 5:33 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChangeTransactionHelperImpl implements ChangeTransactionHelper {

    /**
     * Member helper method to perform tasks that are associated with the member
     */
    private final MemberHelper memberHelper;

    /**
     * Enrollment span helper to perform tasks that are associated with the enrollment span
     */
    private final EnrollmentSpanHelper enrollmentSpanHelper;

    /**
     * Processing validation producer to send the transaction for validation
     */
    private final AccountProcessingValidationProducer accountProcessingValidationProducer;

    /**
     * Instance of the account repository
     */
    private final AccountRepository accountRepository;

    /**
     * Member management service instance to get information from MMS
     */
    private final MemberManagementService memberManagementService;

    /**
     * The spring environment instance
     */
    private final Environment environment;

    /**
     * Update the account based on the transaction details
     * @param accountDto account information that was retrieved from MMS
     * @param account Account that needs to be updated
     * @param transactionDto the dto object that was received for processing the account
     */
    @Override
    public void updateAccount(AccountDto accountDto,
                                 Account account,
                                 TransactionDto transactionDto) throws JsonProcessingException {
        // Get the enrollment span that matches the group policy id received in the transaction
        EnrollmentSpanDto matchedEnrollmentSpan = enrollmentSpanHelper.getMatchedEnrollmentSpan(
                accountDto.getEnrollmentSpans(),
                transactionDto.getTransactionDetail().getGroupPolicyId());
        // Send transaction for validation -- Do not do this when running unit tests
        if(!Arrays.asList(environment.getActiveProfiles()).contains("test")){
            ProcessingRequest processingRequest = account.getProcessRequest();
            ProcessingValidationRequest validationRequest = ProcessingValidationRequest.builder()
                    .processFlowType(ProcessFlowType.CHANGE)
                    .transactionDto(transactionDto)
                    .accountDto(AccountDto.builder()
                            .accountNumber(accountDto.getAccountNumber())
                            .enrollmentSpans(Set.copyOf(Optional.of(
                                            Collections.singletonList(matchedEnrollmentSpan))
                                    .orElse(Collections.emptyList())))
                            .build())
                    .accountSK(account.getAccountSK())
                    .processRequestSK(processingRequest.getProcessRequestSK())
                    .zrcnTypeCode(processingRequest.getZrcnTypeCode())
                    .zrcn(processingRequest.getZrcn())
                    .build();
            accountProcessingValidationProducer.sendAccountProcessingValidationRequest(validationRequest,
                    processingRequest.getRequestPayloadId());
        }else{
            // in a unit test environment continue to process the change from the transaction
            updateChanges(accountDto, account, transactionDto);
        }
    }

    /**
     * Continue to process the transaction once the validations are completed
     * @param processingValidationResult
     */
    @Override
    public Account postValidationProcessing(ProcessingValidationResult processingValidationResult) throws
            JsonProcessingException {
        // todo check if all the rules have passed
        // if all the rules have passed then continue to process the transaction
        ProcessingValidationRequest request = processingValidationResult.getValidationRequest();
        TransactionDto transactionDto = request.getTransactionDto();
        Account account = accountRepository.getReferenceById(request.getAccountSK());
        // get the account dto from member management service
        AccountDto accountDto = memberManagementService.getAccountByAccountNumber(request.getAccountDto().getAccountNumber());
        updateChanges(accountDto, account, transactionDto);
        return account;
    }

    /**
     * This method continues to process the changes received for the account from the transaction
     * @param accountDto
     * @param account
     * @param transactionDto
     */
    private void updateChanges(AccountDto accountDto,
                               Account account,
                               TransactionDto transactionDto){
        // Identify if there are any member level changes (Demographic, Addresses, Communication etc.) to the account
        memberHelper.matchMember(accountDto, transactionDto, account);
        // Identify if the change transaction is a financial or non-financial change
        ChangeTransactionInfo changeTransactionInfo = getChangeTransactionInfo(accountDto, transactionDto);
        // If it is a financial change or CSR Variant change premium span updates are needed for the account
        if(changeTransactionInfo.isPremiumSpanUpdateRequired())
        {
            EnrollmentSpanDto matchedEnrollmentSpan = accountDto.getEnrollmentSpans()
                    .stream()
                    .filter(
                            enrollmentSpanDto ->
                                    enrollmentSpanDto.getEnrollmentSpanSK()
                                            .equals(changeTransactionInfo.getMatchedEnrollmentSpanSK())).findFirst().orElseThrow();
            enrollmentSpanHelper.processFinancialChange(changeTransactionInfo,
                    transactionDto,
                    account,
                    accountDto,
                    matchedEnrollmentSpan);

        }
    }




    /**
     * Determine if the change transaction received is a Financial Change or a Non-financial Change
     * @param accountDto - Account for which change is received
     * @param transactionDto - Data from the transaction
     * @return - True if it is financial change else return false
     */
    private ChangeTransactionInfo getChangeTransactionInfo(AccountDto accountDto, TransactionDto transactionDto){
        ChangeTransactionInfo changeTransactionInfo = ChangeTransactionInfo.builder().build();
        boolean isPremiumSpanUpdateRequired = false;
        // Check if the transaction has any rates
        // If the transaction does not have any rates then the change is non-financial.
        // Set the financial change flag as false and return
        if(transactionDto.getTransactionRates() == null || transactionDto.getTransactionRates().isEmpty()){
            changeTransactionInfo.setPremiumSpanUpdateRequired(isPremiumSpanUpdateRequired);
            return changeTransactionInfo;
        }
        // Check if the transaction contains at least one rate that is of type PREAMTTOT
        List<TransactionRateDto> premiumTotals = transactionDto.getTransactionRates().stream()
                .filter(transactionRateDto -> transactionRateDto.getRateTypeCode().equals("PREAMTTOT")).toList();
        // if there is no rates of type PREAMTTOT then it is not a financial change
        if (premiumTotals.isEmpty()){
            changeTransactionInfo.setPremiumSpanUpdateRequired(isPremiumSpanUpdateRequired);
            return changeTransactionInfo;
        }
        // If there is at least one PREAMTTOT then continue to check if the transaction is financial change or not
        // Get the group policy id from the transaction
        String groupPolicyId = transactionDto.getTransactionDetail().getGroupPolicyId();
        // get the matched enrollment span by the group policy id
        EnrollmentSpanDto matchedEnrollmentSpan = accountDto.getEnrollmentSpans().stream()
                .filter(
                        enrollmentSpanDto ->
                                enrollmentSpanDto.getGroupPolicyId().equals(groupPolicyId))
                .findFirst()
                .orElseThrow();
        changeTransactionInfo.setMatchedEnrollmentSpanSK(matchedEnrollmentSpan.getEnrollmentSpanSK());
        log.info("Premium Amt Total Rate Size:{}", premiumTotals.size());
        if(premiumTotals.size() > 1){
            // The transaction contains multiple premiums
            changeTransactionInfo.setMultipleFinancialsPresent(true);
            List<TransactionRateDto> sortedPremiumTotals = premiumTotals.stream().sorted(
                    Comparator.comparing(TransactionRateDto::getRateStartDate)
            ).toList();
            // Set the rate end dates so that it can be used for comparison with the premium span end dates
            setRateEndDate(sortedPremiumTotals, matchedEnrollmentSpan);
            // Loop through every transaction rate that is received for each of the dates
            // Compare the start date, end date, the amounts and the csr variant with the matching premium span
            // if any of them are different then set the financial change flag as true
            List<PremiumSpanUpdateInfo> premiumSpanUpdateInfos = new ArrayList<>();
            sortedPremiumTotals.forEach(preAmtTotRate -> updatePremiumSpanUpdateInfo(premiumSpanUpdateInfos, preAmtTotRate,
                    transactionDto,
                    matchedEnrollmentSpan));
            log.info("Premium Span Update Infos in Change Transaction Helper:{}", premiumSpanUpdateInfos);
            boolean isPremiumSpanUpdatedRequired = premiumSpanUpdateInfos.stream()
                    .anyMatch(premiumSpanUpdateInfo -> premiumSpanUpdateInfo.getUpdateRequired() == 1 ||
                            premiumSpanUpdateInfo.getUpdateRequired() == 2);
            if (isPremiumSpanUpdatedRequired){
                changeTransactionInfo.setPremiumSpanUpdateRequired(true);
            }
            changeTransactionInfo.setPremiumSpanUpdateInfos(premiumSpanUpdateInfos);
        }else{
            // Create premium span updated info object to store all the updates that needs to be done
            PremiumSpanUpdateInfo premiumSpanUpdateInfo = PremiumSpanUpdateInfo.builder().build();
            TransactionRateDto preAmtTotRateDto = premiumTotals.get(0);
            // Set the change effective date
            LocalDate changeEffectiveDate = preAmtTotRateDto.getRateStartDate();
            String csrVariant = preAmtTotRateDto.getCsrVariant();
            premiumSpanUpdateInfo.setTransactionCSRVariant(csrVariant);
            premiumSpanUpdateInfo.setRateEffectiveDate(changeEffectiveDate);
            // Transaction contains only one pre amt total


            PremiumSpanDto matchedPremiumSpan = retrieveMatchingPremiumSpan(matchedEnrollmentSpan, changeEffectiveDate);
            premiumSpanUpdateInfo.setMatchedPremiumSpanSK(matchedPremiumSpan.getPremiumSpanSK());
            // Set premium spans update required to true if there is a CSR Variant change or if a dep is added/canceled/termed
            changeTransactionInfo.setPremiumSpanUpdateRequired(
                    (!csrVariant.equals(matchedPremiumSpan.getCsrVariant()) ||
                    isDepAddedOrCanceled(transactionDto)));
            // If CSR Variant is not changed then check if the amounts have changed
            if(!changeTransactionInfo.isPremiumSpanUpdateRequired()){
                // Set premium spans update required to true if there is are any amounts changed
                changeTransactionInfo.setPremiumSpanUpdateRequired(isAmountUpdated(transactionDto.getTransactionRates(),
                        matchedPremiumSpan));
            }
            setAmounts(premiumSpanUpdateInfo, transactionDto.getTransactionRates());
            changeTransactionInfo.setPremiumSpanUpdateInfo(premiumSpanUpdateInfo);

        }
        return changeTransactionInfo;
    }

    /**
     * Check if the transaction is updating the rates
     * @param transactionRateDtos - The rate details from the transaction
     * @param matchedPremiumSpanDto - The matched premium span for comparison
     * @return - boolean indicating if the amounts are updated or not
     */
    private boolean isAmountUpdated(List<TransactionRateDto> transactionRateDtos, PremiumSpanDto matchedPremiumSpanDto){
        boolean isAmountUpdated = false;
        boolean isPreAmtUpdated = compareAmount(transactionRateDtos,
                "PREAMTTOT",
                matchedPremiumSpanDto.getTotalPremiumAmount());
        boolean isTotResAmtUpdated = compareAmount(transactionRateDtos,
                "TOTRESAMT",
                matchedPremiumSpanDto.getTotalResponsibleAmount());
        boolean isAPTCAmtUpdated = compareAmount(transactionRateDtos,
                "APTCAMT",
                matchedPremiumSpanDto.getAptcAmount());
        boolean isCSRAmtUpdated = compareAmount(transactionRateDtos,
                "CSRAMT",
                matchedPremiumSpanDto.getCsrAmount());
        boolean isOthPayUpdated = compareAmount(transactionRateDtos,
                "OTHERPAYAMT",
                matchedPremiumSpanDto.getOtherPayAmount());
        isAmountUpdated = isPreAmtUpdated ||
                isTotResAmtUpdated ||
                isAPTCAmtUpdated ||
                isCSRAmtUpdated ||
                isOthPayUpdated;
        return isAmountUpdated;
    }

    /**
     * Compare the amount from transaction to the amount in the premium span
     * @param transactionRateDtos - Transaction rate dtos
     * @param rateTypeCode - Rate type code for comparison
     * @param premiumSpanAmount - The respective premium span amount
     * @return - Return true if it is different else return false
     */
    private boolean compareAmount(List<TransactionRateDto> transactionRateDtos,
                                  String rateTypeCode,
                                  BigDecimal premiumSpanAmount){
        if(!rateTypeCode.equals("OTHERPAYAMT")){
            Optional<TransactionRateDto> optionalTransactionRateDto = transactionRateDtos.stream()
                    .filter(
                            transactionRateDto ->
                                    transactionRateDto.getRateTypeCode()
                                            .equals(rateTypeCode)).findFirst();
            if (optionalTransactionRateDto.isPresent()){
                TransactionRateDto transactionRateDto = optionalTransactionRateDto.get();
                BigDecimal transactionRate = transactionRateDto.getTransactionRate();
//                log.info("Transaction Rate:{}", transactionRate);
//                log.info("Premium Span Amount:{}", premiumSpanAmount);
                int rateComparison = transactionRate.compareTo(premiumSpanAmount);
                return rateComparison != 0;

            }else{
                return false;
            }
        }else{
            List<BigDecimal> otherPayRates = transactionRateDtos.stream()
                    .filter(transactionRateDto ->
                            transactionRateDto.getRateTypeCode().equals(RateType.OTHERPAYAMT1.toString()) ||
                                    transactionRateDto.getRateTypeCode().equals(RateType.OTHERPAYAMT2.toString())).map(
                            TransactionRateDto::getTransactionRate
                    ).toList();
//            log.info("other pay rates:{}", otherPayRates);
            if(!otherPayRates.isEmpty()){
                BigDecimal otherPayAmount = otherPayRates.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                int rateComparison = otherPayAmount.compareTo(premiumSpanAmount);
                return rateComparison != 0;
            }else{
                return false;
            }

        }
    }

    /**
     * Return the premium span that matches the effective date
     * @param matchedEnrollmentSpanDto
     * @param changeEffectiveDate
     * @return
     */
    private PremiumSpanDto retrieveMatchingPremiumSpan(EnrollmentSpanDto matchedEnrollmentSpanDto,
                                                       LocalDate changeEffectiveDate){
        PremiumSpanDto matchedPremiumSpan = matchedEnrollmentSpanDto.getPremiumSpans()
                .stream()
                .filter(premiumSpan -> {
                    LocalDate premiumSpanStartDate = premiumSpan.getStartDate();
                    LocalDate premiumSpanEndDate = premiumSpan.getEndDate();
                    String premiumSpanStatus = premiumSpan.getStatusTypeCode();
                    return (changeEffectiveDate.isEqual(premiumSpanStartDate) ||
                            changeEffectiveDate.isEqual(premiumSpanEndDate) ||
                            (changeEffectiveDate.isAfter(premiumSpanStartDate) &&
                                    changeEffectiveDate.isBefore(premiumSpanEndDate))) &&
                            premiumSpanStatus.equals(PremiumSpanStatus.ACTIVE.toString());
                }).findFirst()
                .orElseThrow();
        return matchedPremiumSpan;
    }

    /**
     * This method evaluated the rates and the respective dates received in the transaction
     * with the respective matching premium span and for each of the premium span rates
     * identifies if there are any changes to be made
     * @param premiumSpanUpdateInfos - List of premium span update info object that will be populated
     * @param preAmtTotRateDto - The premium amt tot rate that will have the start date, end date and
     *                         csr variant that needs to be used for the respective premium span
     * @param transactionDto - The transaction dto
     * @param matchedEnrollmentSpan - the matched enrollment span
     */
    private void updatePremiumSpanUpdateInfo(List<PremiumSpanUpdateInfo> premiumSpanUpdateInfos,
                                      TransactionRateDto preAmtTotRateDto,
                                      TransactionDto transactionDto,
                                      EnrollmentSpanDto matchedEnrollmentSpan){
//        log.info("Premium Span Updated Info - Update Premium Span Method:{}", premiumSpanUpdateInfos);
        List<TransactionRateDto> transactionRates = transactionDto.getTransactionRates();
        PremiumSpanUpdateInfo premiumSpanUpdateInfo = PremiumSpanUpdateInfo.builder()
                .build();
        LocalDate effectiveDate = preAmtTotRateDto.getRateStartDate();
        LocalDate rateEndDate = preAmtTotRateDto.getRateEndDate();
        premiumSpanUpdateInfo.setRateEffectiveDate(effectiveDate);
        premiumSpanUpdateInfo.setRateEndDate(rateEndDate);
        String csrVariant = preAmtTotRateDto.getCsrVariant();
        premiumSpanUpdateInfo.setTransactionCSRVariant(csrVariant);
        // Get all the rates that matches the effective date
        List<TransactionRateDto> transactionRateDtos = transactionRates
                .stream()
                .filter(
                        transactionRateDto ->
                                transactionRateDto.getRateStartDate()
                                        .equals(effectiveDate)).toList();
        PremiumSpanDto matchedPremiumSpan = retrieveMatchingPremiumSpan(matchedEnrollmentSpan, effectiveDate);
        boolean matchedPremiumSpanExist = premiumSpanUpdateInfos.stream()
                .anyMatch(premSpanUpdInfo ->
                        premSpanUpdInfo.getMatchedPremiumSpanSK()
                                .equals(matchedPremiumSpan.getPremiumSpanSK()));
//        log.info("Matched Premium Span Exist:{}", matchedPremiumSpanExist);
        premiumSpanUpdateInfo.setMatchedPremiumSpanSK(matchedPremiumSpan.getPremiumSpanSK());
        if(!effectiveDate.equals(matchedPremiumSpan.getStartDate())){
            // Dates are not equal, so the matching premium span has to be canceled and a new one should be created
            premiumSpanUpdateInfo.setUpdateRequired(1);
            premiumSpanUpdateInfo.setCreateNewPremiumSpan(true);
            setAmounts(premiumSpanUpdateInfo, transactionRateDtos);

        }else{
            // start date is equal
            // Check if the amounts are updated
            boolean isAmountUpdated = isAmountUpdated(transactionRateDtos, matchedPremiumSpan);
            // Check if the CSR Variant is updated
            boolean isCSRVariantUpdated = !csrVariant.equals(matchedPremiumSpan.getCsrVariant());
            boolean isDepAddedOrCanceledOrTermed = isDepAddedOrCanceled(transactionDto, effectiveDate, rateEndDate);
            // Check if the end date is different
            boolean isEndDateDifferent = !rateEndDate.isEqual(matchedPremiumSpan.getEndDate());
            if (isAmountUpdated || isCSRVariantUpdated || isDepAddedOrCanceledOrTermed){
                // If either the amounts or the CSR Variant is updated
                // The matching premium span has to be canceled and new one should be created
                premiumSpanUpdateInfo.setUpdateRequired(1);
                premiumSpanUpdateInfo.setCreateNewPremiumSpan(true);
                setAmounts(premiumSpanUpdateInfo, transactionRateDtos);
            }else if(isEndDateDifferent){
                // The amounts and the csr variant are same, but the end date is different
                if (rateEndDate.isAfter(matchedPremiumSpan.getEndDate())){
                    // cancel the matched premium span
                    premiumSpanUpdateInfo.setUpdateRequired(1);
                    premiumSpanUpdateInfo.setCreateNewPremiumSpan(true);
                    setAmounts(premiumSpanUpdateInfo, transactionRateDtos);
                }else{
                    // the rate end date is less than the matched premium span
                    // end date the matched premium span with date as that of
                    // the rate end date
                    premiumSpanUpdateInfo.setUpdateRequired(2);
                    premiumSpanUpdateInfo.setCreateNewPremiumSpan(false);
                }
            }else {
                premiumSpanUpdateInfo.setUpdateRequired(0);
                premiumSpanUpdateInfo.setCreateNewPremiumSpan(false);
            }
        }
        if(matchedPremiumSpanExist){
            // If the matched premium span already exist in the premium span update info list
            // then overwrite the updated required with 3
            premiumSpanUpdateInfo.setUpdateRequired(3);
        }
        premiumSpanUpdateInfos.add(premiumSpanUpdateInfo);
    }

    /**
     * This method loops through all the available rates for different dates and
     * sets the end date so that it can be used as the end date of the premium span that will be
     * created
     * @param transactionRateDtos
     * @param matchingEnrollmentSpanDto
     */
    private void setRateEndDate(List<TransactionRateDto> transactionRateDtos, EnrollmentSpanDto matchingEnrollmentSpanDto){
        Iterator<TransactionRateDto> iterator = transactionRateDtos.iterator();
        int listSize = transactionRateDtos.size();
        int i = 1;
        while (iterator.hasNext()){
            TransactionRateDto rateDto = iterator.next();
            if(i < listSize){
                TransactionRateDto nextRate = transactionRateDtos.get(i);
                rateDto.setRateEndDate(nextRate.getRateStartDate().minusDays(1));
                i = i + 1;
            }else{
                rateDto.setRateEndDate(matchingEnrollmentSpanDto.getEndDate());
            }
        }
    }

    /**
     * Set the premium amounts
     * @param premiumSpanUpdateInfo
     * @param transactionRateDtos
     */
    private void setAmounts(PremiumSpanUpdateInfo premiumSpanUpdateInfo, List<TransactionRateDto> transactionRateDtos){
        transactionRateDtos.forEach(transactionRateDto -> {

            String rateTypeCode = transactionRateDto.getRateTypeCode();
            switch (rateTypeCode) {
                case "TOTRESAMT" -> premiumSpanUpdateInfo.setTotResAmt(transactionRateDto.getTransactionRate());
                case "PREAMTTOT" -> premiumSpanUpdateInfo.setPreAmtTot(transactionRateDto.getTransactionRate());
                case "APTCAMT" -> premiumSpanUpdateInfo.setAptcAmt(transactionRateDto.getTransactionRate());
                case "CSRAMT" -> premiumSpanUpdateInfo.setCsrAmt(transactionRateDto.getTransactionRate());
                case "OTHERPAYAMT1", "OTHERPAYAMT2" -> setOtherPayAmount(premiumSpanUpdateInfo,
                        transactionRateDto.getTransactionRate());
            }
        });
    }

    /**
     * Sum the two other pay amounts and set it
     * @param premiumSpanUpdateInfo
     * @param otherPayAmount
     */
    private void setOtherPayAmount(PremiumSpanUpdateInfo premiumSpanUpdateInfo, BigDecimal otherPayAmount){
        if(premiumSpanUpdateInfo.getOtherPayAmt() == null){
            premiumSpanUpdateInfo.setOtherPayAmt(otherPayAmount);
        }else {
            premiumSpanUpdateInfo.setOtherPayAmt(
                    premiumSpanUpdateInfo.getOtherPayAmt().add(otherPayAmount));
        }
    }

    /**
     * Check if a dependent is being added or canceled
     * @param transactionDto
     * @return
     */
    private boolean isDepAddedOrCanceled(TransactionDto transactionDto){
        return transactionDto.getMembers().stream().anyMatch(transactionMemberDto ->
                transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.CANCELORTERM.toString()) ||
//                transactionMemberDto.getTransactionTypeCode().equals("TERM") ||
                transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.ADD.toString()));
    }

    private boolean isDepAddedOrCanceled(TransactionDto transactionDto, LocalDate startDate, LocalDate endDate){
        boolean isMemberAddedCanceledOrTermed = transactionDto.getMembers().stream().anyMatch(transactionMemberDto ->
                transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.CANCELORTERM.toString()) ||
//                        transactionMemberDto.getTransactionTypeCode().equals("TERM") ||
                        transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.ADD.toString()));
        if(!isMemberAddedCanceledOrTermed){
            return false;
        }else{
            List<TransactionMemberDto> memberDtos = transactionDto.getMembers().stream().filter(transactionMemberDto ->
                    transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.CANCELORTERM.toString()) ||
//                            transactionMemberDto.getTransactionTypeCode().equals("TERM") ||
                            transactionMemberDto.getTransactionTypeCode().equals(TransactionTypes.ADD.toString())).toList();
            // retrieve all members who are being added or termed or canceled
            return memberDtos.stream().anyMatch(transactionMemberDto -> {
               String transactionTypeCode = transactionMemberDto.getTransactionTypeCode();
                LocalDate effectiveDate = transactionMemberDto.getEffectiveDate();
               if(transactionTypeCode.equals(TransactionTypes.ADD.toString())){
                   return effectiveDate.isBefore(startDate) ||
                           effectiveDate.isEqual(startDate) ||
                           effectiveDate.isEqual(endDate) ||
                           (effectiveDate.isAfter(startDate) && effectiveDate.isBefore(endDate));
               }else{
                   return effectiveDate.isBefore(startDate) ||
                           effectiveDate.isEqual(startDate);
               }
            });
        }
    }
}
