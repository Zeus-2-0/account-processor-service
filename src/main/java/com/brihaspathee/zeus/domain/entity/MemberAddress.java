package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

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
 * Date: 23, November 2022
 * Time: 5:03 AM
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
@Table(name = "MEMBER_ADDRESS")
public class MemberAddress {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_address_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberAddressSK;

    /**
     * The key assigned to the address record in MMS
     */
    @Column(name = "member_acct_address_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private UUID memberAcctAddressSK;

    /**
     * The member to whom the phone is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * Unique code assigned to the address
     */
    @Column(name = "member_address_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String memberAddressCode;

    /**
     * The type of the address
     */
    @Column(name = "address_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String addressTypeCode;

    /**
     * The address line of the address
     */
    @Column(name = "address_line_1", columnDefinition = "varchar", length = 50, nullable = false)
    private String addressLine1;

    /**
     * The address line 2 of the address
     */
    @Column(name = "address_line_2", columnDefinition = "varchar", length = 50, nullable = true)
    private String addressLine2;

    /**
     * The city of the address
     */
    @Column(name = "city", columnDefinition = "varchar", length = 50, nullable = true)
    private String city;

    /**
     * The state of the address
     */
    @Column(name = "state_type_code", columnDefinition = "varchar", length = 50, nullable = true)
    private String stateTypeCode;

    /**
     * FIPS code of the address
     */
    @Column(name = "fips_code", columnDefinition = "varchar", length = 20, nullable = true)
    private String fipsCode;

    /**
     * Zip code of the address
     */
    @Column(name = "zip_code", columnDefinition = "varchar", length = 20, nullable = true)
    private String zipCode;

    /**
     * The county code of the address
     */
    @Column(name = "county_code", columnDefinition = "varchar", length = 20, nullable = true)
    private String countyCode;

    /**
     * The zeus transaction control number of the transaction that created the address
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * Start date of the address
     */
    @Column(name = "start_date", columnDefinition = "datetime", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the address
     */
    @Column(name = "end_date", columnDefinition = "datetime", nullable = true)
    private LocalDate endDate;

    /**
     * Identifies if the member address was updated
     */
    @Column(name = "changed", columnDefinition = "boolean", nullable = false)
    private boolean changed;

    /**
     * Date when the record was created
     */
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Date and time when the record was updated
     */
    @CreationTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
