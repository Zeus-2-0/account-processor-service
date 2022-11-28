package com.brihaspathee.zeus.helper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Broker;
import com.brihaspathee.zeus.domain.entity.Sponsor;
import com.brihaspathee.zeus.domain.repository.SponsorRepository;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.helper.interfaces.SponsorHelper;
import com.brihaspathee.zeus.util.ZeusRandomStringGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
     * Sponsor Repository instance to perform CRUD operations
     */
    private final SponsorRepository sponsorRepository;

    /**
     * Create a sponsor
     * @param transactionDto
     * @param account
     */
    @Override
    public void createSponsor(TransactionDto transactionDto, Account account) {
        if(transactionDto.getSponsor() != null){
            List<Sponsor> sponsors = new ArrayList<>();
            Sponsor sponsor = Sponsor.builder()
                    .acctSponsorSK(null)
                    .account(account)
                    .sponsorCode(ZeusRandomStringGenerator.randomString(15))
                    .sponsorName(transactionDto.getSponsor().getSponsorName())
                    .sponsorId(transactionDto.getSponsor().getSponsorId())
                    .startDate(transactionDto.getBroker().getReceivedDate().toLocalDate())
                    .endDate(null)
                    .build();
            sponsor = sponsorRepository.save(sponsor);
            sponsors.add(sponsor);
            account.setSponsors(sponsors);
        }

    }
}
