package com.brihaspathee.zeus.broker.message;

import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.web.model.ProcessingRequestDto;
import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 07, April 2024
 * Time: 7:14 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountProcessingResult {

    /**
     * The request received for processing the account
     */
    private ProcessingRequestDto processingRequestDto;

    /**
     * The account after the updates are made
     */
    private AccountDto accountDto;
}
