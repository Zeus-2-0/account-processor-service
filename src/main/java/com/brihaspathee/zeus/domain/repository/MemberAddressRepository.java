package com.brihaspathee.zeus.domain.repository;

import com.brihaspathee.zeus.domain.entity.MemberAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Created in Intellij IDEA
 * User: Balaji Varadharajan
 * Date: 23, November 2022
 * Time: 12:52 PM
 * Project: Zeus
 * Package Name: com.brihaspathee.zeus.domain.repository
 * To change this template use File | Settings | File and Code Template
 */
public interface MemberAddressRepository extends JpaRepository<MemberAddress, UUID> {
}
