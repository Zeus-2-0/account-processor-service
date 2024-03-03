package com.brihaspathee.zeus.domain.repository;

import com.brihaspathee.zeus.domain.entity.ProcessingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 14, January 2024
 * Time: 8:44 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.domain.repository
 * To change this template use File | Settings | File and Code Template
 */
@Repository
public interface ProcessingRequestRepository extends JpaRepository<ProcessingRequest, UUID> {

    /**
     * Find processing request by ZRCN
     * @param zrcn
     * @return
     */
    Optional<ProcessingRequest> findByZrcn(String zrcn);
}
