package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 25, November 2022
 * Time: 6:39 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.helper.interfaces
 * To change this template use File | Settings | File and Code Template
 */
public interface PremiumSpanHelper {

    /**
     * Create the premium spans that are required for the enrollment span
     * @param transactionDto
     * @param enrollmentSpan
     * @return
     */
    List<PremiumSpan> createPremiumSpans(TransactionDto transactionDto,
                                         EnrollmentSpan enrollmentSpan,
                                         Account account);

    /**
     * Set the premium span to send to MMS
     * @param enrollmentSpanDto
     * @param enrollmentSpan
     */
    void setPremiumSpan(EnrollmentSpanDto enrollmentSpanDto,
                        EnrollmentSpan enrollmentSpan,
                        String ztcn);
}
