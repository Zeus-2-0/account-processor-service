package com.brihaspathee.zeus.domain.repository;

import com.brihaspathee.zeus.domain.entity.MemberIdentifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 22, November 2022
 * Time: 6:11 AM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.domain.repository
 * To change this template use File | Settings | File and Code Template
 */
@Repository
public interface MemberIdentifierRepository extends JpaRepository<MemberIdentifier, UUID> {
}
