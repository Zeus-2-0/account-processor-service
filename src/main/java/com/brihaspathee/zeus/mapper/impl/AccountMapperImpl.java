package com.brihaspathee.zeus.mapper.impl;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.helper.interfaces.BrokerHelper;
import com.brihaspathee.zeus.mapper.interfaces.AccountMapper;
import com.brihaspathee.zeus.mapper.interfaces.BrokerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 30, November 2022
 * Time: 10:23 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMapperImpl implements AccountMapper {

    /**
     * Convert account entity to account dto
     * @param account
     * @return
     */
    @Override
    public AccountDto accountToAccountDto(Account account) {
        if(account == null){
            return null;
        }
        AccountDto accountDto = AccountDto.builder()
                .accountSK(account.getMatchAccountSK())
                .accountNumber(account.getAccountNumber())
                .lineOfBusinessTypeCode(account.getLineOfBusinessTypeCode())
                .createdDate(account.getCreatedDate())
                .updatedDate(account.getUpdatedDate())
                .build();
        return accountDto;
    }
}
