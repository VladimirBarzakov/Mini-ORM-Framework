package orm.em_builder;

import contracts.SchemaInitializationStrategy;
import orm.manager.EntityManager;

import java.sql.Connection;
import java.sql.SQLException;

public class EntityManagerBuilder {
    private Connection connection;
    private String dataSource;
    private SchemaInitializationStrategy strategy;

    public EntityManagerBuilder(String dataSource) {
        this.dataSource = dataSource;
    }

    public EntityManager build() throws SQLException {
        return new EntityManager(this.connection, this.dataSource, this.strategy);
    }

    public Connector configureConnectionString(){
        return new Connector(this);
    }

    public StrategyConfigurer configureCreationType(){
        return new StrategyConfigurer(this);
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public String getDataSource() {
        return this.dataSource;
    }

    public SchemaInitializationStrategy getStrategy() {
        return this.strategy;
    }

    public void setStrategy(SchemaInitializationStrategy strategy) {
        this.strategy = strategy;
    }


}
