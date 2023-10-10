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
public interface MemberLanguageHelper {

    /**
     * Create member language
     * @param member
     * @param transactionMemberDto
     */
    void createMemberLanguage(Member member, TransactionMemberDto transactionMemberDto);

    /**
     * Set the member language dto to  send to MMS
     * @param memberDto
     * @param member
     */
    void setMemberLanguage(MemberDto memberDto, Member member);

    /**
     * Match member language
     * @param member
     * @param memberDto
     * @param transactionMemberDto
     */
    void matchMemberLanguage(Member member, MemberDto memberDto, TransactionMemberDto transactionMemberDto);
}
