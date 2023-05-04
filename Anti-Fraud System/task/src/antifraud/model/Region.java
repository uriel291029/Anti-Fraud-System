package antifraud.model;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString.Exclude;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long regionId;

  private String code;

  private String description;

  @Exclude
  @OneToMany(mappedBy = "region")
  private List<Transaction> transactions;
}
