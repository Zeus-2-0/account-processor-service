package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 22, November 2022
 * Time: 3:16 PM
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
@Table(name = "MEMBER_PHONE")
public class MemberPhone {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_phone_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberPhoneSK;

    /**
     * The key assigned to the phone record in MMS
     */
    @Column(name = "member_acct_phone_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    private UUID memberAcctPhoneSK;

    /**
     * The member to whom the phone is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * Unique code assigned to the phone
     */
    @Column(name = "member_phone_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String memberPhoneCode;

    /**
     * The type of the phone number
     */
    @Column(name = "phone_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String phoneTypeCode;

    /**
     * The phone number of the member
     */
    @Column(name = "phone_number", columnDefinition = "varchar", length = 50, nullable = false)
    private String phoneNumber;

    /**
     * The zeus transaction control number of the transaction that created the phone number
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * The start date of the phone number
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The end date of the phone number
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Identifies if the member phone was updated
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
