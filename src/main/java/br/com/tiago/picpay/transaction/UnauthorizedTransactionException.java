package br.com.tiago.picpay.transaction;

public class UnauthorizedTransactionException extends RuntimeException {
    public UnauthorizedTransactionException(String message) {
      super(message);
    }
  }
