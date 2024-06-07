package br.com.tiago.picpay.transaction;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.tiago.picpay.authorization.AuthorizerService;
import br.com.tiago.picpay.exception.InvalidTransactionException;
import br.com.tiago.picpay.wallet.WalletRepository;
import br.com.tiago.picpay.wallet.WalletType;
import br.com.tiago.picpay.notification.NotificationService;

@Service
public class TransactionService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionService.class);
  private final TransactionRepository transactionRepository;
  private final WalletRepository walletRepository;
  private final AuthorizerService authorizerService;
  private final NotificationService notificationService;

  public TransactionService(TransactionRepository transactionRepository,
      WalletRepository walletRepository, AuthorizerService authorizerService,
      NotificationService notificationService) {
    this.transactionRepository = transactionRepository;
    this.walletRepository = walletRepository;
    this.authorizerService = authorizerService;
    this.notificationService = notificationService;
  }

  @Transactional
  public Transaction create(Transaction transaction) {
    validate(transaction);

    var newTransaction = transactionRepository.save(transaction);

    var walletPayer = walletRepository.findById(transaction.payer()).get();
    var walletPayee = walletRepository.findById(transaction.payee()).get();
    walletRepository.save(walletPayer.debit(transaction.value()));
    walletRepository.save(walletPayee.credit(transaction.value()));

    authorizerService.authorize(transaction);
    notificationService.notify(newTransaction);

    return newTransaction;
  }

  
  private void validate(Transaction transaction) {
    LOGGER.info("validating transaction {}...", transaction);

    walletRepository.findById(transaction.payee())
        .map(payee -> walletRepository.findById(transaction.payer())
            .map(
                payer -> payer.type() == WalletType.COMUM.getValue() &&
                    payer.balance().compareTo(transaction.value()) >= 0 &&
                    !payer.id().equals(transaction.payee()) ? true : null)
            .orElseThrow(() -> new InvalidTransactionException(
                "Invalid transaction - " + transaction)))
        .orElseThrow(() -> new InvalidTransactionException(
            "Invalid transaction - " + transaction));
  }

  public List<Transaction> list() {
    return transactionRepository.findAll();
  }
}