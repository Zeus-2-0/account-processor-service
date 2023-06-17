package com.brihaspathee.zeus.web.model;

import com.brihaspathee.zeus.dto.account.EnrollmentSpanDto;
import lombok.*;

import java.util.List;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 13, December 2022
 * Time: 1:58 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.web.model
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentSpanStatusDto {

    /**
     * The enrollment span for which the status is/has to be calculated
     */
    private EnrollmentSpanDto currentEnrollmentSpan;

    /**
     * The enrollment spans that are prior to the current enrollment span
     */
    private List<EnrollmentSpanDto> priorEnrollmentSpans;
}
