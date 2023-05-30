package com.antifraud.system.service.transaction;

import com.antifraud.system.domain.exception.NotFoundException;
import com.antifraud.system.domain.response.TransactionInfoResponse;
import com.antifraud.system.model.Transaction;
import com.antifraud.system.repository.TransactionRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;

  public List<TransactionInfoResponse> retrieveHistory(String number) {
    log.info("Retrieving the history of the transaction with number : {}", number);
    List<Transaction> transactions;
    if (Objects.nonNull(number)) {
      log.info("Retrieving the history of the transaction with order by id asc and with number : {}", number);
      transactions = transactionRepository.findByNumber(Sort.by(Direction.ASC, "transactionId"), number);
      if(transactions.isEmpty()){
        log.error("The transactions for a specified card number are not found");
        throw new NotFoundException("The transactions for a specified card number are not found");
      }
    } else {
      transactions = transactionRepository.findAll(Sort.by(Direction.ASC, "transactionId"));
    }

    transactions.forEach( transaction -> log.info("The transaction is : {}", transaction));
    return transactions.stream()
        .map(transaction -> TransactionInfoResponse.builder()
            .transactionId(transaction.getTransactionId())
            .amount(transaction.getAmount())
            .ip(transaction.getIp())
            .number(transaction.getNumber())
            .region(transaction.getRegion().getCode())
            .date(transaction.getDate())
            .result(transaction.getResult())
            .feedback(Objects.nonNull(transaction.getFeedback()) ? transaction.getFeedback().name() : Strings.EMPTY)
            .build()).collect(Collectors.toList());
  }
}
