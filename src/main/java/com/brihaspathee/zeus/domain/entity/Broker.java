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
 * Time: 2:45 PM
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
@Table(name = "BROKER")
public class Broker {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "broker_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID brokerSK;

    /**
     * The key of the broker in MMS
     */
    @Column(name = "acct_broker_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctBrokerSK;

    /**
     * The account of the broker
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_sk")
    private Account account;

    /**
     * The unique code that is associated with the broker
     */
    @Column(name = "broker_code", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String brokerCode;

    /**
     * The name of the broker
     */
    @Column(name = "broker_name", columnDefinition = "varchar", nullable = false, updatable = true, length = 100)
    private String brokerName;

    /**
     * The id of the broker
     */
    @Column(name = "broker_id", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String brokerId;

    /**
     * The name of the agency
     */
    @Column(name = "agency_name", columnDefinition = "varchar", nullable = true, updatable = true, length = 100)
    private String agencyName;

    /**
     * The id of the agency
     */
    @Column(name = "agency_id", columnDefinition = "varchar", nullable = true, updatable = true, length = 50)
    private String agencyId;

    /**
     * The account number 1 of the broker
     */
    @Column(name = "account_number_1", columnDefinition = "varchar", nullable = true, updatable = true, length = 50)
    private String accountNumber1;

    /**
     * The account number 2 of the broker
     */
    @Column(name = "account_number_2", columnDefinition = "varchar", nullable = true, updatable = true, length = 50)
    private String accountNumber2;

    /**
     * The zeus transaction control number of the transaction that created the broker
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * The start date of the broker
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The end date of the broker
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Identifies if the broker was updated
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
