package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
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
     * Create the member who are in the transaction
     * @param members
     * @param account
     */
    List<Member> createMembers(List<TransactionMemberDto> members, Account account);

    /**
     * Set the members in the account to send to MMS
     * @param accountDto
     * @param account
     */
    void setMember(AccountDto accountDto, Account account);

    /**
     * Match the members in the transaction with members in the account
     * @param accountDto The account dto that contains the members in the account
     * @param transactionDto The transaction dto that contains the members in the transaction
     * @param account The account entity to which the matched members have to be added
     */
    void matchMember(AccountDto accountDto, TransactionDto transactionDto, Account account);

    /**
     * Create the member who are not in the transaction, but are present in the account
     * @param memberDto - Member to be created
     * @param account - The account for which the member is to be created
     */
    Member createMember(MemberDto memberDto, Account account);
}
