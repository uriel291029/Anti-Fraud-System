package antifraud.domain.antifraud;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInfo {

  private TransactionPolicy transactionPolicy;

  private String info;
}
