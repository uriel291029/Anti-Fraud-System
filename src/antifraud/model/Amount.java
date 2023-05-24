package antifraud.model;

import antifraud.domain.antifraud.TransactionPolicy;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long amountId;

  private Long maxAmount;

  @Enumerated(EnumType.STRING)
  private TransactionPolicy target;

  private String number;
}
