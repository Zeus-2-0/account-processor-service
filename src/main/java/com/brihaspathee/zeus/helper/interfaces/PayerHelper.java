package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 27, November 2022
 * Time: 4:46 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface PayerHelper {

    /**
     * Create a payer
     * @param transactionDto
     */
    void createPayer(TransactionDto transactionDto, Account account);

    /**
     * Set the payer to dto to send to MMS
     * @param accountDto
     * @param account
     */
    void setPayer(AccountDto accountDto, Account account);
}
