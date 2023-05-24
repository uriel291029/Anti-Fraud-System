package antifraud.model;

import antifraud.domain.security.RoleEnum;
import javax.persistence.Entity;
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
public class User {

  @Id
  @GeneratedValue//(strategy = GenerationType.IDENTITY)
  private Long userId;

  private String name;

  private String username;

  private String password;

  private RoleEnum role;

  private boolean unlock;
}
