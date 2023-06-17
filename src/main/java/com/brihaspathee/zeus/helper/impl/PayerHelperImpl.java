package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Payer;
import com.brihaspathee.zeus.domain.entity.Sponsor;
import com.brihaspathee.zeus.domain.repository.PayerRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.PayerHelper;
import com.brihaspathee.zeus.mapper.interfaces.PayerMapper;
import com.brihaspathee.zeus.util.AccountProcessorUtil;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2022
 * Time: 4:48 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayerHelperImpl implements PayerHelper {

    /**
     * Payer repository instance to perform CRUD operations
     */
    private final PayerRepository payerRepository;

    /**
     * Payer mapper instance
     */
    private final PayerMapper payerMapper;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create a payer
     * @param transactionDto
     * @param account
     */
    @Override
    public void createPayer(TransactionDto transactionDto, Account account) {
        if(transactionDto.getPayer() != null){
            List<Payer> payers = new ArrayList<>();
            String payerCode = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                    "payerCode");
            Payer payer = Payer.builder()
                    .acctPayerSK(null)
                    .account(account)
                    .payerCode(payerCode)
                    .payerName(transactionDto.getPayer().getPayerName())
                    .payerId(transactionDto.getPayer().getPayerId())
                    .startDate(transactionDto.getBroker().getReceivedDate().toLocalDate())
                    .endDate(null)
                    .changed(true)
                    .build();
            payer = payerRepository.save(payer);
            payers.add(payer);
            account.setPayers(payers);
        }

    }

    /**
     * Set the payer in account dto to send to MMS
     * @param accountDto
     * @param account
     */
    @Override
    public void setPayer(AccountDto accountDto, Account account) {
        if(account.getPayers() != null && account.getPayers().size() > 0){
            accountDto.setPayers(
                    payerMapper.payersToPayerDtos(
                            account.getPayers())
                            .stream()
                            .collect(Collectors.toSet()));
        }
    }
}
