package com.brihaspathee.zeus.broker.message;

import com.brihaspathee.zeus.validator.MemberValidationResult;
import com.brihaspathee.zeus.validator.rules.RuleResult;
import lombok.*;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 05, December 2022
 * Time: 4:09 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateResponse {

    /**
     * A unique id that is created as a response for processing the account information
     */
    private String responseId;

    /**
     * The request payload id for which the response is sent
     */
    private String requestPayloadId;

    /**
     * Account number for which the response is created
     */
    private String accountNumber;

    /**
     * indicates if the request received for the account was processed successfully or not
     */
    private boolean processedSuccessfully;

    /**
     * The results of the rules that were executed for the account
     */
    private List<RuleResult> ruleResults;

    /**
     * The validation results for the members who are present in the account
     */
    private List<MemberValidationResult> memberValidationResults;

    /**
     * toString method
     * @return
     */
    @Override
    public String toString() {
        return "AccountUpdateResponse{" +
                "responseId='" + responseId + '\'' +
                ", requestPayloadId='" + requestPayloadId + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", processedSuccessfully=" + processedSuccessfully +
                ", ruleResults=" + ruleResults +
                ", memberValidationResults=" + memberValidationResults +
                '}';
    }
}
