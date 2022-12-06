package com.brihaspathee.zeus.broker.message;

import com.brihaspathee.zeus.dto.account.AccountDto;
import lombok.*;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 05, December 2022
 * Time: 4:25 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    /**
     * The account dto that is sent as request to update
     */
    private AccountDto accountDto;
}
