package orm.strategies;

import contracts.TableCreator;
import orm.entity_scanner.ClassEntityScanner;
import orm.strategies.base_classes.SchemaInitializationStrategyAbstract;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class DropCreateStrategy extends SchemaInitializationStrategyAbstract {

    public DropCreateStrategy(ClassEntityScanner entityScanner,
                              TableCreator creator,
                              Connection connection,
                              String dataSource) {
        super(entityScanner, creator, connection, dataSource);
    }

    @Override
    public void execute() throws SQLException {
        String query = String.format("DROP DATABASE IF EXISTS `%s` ;",super.dataSource);
        this.connection.prepareStatement(query).executeUpdate();
        query= String.format("CREATE DATABASE `%s` ;",super.dataSource);
        this.connection.prepareStatement(query).execute();
        this.connection.setCatalog(super.dataSource);
        this.createTables(super.scanForEntities());
    }

    private void createTables(Map<String, Class> entities) throws SQLException {
        for (Class entity : entities.values()) {
            this.creator.doCreate(entity);
        }
    }
}
