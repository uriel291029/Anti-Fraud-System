package com.antifraud.system.repository;

import com.antifraud.system.model.SuspiciousIp;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SuspiciousIpRepository extends JpaRepository<SuspiciousIp, Long> {

   boolean existsByIp(String ip);

   Optional<SuspiciousIp> findByIp(String ip);
}
