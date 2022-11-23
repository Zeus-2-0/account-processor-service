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
 * Time: 3:59 PM
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
@Table(name = "PAYER")
public class Payer {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-char")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "payer_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID payerSK;

    /**
     * The key of the payer in MMS
     */
    @Column(name = "acct_payer_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    private UUID acctPayerSK;

    /**
     * The account of the payer
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_sk")
    private Account account;

    /**
     * The unique code that is associated with the payer
     */
    @Column(name = "payer_code", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String payerCode;

    /**
     * The name of the payer
     */
    @Column(name = "payer_name", columnDefinition = "varchar", nullable = false, updatable = true, length = 100)
    private String payerName;

    /**
     * The id of the payer
     */
    @Column(name = "payer_id", columnDefinition = "varchar", nullable = false, updatable = true, length = 50)
    private String payerId;

    /**
     * The start date of the payer
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The end date of the payer
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
