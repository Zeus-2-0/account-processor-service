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
     * @param ztcn
     * @param source
     */
    void createMemberIdentifier(Member member,
                                TransactionMemberDto transactionMemberDto,
                                String ztcn,
                                String source);

    /**
     * Set the member identifier dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberIdentifier(MemberDto memberDto, Member member);

    /**
     * Match the SSN of the member from the transaction to see if they match with the SSN
     * the member has in the account
     * @param member - The member entity
     * @param memberDto - The member dto
     * @param transactionMemberDto - The member's transaction information
     * @param ztcn
     * @param source
     */
    void matchMemberIdentifier(Member member,
                               MemberDto memberDto,
                               TransactionMemberDto transactionMemberDto,
                               String ztcn,
                               String source);
}
