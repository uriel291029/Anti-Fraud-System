package com.antifraud.system.domain.request;

import com.antifraud.system.domain.antifraud.AccessOperation;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessRequest {

  @NotBlank
  private String username;

  private AccessOperation operation;
}
