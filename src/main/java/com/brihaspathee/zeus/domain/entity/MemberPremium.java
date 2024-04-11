package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 25, November 2022
 * Time: 4:14 PM
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
@Table(name = "MEMBER_PREMIUM")
public class MemberPremium {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_premium_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberPremiumSK;

    /**
     * The key of the member premium record in MMS
     */
    @Column(name = "acct_mem_prem_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctMemPremSK;

    /**
     * The key of the premium span record in MMS
     */
    @Column(name = "acct_prem_span_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctPremSpanSK;

    /**
     * The key of the member record in MMS
     */
    @Column(name = "acct_member_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctMemberSK;

    /**
     * The premium span key
     */
    @ManyToOne
    @JoinColumn(name = "premium_span_sk")
    private PremiumSpan premiumSpan;

    /**
     * The key of the member
     */
    @ManyToOne
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * The exchange member id of the member
     */
    @Column(name = "exchange_member_id", length = 50, columnDefinition = "varchar", nullable = false, updatable = false)
    private String exchangeMemberId;

    /**
     * The rate of the member
     */
    @Column(name = "individual_premium_amount")
    private BigDecimal individualRateAmount;

    /**
     * Identifies if the member premium was updated
     */
    @Column(name = "changed", columnDefinition = "boolean", nullable = false)
    private boolean changed;

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
