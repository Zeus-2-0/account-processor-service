package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 12:54 PM
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
@Table(name = "ALTERNATE_CONTACT")
public class AlternateContact {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "alternate_contact_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID alternateContactSK;

    /**
     * The key assigned to the alternate contact record in MMS
     */
    @Column(name = "acct_alt_contact_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    private UUID acctAltContactSK;

    /**
     * The member to whom the alternate contact is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * Unique code assigned to the alternate contact
     */
    @Column(name = "alternate_contact_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String alternateContactCode;

    /**
     * The type of the alternate contact
     */
    @Column(name = "alternate_contact_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String alternateContactTypeCode;

    /**
     * The first name of the alternate contact
     */
    @Column(name = "first_name", length = 100, columnDefinition = "varchar", nullable = true)
    private String firstName;

    /**
     * The middle name of the alternate contact
     */
    @Column(name = "middle_name", length = 50, columnDefinition = "varchar", nullable = true)
    private String middleName;

    /**
     * The last name of the alternate contact
     */
    @Column(name = "last_name", length = 100, columnDefinition = "varchar", nullable = false)
    private String lastName;

    /**
     * The identifier type received for the alternate contact
     */
    @Column(name = "identifier_type_code", length = 50, columnDefinition = "varchar", nullable = true)
    private String identifierTypeCode;

    /**
     * The identifier received for the alternate contact
     */
    @Column(name = "identifier_value", length = 50, columnDefinition = "varchar", nullable = true)
    private String identifierValue;

    /**
     * The phone type received for the alternate contact
     */
    @Column(name = "phone_type_code", length = 50, columnDefinition = "varchar", nullable = true)
    private String phoneTypeCode;

    /**
     * The phone number received for the alternate contact
     */
    @Column(name = "phone_number", length = 50, columnDefinition = "varchar", nullable = true)
    private String phoneNumber;

    /**
     * The email received for the alternate contact
     */
    @Column(name = "email", length = 50, columnDefinition = "varchar", nullable = true)
    private String email;

    /**
     * The address line 1 of the address received for the alternate contact
     */
    @Column(name = "address_line_1", length = 100, columnDefinition = "varchar", nullable = true)
    private String addressLine1;

    /**
     * The address line 2 of the address received for the alternate contact
     */
    @Column(name = "address_line_2", length = 50, columnDefinition = "varchar", nullable = true)
    private String addressLine2;

    /**
     * The city of the address received for the alternate contact
     */
    @Column(name = "city", length = 50, columnDefinition = "varchar", nullable = true)
    private String city;

    /**
     * The state of the address received for the alternate contact
     */
    @Column(name = "state_type_code", length = 50, columnDefinition = "varchar", nullable = true)
    private String stateTypeCode;

    /**
     * The zipcode of the address received for the alternate contact
     */
    @Column(name = "zip_code", length = 50, columnDefinition = "varchar", nullable = true)
    private String zipCode;

    /**
     * Start date of the alternate contact
     */
    @Column(name = "start_date", columnDefinition = "datetime", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the alternate contact
     */
    @Column(name = "end_date", columnDefinition = "datetime", nullable = true)
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
