package com.antifraud.system.domain.request;

import com.antifraud.system.domain.antifraud.TransactionPolicy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestPut {

  private Long transactionId;

  private TransactionPolicy feedback;
}
