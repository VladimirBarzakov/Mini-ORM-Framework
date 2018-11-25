package orm.strategies.base_classes;

import contracts.SchemaInitializationStrategy;
import contracts.TableCreator;
import orm.entity_scanner.ClassEntityScanner;

import java.sql.Connection;
import java.util.Map;

public abstract class SchemaInitializationStrategyAbstract implements SchemaInitializationStrategy {
    protected ClassEntityScanner entityScanner;
    protected TableCreator creator;
    protected Connection connection;
    protected String dataSource;

    public SchemaInitializationStrategyAbstract(ClassEntityScanner entityScanner, TableCreator creator, Connection connection, String dataSource) {
        this.entityScanner = entityScanner;
        this.creator = creator;
        this.connection = connection;
        this.dataSource = dataSource;
    }

    protected Map<String, Class> scanForEntities(){
        return this.entityScanner.listFilesForFolder(System.getProperty("user.dir"));
    }
}
