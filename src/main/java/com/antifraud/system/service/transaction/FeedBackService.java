package com.antifraud.system.service.transaction;

import com.antifraud.system.domain.antifraud.TransactionPolicy;
import com.antifraud.system.domain.exception.ConflictException;
import com.antifraud.system.domain.exception.NotFoundException;
import com.antifraud.system.domain.exception.UnprocessableException;
import com.antifraud.system.domain.request.TransactionRequestPut;
import com.antifraud.system.domain.response.TransactionInfoResponse;
import com.antifraud.system.model.Amount;
import com.antifraud.system.model.Feedback;
import com.antifraud.system.model.Transaction;
import com.antifraud.system.repository.AmountRepository;
import com.antifraud.system.repository.FeedbackRepository;
import com.antifraud.system.repository.TransactionRepository;
import com.antifraud.system.utils.FeedbackUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedBackService {

  private final TransactionRepository transactionRepository;

  private final FeedbackRepository feedbackRepository;

  private final AmountRepository amountRepository;

  public TransactionInfoResponse processFeedback(TransactionRequestPut transactionRequestPut) {
    Optional<Transaction> transactionOptional = transactionRepository.findById(
        transactionRequestPut.getTransactionId());
    if (transactionOptional.isEmpty()) {
      log.error("The transaction is not found in history");
      throw new NotFoundException("The transaction is not found in history");
    }
    Transaction transaction = transactionOptional.get();

    log.info("Validating if there is feedback in the transaction");
    if (Objects.nonNull(transaction.getFeedback())) {
      log.error("The feedback for this specified transaction is already in the database.");
      throw new ConflictException(
          "The feedback for this specified transaction is already in the database.");
    }
    TransactionPolicy feedback = transactionRequestPut.getFeedback();
    if (transaction.getResult().equals(feedback)) {
      log.info("The feedback throws an Exception following the table.");
      throw new UnprocessableException("The feedback throws an Exception following the table.");
    }
    log.info("Retrieving the feedbacks by validity : {} and feedback : {}.",
        transaction.getResult(),
        feedback);
    List<Feedback> feedbacks = feedbackRepository.findByValidityAndFeedback(transaction.getResult(),
        feedback);
    processTransactionFeedbacks(feedbacks, transaction);
    transaction.setFeedback(transactionRequestPut.getFeedback());
    transactionRepository.save(transaction);
    log.info("Updating the transaction with the feedback : {}.", transaction);
    return TransactionInfoResponse.builder()
        .transactionId(transaction.getTransactionId())
        .amount(transaction.getAmount())
        .ip(transaction.getIp())
        .number(transaction.getNumber())
        .region(transaction.getRegion().getCode())
        .date(transaction.getDate())
        .result(transaction.getResult())
        .feedback(Objects.nonNull(transaction.getFeedback()) ? transaction.getFeedback().name()
            : Strings.EMPTY)
        .build();
  }


  private void processTransactionFeedbacks(List<Feedback> feedbacks,
      Transaction transaction) {
    log.info("Updating the limits of the amounts.");
    Map<TransactionPolicy, Amount> amountMap = getAmountMap(transaction);
    for (Feedback feedback : feedbacks) {
      long newLimit;
      if (feedback.isIncrease()) {
        newLimit = FeedbackUtils.increaseLimit(amountMap.get(feedback.getTarget()).getMaxAmount(),
            transaction.getAmount());
      } else {
        newLimit = FeedbackUtils.decreaseLimit(amountMap.get(feedback.getTarget()).getMaxAmount(),
            transaction.getAmount());
      }
      Amount amount = amountMap.get(feedback.getTarget());
      amount.setMaxAmount(newLimit);
      amountRepository.save(amount);
      log.info("Amount saved successfully : {}", amount);
    }
  }

  private Map<TransactionPolicy, Amount> getAmountMap(Transaction transaction) {
    List<Amount> amounts = amountRepository.findByNumber(transaction.getNumber());
    if (amounts.isEmpty()) {
      amounts = new ArrayList<>();
      amounts.add(Amount.builder().target(TransactionPolicy.ALLOWED)
          .number(transaction.getNumber())
          .maxAmount(200L).build());
      amounts.add(Amount.builder().target(TransactionPolicy.MANUAL_PROCESSING)
          .number(transaction.getNumber())
          .maxAmount(1500L).build());
    } else if (!containsAmount(amounts, TransactionPolicy.ALLOWED)) {
      amounts = new ArrayList<>(amounts);
      amounts.add(Amount.builder().target(TransactionPolicy.ALLOWED)
          .number(transaction.getNumber())
          .maxAmount(200L).build());
    } else if (!containsAmount(amounts, TransactionPolicy.MANUAL_PROCESSING)) {
      amounts = new ArrayList<>(amounts);
      amounts.add(Amount.builder().target(TransactionPolicy.MANUAL_PROCESSING)
          .number(transaction.getNumber())
          .maxAmount(1500L).build());
    }

    amounts.forEach(amount -> log.info("The amounts of the transaction : {}", amount));
    return amounts.stream().collect(Collectors.toMap(
        Amount::getTarget, Function.identity()));
  }

  private boolean containsAmount(List<Amount> amounts, TransactionPolicy transactionPolicy) {
    return amounts.stream().anyMatch(amount -> amount.getTarget().equals(transactionPolicy));
  }
}
