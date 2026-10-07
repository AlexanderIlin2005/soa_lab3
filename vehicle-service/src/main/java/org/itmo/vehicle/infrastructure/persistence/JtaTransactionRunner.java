package org.itmo.vehicle.infrastructure.persistence;

import org.itmo.vehicle.application.TransactionRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

/**
 * Spring-вариант TransactionRunner. {@code @Transactional} теперь
 * из Spring Framework, а не из Jakarta EE — управление транзакциями
 * идёт через JpaTransactionManager поверх DataSource из JNDI.
 */
@Component
public class JtaTransactionRunner implements TransactionRunner {

    @Override
    @Transactional
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
