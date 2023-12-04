package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.AlternateContactDto;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:38 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface AlternateContactHelper {

    /**
     * Create alternate contact
     * @param member
     * @param transactionMemberDto
     */
    void createAlternateContact(Member member, TransactionMemberDto transactionMemberDto);

    /**
     * Set the alternate contact dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setAlternateContact(MemberDto memberDto, Member member);
}
