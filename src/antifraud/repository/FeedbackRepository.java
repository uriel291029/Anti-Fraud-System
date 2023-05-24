package antifraud.repository;

import antifraud.domain.antifraud.TransactionPolicy;
import antifraud.model.Feedback;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  List<Feedback> findByValidityAndFeedback(TransactionPolicy validity, TransactionPolicy feedback);
}
