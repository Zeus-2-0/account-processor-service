package com.brihaspathee.zeus.mapper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.dto.account.AccountDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 28, November 2022
 * Time: 11:25 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.mapper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface AccountMapper {

    /**
     * Convert account entity to account dto
     * @param account
     * @return
     */
    AccountDto accountToAccountDto(Account account);
}
