package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:36 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberPhoneHelper {

    /**
     * Create member phone
     * @param member
     * @param transactionMemberDto
     */
    void createMemberPhone(Member member, TransactionMemberDto transactionMemberDto);

    /**
     * Set the member phone dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberPhone(MemberDto memberDto, Member member);

    /**
     * Match member phones from the transaction to the account
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    void matchMemberPhone(Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto);
}
