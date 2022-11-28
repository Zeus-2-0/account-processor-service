package com.brihaspathee.zeus.web.model;

import com.brihaspathee.zeus.dto.transaction.TransactionDto;
import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 3:57 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProcessingRequest {

    /**
     * The account number for which the request is received.
     */
    private String accountNumber;

    /**
     * The transaction details that needs to be processed
     */
    private TransactionDto transactionDto;
}
