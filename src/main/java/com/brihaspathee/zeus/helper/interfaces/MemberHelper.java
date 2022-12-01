package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 7:20 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberHelper {

    /**
     * Create the member
     * @param members
     * @param account
     */
    List<Member> createMember(List<TransactionMemberDto> members, Account account);

    /**
     * Set the members in the account to send to MMS
     * @param accountDto
     * @param account
     */
    void setMember(AccountDto accountDto, Account account);
}
