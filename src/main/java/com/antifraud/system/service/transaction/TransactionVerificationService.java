package com.antifraud.system.service.transaction;

import static com.antifraud.system.domain.antifraud.TransactionPolicy.MANUAL_PROCESSING;
import static com.antifraud.system.domain.antifraud.TransactionPolicy.PROHIBITED;

import com.antifraud.system.domain.antifraud.TransactionInfo;
import com.antifraud.system.domain.request.TransactionRequest;
import com.antifraud.system.repository.TransactionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionVerificationService {

  private static final Integer TRANSACTION_COUNT = 2;

  private static final Long ONE_HOUR = 1L;

  private static final String REGION_CORRELATION = "region-correlation";

  private static final String IP_CORRELATION = "ip-correlation";

  private final TransactionRepository transactionRepository;

  public TransactionInfo verifyTransactionRegion(TransactionRequest transactionRequest) {
    LocalDateTime end = transactionRequest.getDate();
    LocalDateTime start = end.minusHours(ONE_HOUR);
    TransactionInfo.TransactionInfoBuilder transactionInfoBuilder = TransactionInfo.builder();

    log.info("Counting the transactions from more than 2 regions of the world other than the region of the transaction that"
        + " is being verified in the last hour in the transaction history region : {}, start : {}, end : {}",
        transactionRequest.getRegion(), start, end);
    long differentRegionCount = transactionRepository.countsDistinctRegionCodeByRegionCodeNotAndDateBetween(
        transactionRequest.getRegion(), start, end);
    log.info("Count : {} the transactions from more than 2 regions of the world other than the region of the transaction that"
            + " is being verified in the last hour in the transaction history region : {}, start : {}, end : {}",
        differentRegionCount, transactionRequest.getRegion(), start, end);

    if (differentRegionCount > TRANSACTION_COUNT) {
      log.info("There are transactions from more than 2 unique IP addresses other than the IP of the transaction that "
          + "is being verified in the last hour in the transaction history");
      transactionInfoBuilder.transactionPolicy(PROHIBITED);
      transactionInfoBuilder.info(REGION_CORRELATION);
    } else if (differentRegionCount == TRANSACTION_COUNT) {
      log.info("There are transactions from 2 unique IP addresses other than the IP of the transaction "
          + "that is being verified in the last hour in the transaction history");
      transactionInfoBuilder.transactionPolicy(MANUAL_PROCESSING);
      transactionInfoBuilder.info(REGION_CORRELATION);
    }
    return transactionInfoBuilder.build();
  }

  public TransactionInfo verifyTransactionIp(TransactionRequest transactionRequest) {
    LocalDateTime end = transactionRequest.getDate();
    LocalDateTime start = end.minusHours(ONE_HOUR);
    TransactionInfo.TransactionInfoBuilder transactionInfoBuilder = TransactionInfo.builder();

    log.info("Counting transactions from more than 2 unique IP addresses other than the IP of "
        + "the transaction that is being verified in the last hour in the transaction history");
    long differentIpCount = transactionRepository.countsDistinctIpByIpNotAndDateBetween(
        transactionRequest.getIp(), start, end);
    log.info("Count : {} transactions from more than 2 unique IP addresses other than the IP of "
        + "the transaction that is being verified in the last hour in the transaction history",
        differentIpCount);

    if (differentIpCount > TRANSACTION_COUNT) {
      log.info("There are transactions from more than 2 unique IP addresses other than the IP of "
          + "the transaction that is being verified in the last hour in the transaction history");
      transactionInfoBuilder.transactionPolicy(PROHIBITED);
      transactionInfoBuilder.info(IP_CORRELATION);
    } else if (differentIpCount == TRANSACTION_COUNT) {
      log.info("There are transactions from 2 unique IP addresses other than the IP of the transaction "
          + "that is being verified in the last hour in the transaction history");
      transactionInfoBuilder.transactionPolicy(MANUAL_PROCESSING);
      transactionInfoBuilder.info(IP_CORRELATION);
    }
    return transactionInfoBuilder.build();
  }
}
