package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, November 2022
 * Time: 6:57 AM
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
@Table(name = "PREMIUM_SPAN")
public class PremiumSpan {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-char")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "premium_span_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID premiumSpanSK;

    /**
     * The key of this premium span in MMS
     */
    @Column(name = "acct_premium_span_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    private UUID acctPremiumSpanSK;

    /**
     * The enrollment span to which the premium span is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "enrollment_span_sk")
    private EnrollmentSpan enrollmentSpan;

    /**
     * Unique span code that is assigned to the premium span
     */
    @Column(name = "premium_span_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String premiumSpanCode;

    /**
     * The start date of the premium span
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * The end date of the enrollment span
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * The CSR Variant present in the plan id received for the enrollment span
     */
    @Column(name = "csr_variant", nullable = false)
    private String csrVariant;

    /**
     * The total premium amount for the premium span
     */
    @Column(name = "total_prem_amt", nullable = false)
    private BigDecimal totalPremAmount;

    /**
     * The total responsible amount for the premium span
     */
    @Column(name = "total_resp_amt", nullable = false)
    private BigDecimal totalResponsibleAmount;

    /**
     * The APTC amount for the premium span
     */
    @Column(name = "aptc_amt", nullable = false)
    private BigDecimal aptcAmount;

    /**
     * The sum of other pay amt 1 and other pay amt 2 for the premium span
     */
    @Column(name = "other_pay_amt", nullable = false)
    private BigDecimal otherPayAmount;

    /**
     * The CSR Amount for the premium span
     */
    @Column(name = "csr_amt", nullable = false)
    private BigDecimal csrAmount;

    /**
     * List of members associated with the premium span
     */
    @OneToMany(mappedBy = "premiumSpan")
    List<MemberPremium> memberPremiums;

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
