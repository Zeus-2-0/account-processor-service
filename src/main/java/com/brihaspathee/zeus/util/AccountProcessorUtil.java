package com.brihaspathee.zeus.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 31, May 2023
 * Time: 6:10 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.util
 * To change this template use File | Settings | File and Code Template
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountProcessorUtil {

    /**
     * The spring environment instance
     */
    private final Environment environment;

    /**
     * Generate a unique code
     * @param entityCodes
     * @param key
     * @return
     */
    public String generateUniqueCode(Map<String, List<String>> entityCodes, String key){
        // Use the code from the test data if the profile is "test"
        if(Arrays.asList(environment.getActiveProfiles()).contains("test")){
//            log.info("Test Environment - entity codes:{}", entityCodes);
            if(entityCodes.get(key) != null && entityCodes.get(key).get(0)!=null){
                String code = entityCodes.get(key).get(0);
                entityCodes.get(key).remove(code);
                return code;
            }
        }
        // if it is not test then generate a new code
        return ZeusRandomStringGenerator.randomString(15);
    }
}
