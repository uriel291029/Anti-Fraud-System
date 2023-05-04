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
import antifraud.domain.response.TransactionResponse;
import antifraud.model.Region;
import antifraud.model.StolenCard;
import antifraud.model.SuspiciousIp;
import antifraud.model.Transaction;
import antifraud.repository.RegionRepository;
import antifraud.repository.StolenCardRepository;
import antifraud.repository.SuspiciousIpRepository;
import antifraud.repository.TransactionRepository;
import antifraud.service.transaction.TransactionVerificationService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  public TransactionResponse processTransaction(TransactionRequest transactionRequest) {
    List<String> reasons = new ArrayList<>();
    long amount = transactionRequest.getAmount();
    TransactionPolicy transactionPolicy;
    //LocalDateTime localDateTime = LocalDateTime.now();
    //transactionRequest.setLocalDateTime(localDateTime);

    TransactionResponse.TransactionResponseBuilder transactionResponseBuilder = TransactionResponse.builder();
    if (amount <= ALLOWED_AMOUNT) {
      log.info("Processing the transaction with amount less than or equal 200 request : {}",
          transactionRequest);
      transactionResponseBuilder.result(TransactionPolicy.ALLOWED.toString());
      transactionPolicy = TransactionPolicy.ALLOWED;
      //reasons.add("none");
    } else if (amount <= 1500) {
      log.info("Processing the transaction with amount less than or equal 1500 request : {}",
          transactionRequest);
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

    log.info("Starting the validation there is a record with the ip : {}", transactionRequest.getIp());
    boolean existsByIp = suspiciousIpRepository.existsByIp(transactionRequest.getIp());
    log.info("Result the existence of a record with ip : {} result : {}", transactionRequest.getIp(),
        existsByIp);
    log.info("Starting the validation there is a record with the card number : {}", transactionRequest.getNumber());
    boolean existsByNumber = stolenCardRepository.existsByNumber(transactionRequest.getNumber());
    if (existsByNumber) {
      transactionResponseBuilder.result(TransactionPolicy.PROHIBITED.toString());
      if(transactionPolicy.equals(TransactionPolicy.MANUAL_PROCESSING)){
        reasons.remove(AMOUNT);
      }
      transactionPolicy = TransactionPolicy.PROHIBITED;
      reasons.add(CARD_NUMBER);
    }

    if (existsByIp) {
      transactionResponseBuilder.result(TransactionPolicy.PROHIBITED.toString());
      if(transactionPolicy.equals(TransactionPolicy.MANUAL_PROCESSING)){
        reasons.remove(AMOUNT);
      }
      reasons.add(IP);
    }

    log.info("Starting the verification of the region");
    TransactionInfo transactionInfo = transactionVerificationService.verifyTransactionRegion(transactionRequest);
    if(Objects.nonNull(transactionInfo.getTransactionPolicy())){
      transactionResponseBuilder.result(transactionInfo.getTransactionPolicy().toString());
      reasons.add(transactionInfo.getInfo());
    }

    transactionInfo = transactionVerificationService.verifyTransactionIp(transactionRequest);
    if(Objects.nonNull(transactionInfo.getTransactionPolicy())){
      transactionResponseBuilder.result(transactionInfo.getTransactionPolicy().toString());
      reasons.add(transactionInfo.getInfo());
    }


    Collections.sort(reasons);
    if(reasons.isEmpty()){
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
        .build();
    log.info("Saving the transaction : {} in the database.", transaction);
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
      throw new ConflictException("This number is already in database");
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
    log.info("Retrieving the list of suspicious ips");
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
}
