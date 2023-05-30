package com.antifraud.system.repository;

import com.antifraud.system.model.StolenCard;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StolenCardRepository extends JpaRepository<StolenCard, Long> {

  boolean existsByNumber(String number);

  Optional<StolenCard> findByNumber(String number);
}
