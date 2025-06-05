package com.ServicePaiement.service;

import com.ServicePaiement.model.Transaction;
import com.ServicePaiement.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction save(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    public Transaction findByTransactionId(String transactionId) {
        return transactionRepository.findByTransactionId(transactionId);
    }
}
