package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

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
 * Time: 4:11 PM
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
@Table(name = "MEMBER_EMAIL")
public class MemberEmail {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_email_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberEmailSK;

    /**
     * The key assigned to the email record in MMS
     */
    @Column(name = "member_acct_email_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    private UUID memberAcctEmailSK;

    /**
     * The member to whom the email is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * Unique code assigned to the email
     */
    @Column(name = "member_email_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String memberEmailCode;

    /**
     * Identifies the type of email
     */
    @Column(name = "email_type_code", length = 20, columnDefinition = "varchar", nullable = false)
    private String emailTypeCode;

    /**
     * The email of the member
     */
    @Column(name = "email", length = 100, columnDefinition = "varchar", nullable = false)
    private String email;

    /**
     * Identifies if the email is the primary email
     */
    @Column(name="is_primary")
    private boolean isPrimary;

    /**
     * The zeus transaction control number of the transaction that created the email
     */
    @Column(name = "ztcn", length = 20, columnDefinition = "varchar", nullable = true)
    private String ztcn;

    /**
     * The source of the data
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * Start date of the email
     */
    @Column(name = "start_date", columnDefinition = "datetime", nullable = false)
    private LocalDate startDate;

    /**
     * End date of the email
     */
    @Column(name = "end_date", columnDefinition = "datetime", nullable = true)
    private LocalDate endDate;

    /**
     * Identifies if the member email was updated
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
