package orm.strategies;

import annotations.Column;
import contracts.TableCreator;
import orm.entity_scanner.ClassEntityScanner;
import orm.strategies.base_classes.SchemaInitializationStrategyAbstract;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UpdateStrategy extends SchemaInitializationStrategyAbstract {

    public UpdateStrategy(ClassEntityScanner entityScanner,
                          TableCreator creator,
                          Connection connection,
                          String dataSource) {
        super(entityScanner, creator, connection, dataSource);
    }

    @Override
    public void execute() throws SQLException {
        Set<String> databaseTables = this.getDatabaseTables();
        Map<String, Class> entityClasses = super.scanForEntities();
        for (String entityClassName : entityClasses.keySet()) {
            String tableName = this.creator.getTableName(entityClasses.get(entityClassName));
            if (!databaseTables.contains(tableName)){
                this.creator.doCreate(entityClasses.get(entityClassName));
            }
            this.checkTableFields(entityClasses.get(entityClassName),tableName);
        }
    }

    private Set<String> getDatabaseTables() throws SQLException {
        String query = String.format("SELECT table_name FROM information_schema.tables WHERE table_schema = '%s' ;",
                this.dataSource);
        ResultSet rs = this.connection.prepareStatement(query).executeQuery();
        Set<String> tableNameList = new HashSet<>();
        while (rs.next()){
            tableNameList.add(rs.getString("table_name"));
        }
        return tableNameList;
    }

    private void checkTableFields(Class entity, String tableName) throws SQLException {
        String query = String.format("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '%s' AND TABLE_NAME = '%s'",
                this.dataSource,this.creator.getTableName(entity));
        ResultSet rs = this.connection.prepareStatement(query).executeQuery();
        Set<String> dbTableFieldNames = new HashSet<>();
        while (rs.next()){
            dbTableFieldNames.add(rs.getString("COLUMN_NAME"));
        }
        Field[] entityFields = entity.getDeclaredFields();
        for (Field entityField : entityFields) {
            if (entityField.isAnnotationPresent(Column.class)){
                if (!dbTableFieldNames.contains(entityField.getAnnotation(Column.class).name())){
                    this.addFieldToTable(tableName,entityField);
                }
            }
        }
    }

    private void addFieldToTable(String tableName, Field field) throws SQLException {
        String query = String.format("ALTER TABLE `%s` ADD COLUMN `%s` %s",
                tableName,
                field.getAnnotation(Column.class).name(),
                this.creator.getDatabaseType(field));
        this.connection.prepareStatement(query).execute();
    }

    private boolean checkIfTableExists(String tableName) throws SQLException {
        String query = String.format("SELECT table_name FROM information_schema.tables WHERE table_schema = '%s'" +
                        " AND table_name = '%s' LIMIT 1 ;",
                this.dataSource,tableName);
        ResultSet rs = this.connection.prepareStatement(query).executeQuery();
        return rs.first();
    }
}
