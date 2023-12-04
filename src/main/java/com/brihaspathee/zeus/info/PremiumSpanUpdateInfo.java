package com.brihaspathee.zeus.info;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 01, December 2023
 * Time: 7:02 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.info
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PremiumSpanUpdateInfo {

    private UUID matchedPremiumSpanSK;

    private LocalDate rateEffectiveDate;

    private LocalDate rateEndDate;

    private String transactionCSRVariant;

    /**
     * Indicates if a new premium span should be created or not,
     * This is populated only when there are multiple financials (i.e. multiple "PREAMTTOT" rates) present in the
     * transaction
     * Because for the single financial, if there was a premium span updated required on a change transaction
     * that by default means a new premium span is to be created
     */
    private boolean createNewPremiumSpan;

    /**
     * Identifies the kind of updates that need to be made to the matching premium span
     * 0 - No updates are needed for the matching premium span
     * 1 - Matching premium span should be canceled
     * 2 - Term the matching premium span with end date received in the transaction for the rate
     * 3 - Matching premium span is already updated/canceled previously
     */
    private int updateRequired;

    private BigDecimal preAmtTot;

    private BigDecimal totResAmt;

    private BigDecimal aptcAmt;

    private BigDecimal otherPayAmt;

    private BigDecimal csrAmt;

    @Override
    public String toString() {
        return "PremiumSpanUpdateInfo{" +
                "matchedPremiumSpanSK=" + matchedPremiumSpanSK +
                ", rateEffectiveDate=" + rateEffectiveDate +
                ", rateEndDate=" + rateEndDate +
                ", transactionCSRVariant='" + transactionCSRVariant + '\'' +
                ", createNewPremiumSpan=" + createNewPremiumSpan +
                ", updateRequired=" + updateRequired +
                ", preAmtTot=" + preAmtTot +
                ", totResAmt=" + totResAmt +
                ", aptcAmt=" + aptcAmt +
                ", otherPayAmt1=" + otherPayAmt +
                ", csrAmt=" + csrAmt +
                '}';
    }
}
