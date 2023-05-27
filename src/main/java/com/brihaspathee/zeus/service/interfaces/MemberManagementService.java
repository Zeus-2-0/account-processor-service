package com.brihaspathee.zeus.service.interfaces;

import com.brihaspathee.zeus.dto.account.AccountDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, May 2023
 * Time: 4:44 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberManagementService {

    /**
     * Get account by account number
     * @param accountNumber Account number of the account that needs to be retrieved
     * @return Return the account dto of the matching account
     */
    AccountDto getAccountByAccountNumber(String accountNumber);
}
