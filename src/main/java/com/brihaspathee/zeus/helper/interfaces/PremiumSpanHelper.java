package com.brihaspathee.zeus.helper.interfaces;

import com.brihaspathee.zeus.domain.entity.Account;
import com.brihaspathee.zeus.domain.entity.EnrollmentSpan;
import com.brihaspathee.zeus.domain.entity.PremiumSpan;
import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import com.brihaspathee.zeus.dto.account.PremiumSpanDto;
import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import com.brihaspathee.zeus.info.ChangeTransactionInfo;

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

    /**
     * Save the updated premium spans
     * @param premiumSpanDtos premium spans that need to be saved
     * @param enrollmentSpan the enrollment span that the premium span belongs
     * @return return the saved premium spans
     */
    List<PremiumSpan> saveUpdatedPremiumSpans(List<PremiumSpanDto> premiumSpanDtos, EnrollmentSpan enrollmentSpan);

    /**
     * Process a financial change for a transaction
     * @param changeTransactionInfo - Contains details about the change transaction
     * @param transactionDto - Change transaction as received
     * @param accountDto - the account for which the transaction was reeived
     * @param account - the account entity
     * @param enrollmentSpan - enrollmentSpan entity that is created
     * @param matchedEnrollmentSpanDto - The enrollment span that was matched for the change transaction
     */
    void processFinancialChange(ChangeTransactionInfo changeTransactionInfo,
                                TransactionDto transactionDto,
                                Account account,
                                AccountDto accountDto,
                                EnrollmentSpan enrollmentSpan,
                                EnrollmentSpanDto matchedEnrollmentSpanDto);

    /**
     * Cancel the premium spans associated with the enrollment span
     * @param enrollmentSpanDto
     * @param enrollmentSpan
     */
    void cancelPremiumSpans(EnrollmentSpanDto enrollmentSpanDto, EnrollmentSpan enrollmentSpan);

    /**
     * Term the premium spans associated with the enrollment span
     * @param enrollmentSpanDto
     * @param enrollmentSpan
     */
    void termPremiumSpans(EnrollmentSpanDto enrollmentSpanDto, EnrollmentSpan enrollmentSpan);

    /**
     * Reinstate the premium spans associated with the enrollment span
     * @param enrollmentSpanDto
     * @param enrollmentSpan
     */
    void reinstatePremiumSpans(EnrollmentSpanDto enrollmentSpanDto, EnrollmentSpan enrollmentSpan);
}
