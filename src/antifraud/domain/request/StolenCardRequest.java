package antifraud.domain.request;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StolenCardRequest {

  @NotBlank
  @CreditCardNumber
  private String number;
}
