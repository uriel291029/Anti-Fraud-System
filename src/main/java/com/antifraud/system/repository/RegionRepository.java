package com.antifraud.system.repository;

import com.antifraud.system.model.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

  Optional<Region> findByCode(String code);
}
