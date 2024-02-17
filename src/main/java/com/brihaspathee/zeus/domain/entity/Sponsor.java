package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, November 2022
 * Time: 4:03 PM
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
@Table(name = "SPONSOR")
public class Sponsor {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "sponsor_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID sponsorSK;

    /**
     * The key of the sponsor in MMS
     */
    @Column(name = "acct_sponsor_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctSponsorSK;

    /**
     * The account of the sponsor
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_sk")
    private Account account;

    /**
     * The unique code that is associated with the sponsor
     */
    @Column(name = "sponsor_code", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String sponsorCode;

    /**
     * The name of the sponsor
     */
    @Column(name = "sponsor_name", columnDefinition = "varchar", nullable = false, updatable = true, length = 100)
    private String sponsorName;

    /**
     * The id of the sponsor
     */
    @Column(name = "sponsor_id", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String sponsorId;

    /**
     * The zeus transaction control number of the transaction that created the sponsor
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * The start date of the sponsor
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The end date of the sponsor
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Identifies if the sponsor was updated
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
