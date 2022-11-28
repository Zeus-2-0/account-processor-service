package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, November 2022
 * Time: 6:35 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.domain.entity
 * To change this template use File | Settings | File and Code Template
 */
@Getter
@Setter
@Builder
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ENROLLMENT_SPAN")
public class EnrollmentSpan {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-char")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "enrollment_span_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID enrollmentSpanSK;

    /**
     * The key of the enrollment span that is present in the account
     */
    @Column(name = "acct_enrollment_span_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    private UUID acctEnrollmentSpanSK;

    /**
     * Unique enrollment span code that is assigned to the enrollment span
     */
    @Column(name = "enrollment_span_code", length = 50, columnDefinition = "varchar", nullable = false, updatable = false)
    private String enrollmentSpanCode;

    /**
     * The account to which the enrollment span is associated
     */
    @ManyToOne
    @JoinColumn(name = "account_sk")
    private Account account;

    /**
     * The state that is associated with the enrollment span
     */
    @Column(name = "state_type_code", length = 50, columnDefinition = "varchar", nullable = false)
    private String stateTypeCode;

    /**
     * The marketplace that is associated with the enrollment span
     */
    @Column(name = "marketplace_type_code", length = 50, columnDefinition = "varchar", nullable = false)
    private String marketplaceTypeCode;

    /**
     * The business unit of the enrollment span
     */
    @Column(name = "business_unit_type_code", length = 50, columnDefinition = "varchar", nullable = false)
    private String businessUnitTypeCode;

    /**
     * The start date of the enrollment span
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The end date of the enrollment span
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * The exchange subscriber id associated with the enrollment span
     */
    @Column(name = "exchange_subscriber_id", columnDefinition = "varchar", length = 50, nullable = false)
    private String exchangeSubscriberId;

    /**
     * The effectuation date of the enrollment span
     */
    @Column(name = "effectuation_date", nullable = true)
    private LocalDate effectuationDate;

    /**
     * The plan id associated with the enrollment span
     */
    @Column(name = "plan_id", columnDefinition = "varchar", nullable = false, length = 100)
    private String planId;

    /**
     * The product type associated with the plan of the enrollment span
     */
    @Column(name = "product_type_code", columnDefinition = "varchar", length = 100, nullable = false)
    private String productTypeCode;

    /**
     * The group policy id associated with the enrollment span
     */
    @Column(name = "group_policy_id", columnDefinition = "varchar", length = 100, nullable = false)
    private String groupPolicyId;

    /**
     * The status of the enrollment span
     */
    @Column(name = "status_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String statusTypeCode;

    /**
     * The list of premium spans that are associated with the enrollment span
     */
    @OneToMany(mappedBy = "enrollmentSpan", fetch = FetchType.EAGER)
    private List<PremiumSpan> premiumSpans;

    /**
     * The date when the record was created
     */
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * The date when the record was updated
     */
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
