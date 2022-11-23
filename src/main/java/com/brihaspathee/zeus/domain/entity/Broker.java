package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

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
    @Type(type = "uuid-char")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "broker_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID brokerSK;

    /**
     * The key of the broker in MMS
     */
    @Column(name = "acct_broker_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
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
