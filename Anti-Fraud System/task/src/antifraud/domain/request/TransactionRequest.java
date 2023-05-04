package antifraud.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

  @Positive
  private long amount;

  @NotBlank
  @Pattern(regexp = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$")
  String ip;

  @NotBlank
  @CreditCardNumber
  String number;

  @NotBlank
  String region;

  //@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
  LocalDateTime date;
}
