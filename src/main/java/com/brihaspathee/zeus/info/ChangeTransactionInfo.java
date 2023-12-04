package com.brihaspathee.zeus.info;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 28, November 2023
 * Time: 7:04AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.util
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTransactionInfo {

    /**
     * Identifies of there are multiple financial information present in the transaction
     */
    private boolean isMultipleFinancialsPresent;

    /**
     * Identifies if one or more premium span update is required
     * If it is false then it is considered a non-financial change
     */
    private boolean isPremiumSpanUpdateRequired;

    /**
     * The enrollment span that was matched for the transaction
     */
    private UUID matchedEnrollmentSpanSK;

    /**
     * The list of premium span update info will be populated if there are
     * multiple financials (i.e. rate with rate type code "PREAMTTOT") are present in the transaction
     */
    private List<PremiumSpanUpdateInfo> premiumSpanUpdateInfos;

    /**
     * This object will be populated if there is only one financial (i.e. rate with rate type code "PREAMTTOT") present
     * in the transaction
     */
    private PremiumSpanUpdateInfo premiumSpanUpdateInfo;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "ChangeTransactionInfo{" +
                "isMultipleFinancialsPresent=" + isMultipleFinancialsPresent +
                ", isPremiumSpanUpdateRequired=" + isPremiumSpanUpdateRequired +
                ", matchedEnrollmentSpanSK=" + matchedEnrollmentSpanSK +
                ", premiumSpanUpdateInfos=" + premiumSpanUpdateInfos +
                ", premiumSpanUpdateInfo=" + premiumSpanUpdateInfo +
                '}';
    }
}
