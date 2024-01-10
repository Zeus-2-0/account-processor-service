package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

import java.time.LocalDateTime;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:37 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberAddressHelper {

    /**
     * Create member address
     * @param member
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    void createMemberAddress(Member member,
                             TransactionMemberDto transactionMemberDto,
                             String ztcn,
                             String source);

    /**
     * Set the member address dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberAddress(MemberDto memberDto, Member member);

    /**
     * Compare ana match the member addresses from the transaction and account
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    void matchMemberAddress(Member member,
                            MemberDto memberDto,
                            TransactionMemberDto transactionMemberDto,
                            String ztcn,
                            String source);
}
