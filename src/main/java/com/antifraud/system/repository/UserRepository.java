package com.antifraud.system.repository;

import com.antifraud.system.domain.security.RoleEnum;
import com.antifraud.system.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);

  boolean existsByRole(RoleEnum roleEnum);

  Optional<User> findByUsername(String username);

  @Transactional
  void deleteByUsername(String username);
}
