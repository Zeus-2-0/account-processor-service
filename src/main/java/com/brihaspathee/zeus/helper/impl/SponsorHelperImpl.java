package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.domain.entity.Sponsor;
import com.brihaspathee.zeus.domain.repository.SponsorRepository;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.SponsorHelper;
import com.brihaspathee.zeus.mapper.interfaces.SponsorMapper;
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
 * Time: 4:47 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SponsorHelperImpl implements SponsorHelper {

    /**
     * Sponsor mapper instance
     */
    private final SponsorMapper sponsorMapper;

    /**
     * Sponsor Repository instance to perform CRUD operations
     */
    private final SponsorRepository sponsorRepository;

    /**
     * The utility class for account processor service
     */
    private final AccountProcessorUtil accountProcessorUtil;

    /**
     * Create a sponsor
     * @param transactionDto
     * @param account
     */
    @Override
    public void createSponsor(TransactionDto transactionDto, Account account) {
        if(transactionDto.getSponsor() != null){
            List<Sponsor> sponsors = new ArrayList<>();
            String sponsorCode = accountProcessorUtil.generateUniqueCode(transactionDto.getEntityCodes(),
                    "sponsorCode");
            Sponsor sponsor = Sponsor.builder()
                    .acctSponsorSK(null)
                    .account(account)
                    .sponsorCode(sponsorCode)
                    .sponsorName(transactionDto.getSponsor().getSponsorName())
                    .sponsorId(transactionDto.getSponsor().getSponsorId())
                    .ztcn(transactionDto.getZtcn())
                    .source(transactionDto.getSource())
                    .startDate(transactionDto.getBroker().getReceivedDate().toLocalDate())
                    .endDate(null)
                    .changed(true)
                    .build();
            sponsor = sponsorRepository.save(sponsor);
            sponsors.add(sponsor);
            account.setSponsors(sponsors);
        }

    }

    /**
     * Set the sponsor to dto to send to MMS
     * @param accountDto
     * @param account
     */
    @Override
    public void setSponsor(AccountDto accountDto, Account account) {
        if(account.getSponsors() != null && account.getSponsors().size() > 0){
            accountDto.setSponsors(
                    sponsorMapper.sponsorsToSponsorDtos(
                                    account.getSponsors())
                            .stream()
                            .collect(Collectors.toSet()));
        }
    }
}
