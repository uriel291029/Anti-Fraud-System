package antifraud.repository;

import antifraud.domain.security.RoleEnum;
import antifraud.model.User;
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
