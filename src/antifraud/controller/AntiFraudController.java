package antifraud.controller;

import antifraud.domain.request.StolenCardRequest;
import antifraud.domain.request.SuspiciousIpRequest;
import antifraud.domain.request.TransactionRequest;
import antifraud.domain.request.TransactionRequestPut;
import antifraud.domain.response.StolenCardResponse;
import antifraud.domain.response.SuspiciousIpResponse;
import antifraud.domain.response.TransactionInfoResponse;
import antifraud.domain.response.TransactionOperationResponse;
import antifraud.service.AntiFraudService;
import antifraud.service.transaction.FeedBackService;
import antifraud.service.transaction.TransactionService;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.CreditCardNumber;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@Validated
@RequestMapping("api/antifraud")
public class AntiFraudController {

  private final AntiFraudService antiFraudService;

  private final TransactionService transactionService;

  private final FeedBackService feedBackService;

  @RolesAllowed({"ROLE_MERCHANT"})
  @PostMapping("transaction")
  public TransactionOperationResponse postTransaction(
      @Valid @RequestBody TransactionRequest transactionRequest) {
    return antiFraudService.processTransaction(transactionRequest);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @PostMapping("suspicious-ip")
  public SuspiciousIpResponse createSuspiciousIp(
      @Valid @RequestBody SuspiciousIpRequest suspiciousIpRequest) {
    return antiFraudService.createSuspiciousIp(suspiciousIpRequest);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @GetMapping("suspicious-ip")
  public List<SuspiciousIpResponse> getSuspiciousIps() {
    return antiFraudService.getSuspiciousIps();
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @DeleteMapping("suspicious-ip/{ip}")
  public SuspiciousIpResponse removeSuspiciousIp(@PathVariable(name = "ip")
      @Pattern(regexp = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$") String ip) {
    return antiFraudService.removeSuspiciousIp(ip);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @PostMapping("stolencard")
  public StolenCardResponse stolenCardResponse(
      @Valid @RequestBody StolenCardRequest stolenCardRequest) {
    return antiFraudService.createStolenCard(stolenCardRequest);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @GetMapping("stolencard")
  public List<StolenCardResponse> getStolenCards() {
    return antiFraudService.getStolenCards();
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @DeleteMapping("stolencard/{number}")
  public StolenCardResponse removeStolenCard(@PathVariable(name = "number")
      @CreditCardNumber String number) {
    return antiFraudService.removeStolenCard(number);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @PutMapping("transaction")
  public TransactionInfoResponse putTransaction(
      @Valid @RequestBody TransactionRequestPut transactionRequest) {
    return feedBackService.processFeedback(transactionRequest);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @GetMapping("history/{number}")
  public List<TransactionInfoResponse> retrieveHistoryByNumber(@PathVariable(name = "number")
  @CreditCardNumber String number) {
    return transactionService.retrieveHistory(number);
  }

  @RolesAllowed({"ROLE_SUPPORT"})
  @GetMapping("history")
  public List<TransactionInfoResponse> retrieveHistory() {
    return transactionService.retrieveHistory(null);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  ResponseEntity<String> handleConstraintViolationException(ConstraintViolationException e) {
    return new ResponseEntity<>("not valid due to validation error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
  }
}
