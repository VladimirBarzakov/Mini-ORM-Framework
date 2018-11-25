package orm.em_builder;

import contracts.SchemaInitializationStrategy;
import orm.entity_scanner.ClassEntityScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class StrategyConfigurer {
    private EntityManagerBuilder builder;

    StrategyConfigurer(EntityManagerBuilder builder) {
        this.builder=builder;
    }

    public <T extends SchemaInitializationStrategy> EntityManagerBuilder set(Class<T> strategyClass) throws
            IllegalAccessException, InvocationTargetException, InstantiationException {

        Constructor<SchemaInitializationStrategy> constructor =
                (Constructor<SchemaInitializationStrategy>) strategyClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);

        SchemaInitializationStrategy strategy = constructor.newInstance(
                new ClassEntityScanner(),
                new DatabaseTableCreator(this.builder.getConnection()),
                this.builder.getConnection(),
                this.builder.getDataSource());

        this.builder.setStrategy(strategy);

        return this.builder;
    }
}
