package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Member;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionMemberDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2022
 * Time: 1:29 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberPremiumHelper {

    /**
     * Create member premiums
     * @param transactionMemberDtos
     * @param premiumSpan
     * @param members
     */
    void createMemberPremiums(List<TransactionMemberDto> transactionMemberDtos,
                                      PremiumSpan premiumSpan,
                                      List<Member> members);

    /**
     * Set the member premium span to send to MMS
     * @param premiumSpanDto
     * @param premiumSpan
     */
    void setMemberPremiums(PremiumSpanDto premiumSpanDto, PremiumSpan premiumSpan);
}
