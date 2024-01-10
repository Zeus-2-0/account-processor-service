package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:13 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberEmailHelper {

    /**
     * Create the member email
     * @param member
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    void createMemberEmail(Member member,
                           TransactionMemberDto transactionMemberDto,
                           String ztcn,
                           String source);

    /**
     * Set the member email dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberEmail(MemberDto memberDto, Member member);

    /**
     * Match the member's email in the account with the email from the transaction
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     * @param ztcn
     * @param source
     */
    void matchMemberEmail(Member member,
                          MemberDto memberDto,
                          TransactionMemberDto transactionMemberDto,
                          String ztcn,
                          String source);
}
