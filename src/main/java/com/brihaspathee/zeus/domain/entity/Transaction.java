package com.brihaspathee.zeus.domain.entity;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 20, November 2022
 * Time: 5:30 PM
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
@Table(name = "TRANSACTION")
public class Transaction {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Type(type = "uuid-char")
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "transaction_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID transactionSK;

    /**
     * Unique assigned to the transaction by the transaction origination service
     */
    @Column(name = "ztcn", length = 50, columnDefinition = "varchar", nullable = false)
    private String ztcn;

    /**
     * The file control number of the file that the transaction was received
     */
    @Column(name = "zfcn", length = 50, columnDefinition = "varchar", nullable = false)
    private String zfcn;

    /**
     * The date the transaction was received
     */
    @Column(name ="transaction_received_date")
    private LocalDateTime transactionReceivedDate;

    /**
     * The source of the transaction
     */
    @Column(name = "transaction_source_type_code", length = 50, columnDefinition = "varchar", nullable = false)
    private String transactionSourceTypeCode;

    /**
     * The account that was created or matched for the transaction
     */
    @OneToOne(mappedBy = "transaction")
    private Account account;

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
