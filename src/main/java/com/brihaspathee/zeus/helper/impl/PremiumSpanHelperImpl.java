package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.*;
import com.brihaspathee.zeus.domain.repository.PremiumSpanRepository;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionRateDto;
import com.brihaspathee.zeus.helper.interfaces.MemberPremiumHelper;
import com.brihaspathee.zeus.helper.interfaces.PremiumSpanHelper;
import com.brihaspathee.zeus.mapper.interfaces.PremiumSpanMapper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
     * Create the premium spans for the enrollment span
     * @param transactionDto
     * @param enrollmentSpan
     * @return
     */
    @Override
    public List<PremiumSpan> createPremiumSpans(TransactionDto transactionDto,
                                                EnrollmentSpan enrollmentSpan,
                                                Account account) {
        List<TransactionRateDto> premiumRates = sortPremiumDates(transactionDto.getTransactionRates());
        // Create the premium spans based on the information that is available in the PREAMTTOT rate
        // If there are more than one PREAMTTOT rates then as many premium spans will be created
        List<PremiumSpan> premiumSpans = createPremiumSpans(premiumRates, enrollmentSpan);
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
                premiumSpanDto.setZtcn(ztcn);
                memberPremiumHelper.setMemberPremiums(premiumSpanDto, premiumSpan);
                premiumSpanDtos.add(premiumSpanDto);
            });
            enrollmentSpanDto.setPremiumSpans(premiumSpanDtos.stream().collect(Collectors.toSet()));
        }
    }

    /**
     * Populate the premium amounts for each of the premium spans
     * @param premiumSpans
     * @param transactionRateDtos
     */
    private void populatePremiumAmounts(List<PremiumSpan> premiumSpans,
                                        List<TransactionRateDto> transactionRateDtos){
        premiumSpans.stream().forEach(premiumSpan -> {
            LocalDate premiumStartDate = premiumSpan.getStartDate();
            List<TransactionRateDto> matchedRates = transactionRateDtos.stream()
                    .filter(
                            rateDto ->
                                    rateDto.getRateStartDate().equals(premiumStartDate))
                    .collect(Collectors.toList());
            matchedRates.stream().forEach(rateDto -> {
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
            }else if(rateType.equals("APTCAMT")){
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
        // PREAMTTOT is already set as part of creating the premium span in the createPremiumSpans() method
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
     * @param premiumRates
     * @param enrollmentSpan
     * @return
     */
    private List<PremiumSpan> createPremiumSpans(List<TransactionRateDto> premiumRates, EnrollmentSpan enrollmentSpan){
        log.info("Premium Rates:{}", premiumRates);
        List<PremiumSpan> premiumSpans = new ArrayList<>();
        premiumRates.stream().forEach(rateDto -> {
            PremiumSpan premiumSpan = PremiumSpan.builder()
                    .premiumSpanCode(ZeusRandomStringGenerator.randomString(15))
                    .enrollmentSpan(enrollmentSpan)
                    .startDate(rateDto.getRateStartDate())
                    .csrVariant(rateDto.getCsrVariant())
                    .acctPremiumSpanSK(null)
                    .totalPremAmount(rateDto.getTransactionRate())
                    .build();
            LocalDate endDate = rateDto.getRateEndDate();
            if(endDate != null){
                premiumSpan.setEndDate(endDate);
            }else{
                int year = rateDto.getRateStartDate().getYear();
                premiumSpan.setEndDate(LocalDate.of(year, 12, 31));
            }
            if(premiumSpans != null && !premiumSpans.isEmpty()){
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
}
