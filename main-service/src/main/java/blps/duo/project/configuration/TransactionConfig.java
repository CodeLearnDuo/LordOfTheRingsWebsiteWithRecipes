package blps.duo.project.configuration;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Configuration
@EnableTransactionManagement
@EnableR2dbcRepositories
public class TransactionConfig {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Bean
    public ReactiveTransactionManager transactionManager() {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public TransactionalOperator requiredTransactionalOperator(ReactiveTransactionManager transactionManager) {

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        return TransactionalOperator.create(transactionManager, definition);

    }

    @Bean
    public TransactionalOperator requiredNewTransactionalOperator(ReactiveTransactionManager transactionManager) {

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);

        return TransactionalOperator.create(transactionManager, definition);
    }

    @Bean
    public TransactionalOperator requiresNewReadCommitedTransactionalOperator(ReactiveTransactionManager transactionManager) {

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);

        return TransactionalOperator.create(transactionManager, definition);
    }

    @Bean
    public TransactionalOperator requiredRepeatableReadTransactionalOperator(ReactiveTransactionManager transactionManager) {

        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        definition.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);

        return TransactionalOperator.create(transactionManager, definition);
    }


}