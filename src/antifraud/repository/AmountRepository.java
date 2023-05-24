package antifraud.repository;

import antifraud.model.Amount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmountRepository extends JpaRepository<Amount, Long> {

  List<Amount> findByNumber(String number);
}
