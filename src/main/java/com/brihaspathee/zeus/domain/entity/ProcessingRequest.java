package com.brihaspathee.zeus.domain.entity;

import com.brihaspathee.zeus.domain.ProcessingState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 14, January 2024
 * Time: 8:34 PM
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
@Table(name = "PROCESS_REQUEST")
public class ProcessingRequest {

    /**
     * Primary key of the table
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @JdbcTypeCode(Types.LONGVARCHAR)
    @GenericGenerator(name="UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "process_request_sk", length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    private UUID processRequestSK;

//    @Enumerated(EnumType.STRING)
//    private ProcessingState state;

    /**
     * Identifies the request control number type code
     * E.g. Transaction
     */
    private String zrcnTypeCode;

    /**
     * The request control number
     * E.g. ztcn
     */
    private String zrcn;

    /**
     * The source of the request
     */
    @Column(name = "source", length = 50, columnDefinition = "varchar", nullable = false)
    private String source;

    /**
     * The date when the request was received
     */
    @Column(name ="request_received_date")
    private LocalDateTime requestReceivedDate;

    /**
     * The account that was created or matched for the transaction
     */
    @OneToOne(mappedBy = "processRequest", cascade = CascadeType.REMOVE)
    private Account account;

    /**
     * The id of the payload from which the request was received
     */
    @Column(name = "request_payload_id", length = 45, columnDefinition = "varchar", nullable = true)
    private String requestPayloadId;

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

    @Override
    public String toString() {
        return "ProcessingRequest{" +
                "processRequestSK=" + processRequestSK +
                ", zrcnTypeCode='" + zrcnTypeCode + '\'' +
                ", zrcn='" + zrcn + '\'' +
                ", source='" + source + '\'' +
                ", requestReceivedDate=" + requestReceivedDate +
                ", requestPayloadId='" + requestPayloadId + '\'' +
                '}';
    }
}
