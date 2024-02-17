package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import org.hibernate.type.SqlTypes;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 21, November 2022
 * Time: 1:25 PM
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
@Table(name = "MEMBER")
public class Member {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberSK;

    /**
     * The key that is assigned to the member in MMS
     */
    @Column(name = "acct_member_sk", length = 36, columnDefinition = "varchar", updatable = true, nullable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID acctMemberSK;

    /**
     * Account to which the member is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_sk")
    private Account account;

    /**
     * The unique code for the member by Transaction manager
     * There may be members in the account who were not received in the transaction
     * We need to be able to insert them without requiring a transaction member code
     */
    @Column(name = "trans_member_code", nullable = true, columnDefinition = "varchar", length = 50)
    private String transactionMemberCode;

    /**
     * The unique code for the member
     */
    @Column(name = "member_code", nullable = false, columnDefinition = "varchar", length = 50)
    private String memberCode;

    /**
     * The relationship of the member with HOH
     */
    @Column(name = "relationship_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String relationShipTypeCode;

    /**
     * The first name of the member
     */
    @Column(name = "first_name", columnDefinition = "varchar", length = 100, nullable = false)
    private String firstName;

    /**
     * The middle name of the member
     */
    @Column(name = "middle_name", columnDefinition = "varchar", length = 50, nullable = true)
    private String middleName;

    /**
     * The last name of the member
     */
    @Column(name = "last_name", columnDefinition = "varchar", length = 100, nullable = false)
    private String lastName;

    /**
     * The date of birth of the member
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /**
     * The gender of the member
     */
    @Column(name = "gender_type_code", columnDefinition = "varchar", length = 20, nullable = true)
    private String genderTypeCode;

    /**
     * The indicator that identifies if the member is a tobacco user
     */
    @Column(name = "tobacco_ind")
    private boolean tobaccoInd;

    /**
     * The height of the member
     */
    @Column(name = "height", columnDefinition = "decimal", nullable = true)
    private double height;

    /**
     * The weight of the member
     */
    @Column(name = "weight", columnDefinition = "decimal", nullable = true)
    private double weight;

    /**
     * The zeus transaction control number of the transaction that created the member
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * List of member address associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<MemberAddress> memberAddresses;

    /**
     * List of member emails associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<MemberEmail> memberEmails;

    /**
     * List of member languages associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<MemberLanguage> memberLanguages;

    /**
     * List of member identifiers associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<MemberIdentifier> memberIdentifiers;

    /**
     * List of member phone numbers associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<MemberPhone> memberPhones;

    /**
     * List of alternate contacts associated with the member
     */
    @OneToMany(mappedBy = "member")
    private List<AlternateContact> alternateContacts;

    /**
     * List of member premiums
     */
    @OneToMany(mappedBy = "member")
    private List<MemberPremium> memberPremiums;

    /**
     * Identifies if the member was updated
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
