package orm.em_builder;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import contracts.TableCreator;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseTableCreator implements TableCreator {
    private Connection connection;

    public DatabaseTableCreator(Connection connection) {
        this.connection = connection;
    }

    public void doCreate(Class entity) throws SQLException {
        String tableName = this.getTableName(entity);
        StringBuilder builder = new StringBuilder();
        Field[] fields = entity.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field currentField = fields[i];
            builder.append(String.format("`%s` %s ",
                    this.getFieldName(currentField),
                    this.getDatabaseType(currentField)));
            if (currentField.isAnnotationPresent(Id.class)){
                builder.append(" PRIMARY KEY AUTO_INCREMENT");
            }
            if (i<fields.length-1){
                builder.append(", ");
            }
        }
        String query = String.format("CREATE TABLE `%s` ( %s ) ;",tableName,builder.toString());
        this.connection.prepareStatement(query).execute();
    }

    @Override
    public String getFieldName(Field field) {
        String fieldName = "";
        if (field.isAnnotationPresent(Column.class)){
            fieldName=field.getAnnotation(Column.class).name();
        }
        if (fieldName.equals("")){
            fieldName=field.getName();
        }
        return fieldName;
    }

    @Override
    public String getTableName(Class<?> entity) {
        String tableName = "";
        if (entity.isAnnotationPresent(Entity.class)){
            tableName= entity.getAnnotation(Entity.class).name();
        }
        if (tableName.equals("")){
            tableName=entity.getSimpleName();
        }
        return tableName;
    }

    @Override
    public String getDatabaseType(Field field) {
        String mySQLType = "";
        switch (field.getType().getSimpleName()){
            case "int":
            case "Integer":
                mySQLType="INT";
                break;
            case "byte":
            case "Byte":
                mySQLType="TINYINT";
                break;
            case "short":
            case "Short":
                mySQLType="SMALLINT";
                break;
            case "long":
            case "Long":
                mySQLType="BIGINT";
                break;
            case "String":
                mySQLType="VARCHAR(50)";
                break;
            case "Date":
                mySQLType="DATETIME";
                break;
        }
        return mySQLType;
    }
}
