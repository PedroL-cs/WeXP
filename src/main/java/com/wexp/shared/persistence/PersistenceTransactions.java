package com.wexp.shared.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

@Component
public class PersistenceTransactions {

    private final TransactionTemplate newTransaction;

    public PersistenceTransactions(PlatformTransactionManager transactionManager) {
        newTransaction = new TransactionTemplate(transactionManager);
        newTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    // Callers handle constraint failures only after the failed transaction has rolled back.
    public <T> T insert(Supplier<T> operation) {
        return newTransaction.execute(status -> operation.get());
    }
}
