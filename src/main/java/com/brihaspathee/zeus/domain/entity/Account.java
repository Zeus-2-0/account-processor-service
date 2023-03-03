package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 20, November 2022
 * Time: 6:10 PM
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
@Table(name = "ACCOUNT")
public class Account {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "account_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID accountSK;

    /**
     * The transaction that is being processed for the account
     */
    @OneToOne
    @JoinColumn(name = "transaction_sk")
    private Transaction transaction;

    /**
     * Identifies if an account match was found in the MMS for the transaction
     */
    @Column(name = "match_found")
    private boolean matchFound;

    /**
     * The key of the account in MMS if a match was found
     */
    @Column(name = "match_account_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    private UUID matchAccountSK;

    /**
     * Unique number that is assigned to the account
     */
    @Column(name = "account_number", length = 50, columnDefinition = "varchar", updatable = false, nullable = false)
    private String accountNumber;

    /**
     * The line of business of the account
     */
    @Column(name = "line_of_business_type_code", columnDefinition = "varchar", nullable = false, length = 50)
    private String lineOfBusinessTypeCode;

    /**
     * The enrollment spans that are associated with the account
     */
    @OneToMany(mappedBy = "account")
    private List<EnrollmentSpan> enrollmentSpan;

    /**
     * The list of members that are associated with the account
     */
    @OneToMany(mappedBy = "account")
    private List<Member> members;

    /**
     * The list of brokers that are associated with the account
     */
    @OneToMany(mappedBy = "account")
    private List<Broker> brokers;

    /**
     * The list of payers that are associated with the account
     */
    @OneToMany(mappedBy = "account")
    private List<Payer> payers;

    /**
     * The list of sponsors that are associated with the account
     */
    @OneToMany(mappedBy = "account")
    private List<Sponsor> sponsors;

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
