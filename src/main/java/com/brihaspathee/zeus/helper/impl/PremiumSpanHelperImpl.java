package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.*;
import com.brihaspathee.zeus.domain.repository.MemberPremiumRepository;
import com.brihaspathee.zeus.domain.repository.PremiumSpanRepository;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberIdentifierDto;
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
        List<PremiumSpan> premiumSpans = new ArrayList<>();
        PremiumSpan premiumSpan = PremiumSpan.builder()
                .enrollmentSpan(enrollmentSpan)
                .acctPremiumSpanSK(null)
                .premiumSpanCode(ZeusRandomStringGenerator.randomString(15))
                .startDate(determineStartDate(transactionDto))
                .endDate(determineEndDate(transactionDto))
                .csrVariant(transactionDto.getTransactionDetail().getCsrVariant())
                .build();
        populatePremiumAmounts(premiumSpan, transactionDto);
        premiumSpan = premiumSpanRepository.save(premiumSpan);
        memberPremiumHelper.createMemberPremiums(transactionDto.getMembers(),
                premiumSpan,
                account.getMembers());
        premiumSpans.add(premiumSpan);
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
     * Determine the start date of the premium span
     * @param transactionDto
     * @return
     */
    private LocalDate determineStartDate(TransactionDto transactionDto){
        Optional<TransactionRateDto> transactionRateDto = transactionDto.getTransactionRates().stream().filter(rateDto -> {
           return rateDto.getRateTypeCode().equals("PREAMTTOT");
        }).findFirst();
        if(transactionRateDto.isPresent()){
            return transactionRateDto.get().getRateStartDate();
        }else{
            return null;
        }
    }

    /**
     * Determine the end date of the premium span
     * @param transactionDto
     * @return
     */
    private LocalDate determineEndDate(TransactionDto transactionDto){
        Optional<TransactionRateDto> transactionRateDto = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("PREAMTTOT");
        }).findFirst();
        if(transactionRateDto.isPresent()){
            LocalDate endDate = transactionRateDto.get().getRateEndDate();
            if(endDate != null){
                return endDate;
            }else{
                int year = transactionRateDto.get().getRateStartDate().getYear();
                return LocalDate.of(year, 12, 31);
            }
        }else{
            return null;
        }
    }

    /**
     * Populate the premium amounts for the premium span
     * @param premiumSpan
     * @param transactionDto
     */
    private void populatePremiumAmounts(PremiumSpan premiumSpan,
                                        TransactionDto transactionDto){
        // Get the premium amount total
        Optional<TransactionRateDto> preAmtTotDto = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("PREAMTTOT");
        }).findFirst();
        if(preAmtTotDto.isPresent()){
            premiumSpan.setTotalPremAmount(preAmtTotDto.get().getTransactionRate());
        }else{
            premiumSpan.setTotalPremAmount(BigDecimal.valueOf(0));
        }

        // Get the total responsible amount
        Optional<TransactionRateDto> totRespAmt = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("TOTRESAMT");
        }).findFirst();
        if(totRespAmt.isPresent()){
            premiumSpan.setTotalResponsibleAmount(totRespAmt.get().getTransactionRate());
        }else{
            premiumSpan.setTotalResponsibleAmount(BigDecimal.valueOf(0));
        }

        // Get the APTC amount
        Optional<TransactionRateDto> aptcAmt = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("APTCAMT");
        }).findFirst();
        if(aptcAmt.isPresent()){
            premiumSpan.setAptcAmount(aptcAmt.get().getTransactionRate());
        }else{
            premiumSpan.setAptcAmount(BigDecimal.valueOf(0));
        }

        // Get the CSR amount
        Optional<TransactionRateDto> csrAmt = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("CSRAMT");
        }).findFirst();
        if(csrAmt.isPresent()){
            premiumSpan.setCsrAmount(csrAmt.get().getTransactionRate());
        }else{
            premiumSpan.setCsrAmount(BigDecimal.valueOf(0));
        }

        // Get the other pay amounts
        BigDecimal otherPayAmt = BigDecimal.valueOf(0);
        Optional<TransactionRateDto> otherpayamt1 = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("OTHERPAYAMT1");
        }).findFirst();
        Optional<TransactionRateDto> otherpayamt2 = transactionDto.getTransactionRates().stream().filter(rateDto -> {
            return rateDto.getRateTypeCode().equals("OTHERPAYAMT2");
        }).findFirst();
        if(otherpayamt1.isPresent()){
            otherPayAmt.add(otherpayamt1.get().getTransactionRate());
        }
        if(otherpayamt2.isPresent()){
            otherPayAmt.add(otherpayamt2.get().getTransactionRate());
        }
        premiumSpan.setOtherPayAmount(otherPayAmt);
    }
}
