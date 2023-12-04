package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.constants.PremiumSpanStatus;
import com.brihaspathee.zeus.domain.entity.*;
import com.brihaspathee.zeus.domain.repository.PremiumSpanRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionRateDto;
import com.brihaspathee.zeus.helper.interfaces.MemberPremiumHelper;
import com.brihaspathee.zeus.helper.interfaces.PremiumSpanHelper;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;
import com.brihaspathee.zeus.info.PremiumSpanUpdateInfo;
import com.brihaspathee.zeus.mapper.interfaces.PremiumSpanMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 25, November 2022
 * Time: 6:40 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PremiumSpanHelperImpl implements PremiumSpanHelper {

    /**
     * Premium span mapper instance
     */
    private final PremiumSpanMapper premiumSpanMapper;

    /**
     * Repository instance to perform CRUD operations
     */
    private final PremiumSpanRepository premiumSpanRepository;

    /**
     * Member premium helper instance to perform operations
     */
    private final MemberPremiumHelper memberPremiumHelper;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create the premium spans for the enrollment span
     * @param transactionDto
     * @param enrollmentSpan
     * @return
     */
    @Override
    public List<PremiumSpan> createPremiumSpans(TransactionDto transactionDto,
                                                EnrollmentSpan enrollmentSpan,
                                                Account account) {
        // Create the premium spans based on the information that is available in the PREAMTTOT rate
        // If there are more than one PREAMTTOT rates then as many premium spans will be created
        List<PremiumSpan> premiumSpans = createPremiumSpans(transactionDto, enrollmentSpan);
        // Once the premium spans are created, the premium amounts will be populated for each of the premium spans from
        // the transaction. This will complete the creation of all the premium spans
        populatePremiumAmounts(premiumSpans, transactionDto.getTransactionRates());
        log.info("Premium Spans:{}",premiumSpans);
        // Save the premium spans and associate them to the members
        premiumSpans.stream().forEach(premiumSpan -> {
            premiumSpanRepository.save(premiumSpan);
            memberPremiumHelper.createMemberPremiums(transactionDto.getMembers(),
                    premiumSpan,
                    account.getMembers(),
                    transactionDto.getTransactionDetail().getCoverageTypeCode());
        });
        // Associate the premium spans with the enrollment span
        enrollmentSpan.setPremiumSpans(premiumSpans);
        return premiumSpans;
    }

    /**
     * Set the premium span in DTO to send to MMS
     * @param enrollmentSpanDto
     * @param enrollmentSpan
     */
    @Override
    public void setPremiumSpan(EnrollmentSpanDto enrollmentSpanDto,
                               EnrollmentSpan enrollmentSpan,
                               String ztcn) {
        if(enrollmentSpan.getPremiumSpans() !=null && enrollmentSpan.getPremiumSpans().size() > 0){
            List<PremiumSpanDto> premiumSpanDtos = new ArrayList<>();
            enrollmentSpan.getPremiumSpans().stream().forEach(premiumSpan -> {
                PremiumSpanDto premiumSpanDto = premiumSpanMapper.premiumSpanToPremiumSpanDto(premiumSpan);
//                premiumSpanDto.setZtcn(ztcn);
                memberPremiumHelper.setMemberPremiums(premiumSpanDto, premiumSpan);
                premiumSpanDtos.add(premiumSpanDto);
            });
            enrollmentSpanDto.setPremiumSpans(premiumSpanDtos.stream().collect(Collectors.toSet()));
        }
    }

    /**
     * Save the updated premium spans
     * @param premiumSpanDtos premium spans that need to be saved
     * @param enrollmentSpan the enrollment span that the premium span belongs
     * @return return the saved premium spans
     */
    @Override
    public List<PremiumSpan> saveUpdatedPremiumSpans(List<PremiumSpanDto> premiumSpanDtos,
                                                     EnrollmentSpan enrollmentSpan) {
        if(premiumSpanDtos != null && !premiumSpanDtos.isEmpty()){
            List<PremiumSpan> savedPremiumSpans = new ArrayList<>();
            premiumSpanDtos.forEach(premiumSpanDto -> {
                PremiumSpan premiumSpan =
                        premiumSpanMapper.premiumSpanDtoToPremiumSpan(premiumSpanDto);
                premiumSpan.setAcctPremiumSpanSK(premiumSpanDto.getPremiumSpanSK());
                premiumSpan.setChanged(true);
                premiumSpan.setEnrollmentSpan(enrollmentSpan);
                premiumSpan = premiumSpanRepository.save(premiumSpan);
                savedPremiumSpans.add(premiumSpan);

            });
            return savedPremiumSpans;
        }

        return null;
    }

    /**
     * Process a financial change for a transaction
     * @param changeTransactionInfo - Contains details about the change transaction
     * @param transactionDto - Change transaction as received
     * @param account - account entity that is created
     * @param enrollmentSpan - enrollmentSpan entity that is created
     * @param matchedEnrollmentSpanDto - The enrollment span that was matched for the change transaction
     */
    @Override
    public void processFinancialChange(ChangeTransactionInfo changeTransactionInfo,
                                       TransactionDto transactionDto,
                                       Account account,
                                       AccountDto accountDto,
                                       EnrollmentSpan enrollmentSpan,
                                       EnrollmentSpanDto matchedEnrollmentSpanDto) {
        log.info("Multiple Financials Present:{}", changeTransactionInfo.isMultipleFinancialsPresent());
        if(changeTransactionInfo.isMultipleFinancialsPresent()){
            processMultipleFinancialChange(changeTransactionInfo.getPremiumSpanUpdateInfos(),
                    transactionDto,
                    account,
                    accountDto,
                    enrollmentSpan,
                    matchedEnrollmentSpanDto);
        }else{
            processSingleFinancialChange(changeTransactionInfo.getPremiumSpanUpdateInfo(),
                    transactionDto,
                    account,
                    accountDto,
                    enrollmentSpan,
                    matchedEnrollmentSpanDto);
        }

    }

    /**
     * Populate the premium amounts for each of the premium spans
     * @param premiumSpans
     * @param transactionRateDtos
     */
    private void populatePremiumAmounts(List<PremiumSpan> premiumSpans,
                                        List<TransactionRateDto> transactionRateDtos){
        premiumSpans.forEach(premiumSpan -> {
            LocalDate premiumStartDate = premiumSpan.getStartDate();
            List<TransactionRateDto> matchedRates = transactionRateDtos.stream()
                    .filter(
                            rateDto ->
                                    rateDto.getRateStartDate().equals(premiumStartDate))
                    .collect(Collectors.toList());
            matchedRates.forEach(rateDto -> {
                setPolicyAmount(premiumSpan, matchedRates, rateDto.getRateTypeCode());
            });
        });
    }

    /**
     * Set the respective policy amount if present in the transaction
     * @param premiumSpan
     * @param matchedRates
     * @param rateTypeCode
     */
    private void setPolicyAmount(PremiumSpan premiumSpan, List<TransactionRateDto> matchedRates, String rateTypeCode){
        Optional<TransactionRateDto> policyAmt = matchedRates.stream()
                .filter(
                        rateDto -> rateDto.getRateTypeCode().equals(rateTypeCode))
                .findFirst();
        if(policyAmt.isPresent()){
            String rateType = policyAmt.get().getRateTypeCode();
            if(rateType.equals("TOTRESAMT")){
                premiumSpan.setTotalResponsibleAmount(policyAmt.get().getTransactionRate());
            }else if (rateType.equals("PREAMTTOT")){
                premiumSpan.setTotalPremAmount(policyAmt.get().getTransactionRate());
            } else if(rateType.equals("APTCAMT")){
                premiumSpan.setAptcAmount(policyAmt.get().getTransactionRate());
            }else if(rateType.equals("CSRAMT")){
                premiumSpan.setCsrAmount(policyAmt.get().getTransactionRate());
            }else if(rateType.equals("OTHERPAYAMT1") || rateType.equals("OTHERPAYAMT2")){
                if(premiumSpan.getOtherPayAmount() == null){
                    premiumSpan.setOtherPayAmount(policyAmt.get().getTransactionRate());
                }else {
                    premiumSpan.setOtherPayAmount(
                            premiumSpan.getOtherPayAmount().add(policyAmt.get().getTransactionRate()));
                }
            }
        }
    }

    /**
     * Sort the rates received by the ascending order of the rate start date
     * @param transactionRateDtos
     * @return
     */
    private List<TransactionRateDto> sortPremiumDates(List<TransactionRateDto> transactionRateDtos){
        // Get all the rate dto objects that have PREAMTTOT as the rate type code and sort it in the
        // ascending order of the start date
        List<TransactionRateDto> premiumRates =
                transactionRateDtos.stream()
                        .filter(rateDto -> rateDto.getRateTypeCode().equals("PREAMTTOT"))
                        .collect(Collectors.toList());
        premiumRates.sort(Comparator.comparing(TransactionRateDto::getRateStartDate));
        return premiumRates;
    }

    /**
     * Create the premium spans with only the start and end dates
     * @param transactionDto
     * @param enrollmentSpan
     * @return
     */
    private List<PremiumSpan> createPremiumSpans(TransactionDto transactionDto, EnrollmentSpan enrollmentSpan){
        List<TransactionRateDto> premiumRates = sortPremiumDates(transactionDto.getTransactionRates());
        log.info("Premium Rates:{}", premiumRates);
        List<PremiumSpan> premiumSpans = new ArrayList<>();
        AtomicInteger premiumSpanSequence = new AtomicInteger(0);
        premiumRates.forEach(rateDto -> {
            String premiumSpanCode = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                    "premiumSpanCode");
            premiumSpanSequence.set(premiumSpanSequence.get() + 1);
            PremiumSpan premiumSpan = PremiumSpan.builder()
                    .premiumSpanCode(premiumSpanCode)
                    .ztcn(transactionDto.getZtcn())
                    .enrollmentSpan(enrollmentSpan)
                    .startDate(rateDto.getRateStartDate())
                    .statusTypeCode("ACTIVE")
                    .csrVariant(rateDto.getCsrVariant())
                    .acctPremiumSpanSK(null)
//                    .totalPremAmount(rateDto.getTransactionRate())
                    .sequence(premiumSpanSequence.get())
                    .changed(true)
                    .build();
            LocalDate endDate = rateDto.getRateEndDate();
            if(endDate != null){
                premiumSpan.setEndDate(endDate);
            }else{
                int year = rateDto.getRateStartDate().getYear();
                premiumSpan.setEndDate(LocalDate.of(year, 12, 31));
            }
            if(!premiumSpans.isEmpty()){
                Optional<PremiumSpan> optionalPremiumSpan = premiumSpans.stream().filter(premiumSpan1 -> {
                    boolean b = premiumSpan1.getStartDate().isBefore(premiumSpan.getStartDate()) &&
                            premiumSpan1.getEndDate().isAfter(premiumSpan.getStartDate());
                    return b;

                }).findFirst();
                if(optionalPremiumSpan.isPresent()){
                    PremiumSpan priorPremiumSpan = optionalPremiumSpan.get();
                    priorPremiumSpan.setEndDate(premiumSpan.getStartDate().minusDays(1));
                }
            }
            premiumSpans.add(premiumSpan);
        });
        return premiumSpans;
    }

    private void processSingleFinancialChange(PremiumSpanUpdateInfo premiumSpanUpdateInfo,
                                              TransactionDto transactionDto,
                                              Account account,
                                              AccountDto accountDto,
                                              EnrollmentSpan enrollmentSpan,
                                              EnrollmentSpanDto matchedEnrollmentSpanDto){
        List<PremiumSpan> updatedPremiumSpans = new ArrayList<>();
        UUID matchedPremiumSpanSK = premiumSpanUpdateInfo.getMatchedPremiumSpanSK();
        // This is done to determine the sequence for the premium span that is to be created
        int premiumSpanSize = matchedEnrollmentSpanDto.getPremiumSpans().size();
        // Create the new premium span
        PremiumSpan newPremiumSpan = PremiumSpan.builder()
                .premiumSpanCode(accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(), "premiumSpanCode"))
                .sequence(premiumSpanSize + 1)
                .changed(true)
                .ztcn(transactionDto.getZtcn())
                .statusTypeCode(String.valueOf(PremiumSpanStatus.ACTIVE))
                .enrollmentSpan(enrollmentSpan)
                .startDate(premiumSpanUpdateInfo.getRateEffectiveDate())
                .endDate(matchedEnrollmentSpanDto.getEndDate())
                .csrVariant(premiumSpanUpdateInfo.getTransactionCSRVariant())
                .totalPremAmount(premiumSpanUpdateInfo.getPreAmtTot())
                .totalResponsibleAmount(premiumSpanUpdateInfo.getTotResAmt())
                .aptcAmount(premiumSpanUpdateInfo.getAptcAmt())
                .csrAmount(premiumSpanUpdateInfo.getCsrAmt())
                .otherPayAmount(premiumSpanUpdateInfo.getOtherPayAmt())
                .build();
//            populatePremiumAmounts(List.of(newPremiumSpan), transactionDto.getTransactionRates());
        log.info("New Premium Span:{}", newPremiumSpan);
        // Save the premium span to the repository
        newPremiumSpan = premiumSpanRepository.save(newPremiumSpan);
        // determine the members to be added to the premium span
        PremiumSpanDto matchedPremiumSpanDto = matchedEnrollmentSpanDto.getPremiumSpans()
                .stream()
                .filter(
                        premiumSpanDto ->
                                premiumSpanDto.getPremiumSpanSK()
                                        .equals(premiumSpanUpdateInfo.getMatchedPremiumSpanSK()))
                .findFirst()
                .orElseThrow();
        // set members on the new premium span
        memberPremiumHelper.createMemberPremiums(matchedPremiumSpanDto, account, accountDto, transactionDto.getMembers(), newPremiumSpan);
        updatedPremiumSpans.add(newPremiumSpan);
        matchedEnrollmentSpanDto.getPremiumSpans().forEach(premiumSpanDto -> {
            // Check if the premium span is the matched premium span
            if (premiumSpanDto.getPremiumSpanSK().equals(matchedPremiumSpanSK)){
                // If the premium span is the matched premium span
                if(premiumSpanDto.getStartDate().isEqual(premiumSpanUpdateInfo.getRateEffectiveDate())){
                    // If the matched premium span start date is equal to the change effective date
                    // Then - Set the end date to be equal to the start date
                    // Set the status to CANCEL
                    // Set the changed flag to TRUE
                    premiumSpanDto.setEndDate(premiumSpanDto.getStartDate());
                    premiumSpanDto.setChanged(new AtomicBoolean(true));
                    premiumSpanDto.setStatusTypeCode(String.valueOf(PremiumSpanStatus.CANCEL));
                }else{
                    // If the matched premium span start date is not equal to the change effective date
                    // Then - Set the end date to be one day prior to the change effective date
                    // Set the changed flag to TRUE
                    premiumSpanDto.setEndDate(premiumSpanUpdateInfo.getRateEffectiveDate().minusDays(1));
                    premiumSpanDto.setChanged(new AtomicBoolean(true));
                }
                // if the premium span is not the matched premium span
            }else if (premiumSpanDto.getStartDate().isBefore(premiumSpanUpdateInfo.getRateEffectiveDate())){
                // If the premium span start date is less than the change effective date
                // No update should be made to the premium span
                // Set the changed flag to false
                premiumSpanDto.setChanged(new AtomicBoolean(false));

            }
            // If the premium span is not the matched premium span and the premium span start date is not before the
            // change effective date then follow the below steps
            else {
                // Set the end date to be equal to the start date
                // Set the status to CANCEL
                // Set the changed flag to TRUE
                premiumSpanDto.setEndDate(premiumSpanDto.getStartDate());
                premiumSpanDto.setChanged(new AtomicBoolean(true));
                premiumSpanDto.setStatusTypeCode(String.valueOf(PremiumSpanStatus.CANCEL));
            }
            PremiumSpan premiumSpan = premiumSpanMapper.premiumSpanDtoToPremiumSpan(premiumSpanDto);
            premiumSpan.setEnrollmentSpan(enrollmentSpan);
            // Save the premium spans to the repository
            premiumSpan = premiumSpanRepository.save(premiumSpan);
            updatedPremiumSpans.add(premiumSpan);
        });
        enrollmentSpan.setPremiumSpans(updatedPremiumSpans);

    }
    /**
     * Process transaction where multiple financial changes were received
     * in the transaction
     * @param premiumSpanUpdateInfos
     * @param transactionDto
     * @param account
     * @param accountDto
     * @param enrollmentSpan
     * @param matchedEnrollmentSpanDto
     */
    private void processMultipleFinancialChange(List<PremiumSpanUpdateInfo> premiumSpanUpdateInfos,
                                                TransactionDto transactionDto,
                                                Account account,
                                                AccountDto accountDto,
                                                EnrollmentSpan enrollmentSpan,
                                                EnrollmentSpanDto matchedEnrollmentSpanDto) {
        log.info("PremiumSpanUpdateInfos:{}", premiumSpanUpdateInfos);
        log.info("PremiumSpanUpdateInfos Size:{}", premiumSpanUpdateInfos.size());
        List<PremiumSpan> updatedPremiumSpans = new ArrayList<>();
        AtomicInteger loopCount = new AtomicInteger(1);
        AtomicInteger updatedPremiumSpanSize = new AtomicInteger(matchedEnrollmentSpanDto.getPremiumSpans().size()+1);
        premiumSpanUpdateInfos.forEach(premiumSpanUpdateInfo -> {
            log.info("Loop Count:{}", loopCount.getAndIncrement());
            log.info("Premium Span Updated Info that is used in the loop:{}", premiumSpanUpdateInfo);
            PremiumSpanDto matchedPremiumSpan = matchedEnrollmentSpanDto.getPremiumSpans()
                    .stream()
                    .filter(premiumSpanDto ->
                            premiumSpanDto.getPremiumSpanSK()
                                    .equals(premiumSpanUpdateInfo.getMatchedPremiumSpanSK()))
                    .findFirst()
                    .orElseThrow();
            if (premiumSpanUpdateInfo.isCreateNewPremiumSpan()){
                log.info("Inside to create a new premium span");
                // Create the new premium span
                PremiumSpan newPremiumSpan = PremiumSpan.builder()
                        .premiumSpanCode(accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(), "premiumSpanCode"))
                        .sequence(updatedPremiumSpanSize.getAndIncrement())
                        .changed(true)
                        .ztcn(transactionDto.getZtcn())
                        .statusTypeCode(String.valueOf(PremiumSpanStatus.ACTIVE))
                        .enrollmentSpan(enrollmentSpan)
                        .startDate(premiumSpanUpdateInfo.getRateEffectiveDate())
                        .endDate(premiumSpanUpdateInfo.getRateEndDate())
                        .csrVariant(premiumSpanUpdateInfo.getTransactionCSRVariant())
                        .totalPremAmount(premiumSpanUpdateInfo.getPreAmtTot())
                        .totalResponsibleAmount(premiumSpanUpdateInfo.getTotResAmt())
                        .aptcAmount(premiumSpanUpdateInfo.getAptcAmt())
                        .csrAmount(premiumSpanUpdateInfo.getCsrAmt())
                        .otherPayAmount(premiumSpanUpdateInfo.getOtherPayAmt())
                        .build();
                // Save the premium span to the repository
                newPremiumSpan = premiumSpanRepository.save(newPremiumSpan);
                memberPremiumHelper.createMemberPremiums(matchedPremiumSpan,
                        account,
                        accountDto,
                        transactionDto.getMembers(),
                        newPremiumSpan);
                updatedPremiumSpans.add(newPremiumSpan);

            }
            // Create the matched premium span only if the update required is not equal to 3
            // If it is equal to 3, that means it has already been updated
            if(premiumSpanUpdateInfo.getUpdateRequired() !=3){
                updatedPremiumSpans.add(updateMatchingSpan(premiumSpanUpdateInfo,
                        matchedPremiumSpan, enrollmentSpan));
            }
//            else {
//                switch (premiumSpanUpdateInfo.getUpdateRequired()) {
//                    case 0 -> {
//                        // No update is required for this premium span
//                        matchedPremiumSpan.setChanged(new AtomicBoolean(false));
//                    }
//                    case 1 -> {
//                        // cancel the matched premium span
//                        // Set the end date to be equal to the start date
//                        // Set the status to CANCEL
//                        // Set the changed flag to TRUE
//                        matchedPremiumSpan.setEndDate(matchedPremiumSpan.getStartDate());
//                        matchedPremiumSpan.setChanged(new AtomicBoolean(true));
//                        matchedPremiumSpan.setStatusTypeCode(String.valueOf(PremiumSpanStatus.CANCEL));
//                    }
//                    case 2 -> {
//                        // term the matched premium span
//                        // Set the end date to be equal to the end date that was received for the rate in the transaction
//                        // Set the changed flag to TRUE
//                        matchedPremiumSpan.setEndDate(premiumSpanUpdateInfo.getRateEndDate());
//                        matchedPremiumSpan.setChanged(new AtomicBoolean(true));
//                    }
//                }
//                PremiumSpan premiumSpan = premiumSpanMapper.premiumSpanDtoToPremiumSpan(matchedPremiumSpan);
//                premiumSpan.setEnrollmentSpan(enrollmentSpan);
//                // Save the premium spans to the repository
//                premiumSpan = premiumSpanRepository.save(premiumSpan);
//                updatedPremiumSpans.add(premiumSpan);
//            }
            log.info("Update Premium Spans Size: {}", updatedPremiumSpans.size());
            log.info("Update Premium Spans: {}", updatedPremiumSpans);
        });
        // check if there are any premium spans with start date that is greater than the greatest rate start date that
        // was received in the transaction, if there are then cancel those premium spans
        // E.g. In transaction rates were received for 1/1 and 4/1 start dates
        // And enrollment span has premium spans from 1/1 - 2/28, 3/1 - 4/30 and 5/1 - 12/31
        // In this case:
        //          1/1 - 2/28 premium span will be canceled and replaced with premium span 1/1 - 3/31
        //          3/1 - 4/30 premium span will be canceled and replaced with premium span 4/1 - 12/31
        //          5/1 - 12/31 premium span needs to be canceled so that it is not overlapping with the new 4/1 span
        LocalDate maxRateEffectiveDate = premiumSpanUpdateInfos.stream()
                .map(PremiumSpanUpdateInfo::getRateEffectiveDate).max(LocalDate::compareTo).get();
        List<PremiumSpanDto> extraPremiumSpans = matchedEnrollmentSpanDto.getPremiumSpans()
                .stream()
                .filter(
                        premiumSpanDto -> premiumSpanDto.getStartDate().isAfter(maxRateEffectiveDate) &&
                                premiumSpanDto.getStatusTypeCode()
                                        .equals(PremiumSpanStatus.ACTIVE.toString())).toList();
        log.info("Is Extra Premium Spans Empty:{}", extraPremiumSpans.isEmpty());
        if(!extraPremiumSpans.isEmpty()){
            extraPremiumSpans.forEach(premiumSpanDto -> {
                // cancel the extra premium span
                // Set the end date to be equal to the start date
                // Set the status to CANCEL
                // Set the changed flag to TRUE
                premiumSpanDto.setEndDate(premiumSpanDto.getStartDate());
                premiumSpanDto.setChanged(new AtomicBoolean(true));
                premiumSpanDto.setStatusTypeCode(String.valueOf(PremiumSpanStatus.CANCEL));
                PremiumSpan premiumSpan = premiumSpanMapper.premiumSpanDtoToPremiumSpan(premiumSpanDto);
                premiumSpan.setEnrollmentSpan(enrollmentSpan);
                // Save the premium spans to the repository
                premiumSpan = premiumSpanRepository.save(premiumSpan);
                updatedPremiumSpans.add(premiumSpan);
            });
        }
        log.info("Updated Premium Spans:{}", updatedPremiumSpans);
        log.info("Updated Premium Spans Size:{}", updatedPremiumSpans.size());
        enrollmentSpan.setPremiumSpans(updatedPremiumSpans);
    }

    private PremiumSpan updateMatchingSpan(PremiumSpanUpdateInfo premiumSpanUpdateInfo,
                                            PremiumSpanDto matchedPremiumSpan,
                                           EnrollmentSpan enrollmentSpan){
        log.info("Premium Span Updated Info in matching span: {}", premiumSpanUpdateInfo);
        switch (premiumSpanUpdateInfo.getUpdateRequired()) {
            case 0 -> {
                // No update is required for this premium span
                matchedPremiumSpan.setChanged(new AtomicBoolean(false));
            }
            case 1 -> {
                // cancel the matched premium span
                // Set the end date to be equal to the start date
                // Set the status to CANCEL
                // Set the changed flag to TRUE
                matchedPremiumSpan.setEndDate(matchedPremiumSpan.getStartDate());
                matchedPremiumSpan.setChanged(new AtomicBoolean(true));
                matchedPremiumSpan.setStatusTypeCode(String.valueOf(PremiumSpanStatus.CANCEL));
            }
            case 2 -> {
                // term the matched premium span
                // Set the end date to be equal to the end date that was received for the rate in the transaction
                // Set the changed flag to TRUE
                matchedPremiumSpan.setEndDate(premiumSpanUpdateInfo.getRateEndDate());
                matchedPremiumSpan.setChanged(new AtomicBoolean(true));
            }
        }
        PremiumSpan premiumSpan = premiumSpanMapper.premiumSpanDtoToPremiumSpan(matchedPremiumSpan);
        premiumSpan.setEnrollmentSpan(enrollmentSpan);
        // Save the premium spans to the repository
        premiumSpan = premiumSpanRepository.save(premiumSpan);
        return premiumSpan;

    }
}
