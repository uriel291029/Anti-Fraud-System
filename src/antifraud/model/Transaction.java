package antifraud.model;

import antifraud.domain.antifraud.TransactionPolicy;
import java.time.LocalDateTime;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue
  private Long transactionId;

  private Long amount;

  private String number;

  private String ip;

  @ManyToOne
  @JoinColumn(name = "regionId")
  private Region region;

  private LocalDateTime date;

  @Enumerated(EnumType.STRING)
  private TransactionPolicy result;

  @Enumerated(EnumType.STRING)
  private TransactionPolicy feedback;
}
