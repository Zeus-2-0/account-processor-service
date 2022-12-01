package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.dto.account.MemberDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, November 2022
 * Time: 7:34 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberIdentifierHelper {

    /**
     * Create member identifier
     * @param member
     * @param transactionMemberDto
     */
    void createMemberIdentifier(Member member, TransactionMemberDto transactionMemberDto);

    /**
     * Set the member identifier dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberIdentifier(MemberDto memberDto, Member member);
}
