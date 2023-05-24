package antifraud.service;

import antifraud.domain.antifraud.TransactionInfo;
import antifraud.domain.antifraud.TransactionPolicy;
import antifraud.domain.exception.ConflictException;
import antifraud.domain.exception.NotFoundException;
import antifraud.domain.request.StolenCardRequest;
import antifraud.domain.request.SuspiciousIpRequest;
import antifraud.domain.request.TransactionRequest;
import antifraud.domain.response.StolenCardResponse;
import antifraud.domain.response.SuspiciousIpResponse;
import antifraud.domain.response.TransactionOperationResponse;
import antifraud.model.Amount;
import antifraud.model.Region;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIp;
import antifraud.model.Transaction;
import antifraud.repository.AmountRepository;
import antifraud.repository.RegionRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import antifraud.service.transaction.TransactionVerificationService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AntiFraudService {

  private static final String AMOUNT = "amount";
  private static final String CARD_NUMBER = "card-number";
  private static final String NONE = "none";
  private static final String IP = "ip";

  private static final Integer ALLOWED_AMOUNT = 200;

  private final SuspiciousIpRepository suspiciousIpRepository;

  private final StolenCardRepository stolenCardRepository;

  private final TransactionRepository transactionRepository;

  private final RegionRepository regionRepository;

  private final TransactionVerificationService transactionVerificationService;

  private final AmountRepository amountRepository;

  public TransactionOperationResponse processTransaction(TransactionRequest transactionRequest) {
    List<String> reasons = new ArrayList<>();
    long amount = transactionRequest.getAmount();
    TransactionPolicy transactionPolicy;
    Map<TransactionPolicy, Long> amountMap = getAmountMap(transactionRequest);
    TransactionOperationResponse.TransactionOperationResponseBuilder transactionResponseBuilder = TransactionOperationResponse.builder();
    if (amount <= amountMap.get(TransactionPolicy.ALLOWED)) {
      log.info("Processing the transaction with amount less than or equal {} request : {}",
          amountMap.get(TransactionPolicy.ALLOWED), transactionRequest);
      transactionResponseBuilder.result(TransactionPolicy.ALLOWED.toString());
      transactionPolicy = TransactionPolicy.ALLOWED;
    } else if (amount <= amountMap.get(TransactionPolicy.MANUAL_PROCESSING)) {
      log.info("Processing the transaction with amount less than or equal {} request : {}",
          amountMap.get(TransactionPolicy.MANUAL_PROCESSING), transactionRequest);
      transactionResponseBuilder.result(TransactionPolicy.MANUAL_PROCESSING.toString());
      reasons.add(AMOUNT);
      transactionPolicy = TransactionPolicy.MANUAL_PROCESSING;
    } else {
      log.info("Processing the transaction with amount greater than 1500 request : {}",
          transactionRequest);
      transactionResponseBuilder.result(TransactionPolicy.PROHIBITED.toString());
      transactionPolicy = TransactionPolicy.PROHIBITED;
      reasons.add(AMOUNT);
    }

    log.info("Starting the validation there is a record with the ip : {}",
        transactionRequest.getIp());
    boolean existsByIp = suspiciousIpRepository.existsByIp(transactionRequest.getIp());
    log.info("Result the existence of a record with ip : {} result : {}",
        transactionRequest.getIp(),
        existsByIp);
    log.info("Starting the validation there is a record with the card number : {}",
        transactionRequest.getNumber());
    boolean existsByNumber = stolenCardRepository.existsByNumber(transactionRequest.getNumber());
    if (existsByNumber) {
      transactionResponseBuilder.result(TransactionPolicy.PROHIBITED.toString());
      if (transactionPolicy.equals(TransactionPolicy.MANUAL_PROCESSING)) {
        reasons.remove(AMOUNT);
      }
      transactionPolicy = TransactionPolicy.PROHIBITED;
      reasons.add(CARD_NUMBER);
    }

    if (existsByIp) {
      transactionResponseBuilder.result(TransactionPolicy.PROHIBITED.toString());
      if (transactionPolicy.equals(TransactionPolicy.MANUAL_PROCESSING)) {
        reasons.remove(AMOUNT);
      }
      log.info("Adding the reason IP with the card number : {} and ip : {}",
          transactionRequest.getNumber(), transactionRequest.getIp());
      transactionPolicy = TransactionPolicy.PROHIBITED;
      reasons.add(IP);
    }

    log.info("Starting the verification of the region with the card number: {}",
        transactionRequest.getNumber());
    TransactionInfo transactionInfo = transactionVerificationService.verifyTransactionRegion(
        transactionRequest);
    if (Objects.nonNull(transactionInfo.getTransactionPolicy())) {
      transactionResponseBuilder.result(transactionInfo.getTransactionPolicy().toString());
      reasons.add(transactionInfo.getInfo());
      transactionPolicy = transactionInfo.getTransactionPolicy();
    }

    transactionInfo = transactionVerificationService.verifyTransactionIp(transactionRequest);
    if (Objects.nonNull(transactionInfo.getTransactionPolicy())) {
      transactionResponseBuilder.result(transactionInfo.getTransactionPolicy().toString());
      reasons.add(transactionInfo.getInfo());
      transactionPolicy = transactionInfo.getTransactionPolicy();
    }

    Collections.sort(reasons);
    if (reasons.isEmpty()) {
      reasons.add("none");
    }
    String info = String.join(", ", reasons);
    log.info("Retrieving the region : {} in the database.", transactionRequest.getRegion());
    Optional<Region> optionalRegion = regionRepository.findByCode(transactionRequest.getRegion());
    log.info("Retrieved region : {}", optionalRegion.get());

    Transaction transaction = Transaction.builder()
        .number(transactionRequest.getNumber())
        .amount(transactionRequest.getAmount())
        .date(transactionRequest.getDate())
        .region(optionalRegion.get())
        .ip(transactionRequest.getIp())
        .result(transactionPolicy)
        .build();
    log.info("Saving the transaction : {} in the database.", transaction);
    //if(transactionPolicy != TransactionPolicy.ALLOWED)
    transactionRepository.save(transaction);
    log.info("Saved successfully the transaction : {} in the database.", transaction);

    return transactionResponseBuilder.info(info).build();
  }

  public SuspiciousIpResponse createSuspiciousIp(SuspiciousIpRequest suspiciousIpRequest) {
    log.info("Starting the process to create the suspicious ip with the request : {}",
        suspiciousIpRequest);
    boolean existsByIp = suspiciousIpRepository.existsByIp(suspiciousIpRequest.getIp());
    if (existsByIp) {
      log.error("This ip is already in database");
      throw new ConflictException("This ip is already in database");
    }
    SuspiciousIp suspiciousIp = SuspiciousIp.builder()
        .ip(suspiciousIpRequest.getIp()).build();
    suspiciousIpRepository.save(suspiciousIp);
    log.error("Saving the suspicious ip successfully {}", suspiciousIp);
    return SuspiciousIpResponse.builder().id(suspiciousIp.getSuspiciousIpId())
        .ip(suspiciousIp.getIp()).build();
  }

  public List<SuspiciousIpResponse> getSuspiciousIps() {
    log.info("Retrieving the list of suspicious ips");
    return suspiciousIpRepository.findAll(Sort.by(Direction.ASC, "suspiciousIpId")).stream()
        .map(suspiciousIp ->
            SuspiciousIpResponse.builder().id(suspiciousIp.getSuspiciousIpId())
                .ip(suspiciousIp.getIp()).build()
        ).collect(Collectors.toList());
  }

  public SuspiciousIpResponse removeSuspiciousIp(String ip) {
    Optional<SuspiciousIp> suspiciousIpOptional = suspiciousIpRepository.findByIp(ip);
    if (suspiciousIpOptional.isEmpty()) {
      throw new NotFoundException("The IP is not found in the database.");
    }
    Long id = suspiciousIpOptional.get().getSuspiciousIpId();
    suspiciousIpRepository.deleteById(id);
    String message = String.format("IP %s successfully removed!", ip);
    return SuspiciousIpResponse.builder().status(message).build();
  }

  public StolenCardResponse createStolenCard(StolenCardRequest stolenCardRequest) {
    log.info("Starting the process to create the stolen card with the request : {}",
        stolenCardRequest);
    boolean existsByNumber = stolenCardRepository.existsByNumber(stolenCardRequest.getNumber());
    if (existsByNumber) {
      log.error("This number is already in database");
      throw new ConflictException("This number is already in database.");
    }
    StolenCard stolenCard = StolenCard.builder()
        .number(stolenCardRequest.getNumber())
        .build();
    stolenCardRepository.save(stolenCard);
    log.error("Saving the stolen card successfully {}", stolenCard);
    return StolenCardResponse.builder()
        .id(stolenCard.getStolenCardId())
        .number(stolenCard.getNumber())
        .build();
  }

  public List<StolenCardResponse> getStolenCards() {
    log.info("Retrieving the list of suspicious ips.");
    return stolenCardRepository.findAll(Sort.by(Direction.ASC, "stolenCardId")).stream()
        .map(stolenCard ->
            StolenCardResponse.builder()
                .id(stolenCard.getStolenCardId())
                .number(stolenCard.getNumber())
                .build())
        .collect(Collectors.toList());
  }

  public StolenCardResponse removeStolenCard(String number) {
    Optional<StolenCard> stolenCardOptional = stolenCardRepository.findByNumber(number);
    if (stolenCardOptional.isEmpty()) {
      throw new NotFoundException("The card number is not found in the database.");
    }
    Long id = stolenCardOptional.get().getStolenCardId();
    stolenCardRepository.deleteById(id);
    String message = String.format("Card %s successfully removed!", number);
    return StolenCardResponse.builder().status(message).build();
  }

  private Map<TransactionPolicy, Long> getAmountMap(TransactionRequest transactionRequest) {
    List<Amount> amounts = amountRepository.findByNumber(transactionRequest.getNumber());
    if (amounts.isEmpty()) {
      amounts = new ArrayList<>();
      amounts.add(Amount.builder().target(TransactionPolicy.ALLOWED)
          .maxAmount(200L).build());
      amounts.add(Amount.builder().target(TransactionPolicy.MANUAL_PROCESSING)
          .maxAmount(1500L).build());
    } else if (!containsAmount(amounts, TransactionPolicy.ALLOWED)) {
      amounts = new ArrayList<>(amounts);
      amounts.add(Amount.builder().target(TransactionPolicy.ALLOWED)
          .maxAmount(200L).build());
    } else if (!containsAmount(amounts, TransactionPolicy.MANUAL_PROCESSING)) {
      amounts = new ArrayList<>(amounts);
      amounts.add(Amount.builder().target(TransactionPolicy.MANUAL_PROCESSING)
          .maxAmount(1500L).build());
    }

    amounts.forEach(amount -> log.info("The amount is : {}", amount));
    return amounts.stream().collect(Collectors.toMap(
        Amount::getTarget, Amount::getMaxAmount));
  }

  private boolean containsAmount(List<Amount> amounts, TransactionPolicy transactionPolicy) {
    return amounts.stream().anyMatch(amount -> amount.getTarget().equals(transactionPolicy));
  }
}
