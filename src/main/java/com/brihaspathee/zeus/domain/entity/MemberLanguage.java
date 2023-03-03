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
 * Date: 21, November 2022
 * Time: 7:42 PM
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
@Table(name = "MEMBER_LANGUAGE")
public class MemberLanguage {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_language_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberLanguageSK;

    /**
     * The key assigned to the language record in MMS
     */
    @Column(name = "member_acct_lang_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    private UUID memberAcctLangSK;

    /**
     * The member to whom the language is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * Unique code assigned to the language
     */
    @Column(name = "member_language_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String memberLanguageCode;

    /**
     * The type of language (spoken, written etc)
     */
    @Column(name = "language_type_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String languageTypeCode;

    /**
     * The ISO language code (ENG, SPA etc)
     */
    @Column(name = "language_code", columnDefinition = "varchar", length = 50, nullable = false)
    private String languageCode;

    /**
     * The start date of the column
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * The end date of the language
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
