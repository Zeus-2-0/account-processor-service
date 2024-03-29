package com.brihaspathee.zeus.service.impl;

import com.brihaspathee.zeus.dto.account.AccountDto;
import com.brihaspathee.zeus.dto.account.AccountList;
import com.brihaspathee.zeus.service.interfaces.MemberManagementService;
import com.brihaspathee.zeus.web.response.ZeusApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 26, May 2023
 * Time: 4:45 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.service.impl
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberManagementServiceImpl implements MemberManagementService {

    /**
     * The reference data host
     */
    @Value("${url.host.member-mgmt}")
    private String memberMgmtHost;

    /**
     * Webclient to connect with other rest APIs
     */
    private final WebClient webClient;

    /**
     * Get account by account number
     * @param accountNumber Account number of the account that needs to be retrieved
     * @return return account dto of the matching account
     */
    @Override
    public AccountDto getAccountByAccountNumber(String accountNumber) {
        log.info("Account number for which the mms is called:{}", accountNumber);

        // Retrieve account information from the member management service
        ZeusApiResponse<AccountList> apiResponse = webClient.get()
                .uri(memberMgmtHost+"zeus/account/"+accountNumber)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ZeusApiResponse<AccountList>>() {})
                .block();
        assert apiResponse != null;
        log.info("API Response:{}", apiResponse);
        log.info("Account Dto in API Response:{}", apiResponse.getResponse());
        AccountList accountList = apiResponse.getResponse();
        if(accountList != null && accountList.getAccountDtos()!=null && !accountList.getAccountDtos().isEmpty()){
            Optional<AccountDto> optionalAccountDto = accountList.getAccountDtos().stream().findFirst();
            return optionalAccountDto.orElse(null);
        }
        return null;
    }
}
