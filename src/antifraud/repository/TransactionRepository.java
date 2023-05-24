package antifraud.repository;

import antifraud.model.Transaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  @Query(value = "Select count(distinct r.CODE) from Transaction t inner join Region r on t.REGION_ID = r.REGION_ID where r.CODE != :region and t.DATE between :start and :end", nativeQuery = true)
  long countsDistinctRegionCodeByRegionCodeNotAndDateBetween(String region, LocalDateTime start, LocalDateTime end);

  @Query(value = "Select count(distinct t.IP) from Transaction t where t.IP != :ip and t.DATE between :start and :end"
  , nativeQuery = true)
  long countsDistinctIpByIpNotAndDateBetween(String ip, LocalDateTime start, LocalDateTime end);

  List<Transaction> findByNumber(Sort sort, String number);

  Optional<Transaction> findByNumber(String number);
}
