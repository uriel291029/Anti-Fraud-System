package antifraud.domain.request;

import antifraud.domain.security.RoleEnum;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

  private String name;

  @NotBlank
  private String username;

  @NotBlank
  private String password;

  private RoleEnum role;
}
