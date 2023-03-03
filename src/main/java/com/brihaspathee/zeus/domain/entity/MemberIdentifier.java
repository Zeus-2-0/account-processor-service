package com.brihaspathee.zeus.domain.entity;

import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;

import jakarta.persistence.*;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 22, November 2022
 * Time: 6:02 AM
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
@Table(name = "MEMBER_IDENTIFIER")
public class MemberIdentifier {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "member_identifier_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID memberIdentifierSK;

    /**
     * The key assigned to the identifier record in MMS
     */
    @Column(name = "member_acct_identifier_sk", length = 36, columnDefinition = "varchar", nullable = true, updatable = true)
    private UUID memberAcctIdentifierSK;

    /**
     * The member to whom the identifier is associated
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_sk")
    private Member member;

    /**
     * The unqiue code that is assigned to the member
     */
    @Column(name = "member_identifier_code", length = 50, columnDefinition = "varchar", nullable = false, updatable = false)
    private String memberIdentifierCode;

    /**
     * The type of identifier
     */
    @Column(name = "identifier_type_code", length = 50, columnDefinition = "varchar", nullable = false)
    private String identifierTypeCode;

    /**
     * The value of the identifier
     */
    @Column(name = "identifier_value", length = 50, columnDefinition = "varchar", nullable = false)
    private String identifierValue;

    /**
     * Indicates if the identifier is active or not
     */
    @Column(name = "active")
    private boolean active;

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
