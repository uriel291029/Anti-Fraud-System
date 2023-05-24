package antifraud.domain.response;

import antifraud.domain.antifraud.TransactionPolicy;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInfoResponse {

  private Long transactionId;

  private long amount;

  private String ip;

  private String number;

  private String region;

  private LocalDateTime date;

  private TransactionPolicy result;

  private String feedback;
}
