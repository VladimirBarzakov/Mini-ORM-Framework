package orm.manager;

import annotations.Column;
import annotations.Entity;
import annotations.Id;
import contracts.DbContext;
import contracts.SchemaInitializationStrategy;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class EntityManager implements DbContext {
    private static final String DATE_FORMAT="yyyy-MM-dd";
    private Connection connection;
    private String dataSource;
    private SchemaInitializationStrategy strategy;

    public EntityManager(Connection connection, String dataSource, SchemaInitializationStrategy strategy) throws SQLException {
        this.connection=connection;
        this.dataSource=dataSource;
        this.strategy=strategy;
        this.strategy.execute();
    }

    @Override
    public<E> boolean persist(E entity) throws IllegalAccessException, SQLException {
        Field primary = this.getId(entity.getClass());
        primary.setAccessible(true);
        Object value = primary.get(entity);

        if (value==null||(Integer)value<=0){
            return this.doInsert(entity);
        }
        return this.doUpdate(entity,primary);
    }

    @Override
    public <E> Iterable<E> find(Class<E> table) throws SQLException, InstantiationException, IllegalAccessException {
        return this.find(table, null);
    }

    public <E> Iterable<E> find(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException {
        Statement stmt = connection.createStatement();
        String query = String.format("SELECT * FROM `%s` WHERE 1 %s ;"
                ,this.getTableName(table)
                ,where!=null? " AND "+where : "");
        ResultSet rs = stmt.executeQuery(query);

        List<E> resultSet = new ArrayList<>();
        while (rs.next()){
            E entity = table.newInstance();
            this.fillEntity(table, rs, entity);
            resultSet.add(entity);
        }
        return resultSet;
    }

    public <E> E findFirst(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException {
        return this.findFirst(table, null);
    }

    public <E> E findFirst(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException {
        Statement stmt = connection.createStatement();
        String query = String.format("SELECT * FROM %s WHERE 1 %s LIMIT 1 ;"
                ,this.getTableName(table)
                ,where!=null? " AND "+where : "");
        ResultSet rs = stmt.executeQuery(query);
        E entity = table.newInstance();
        rs.next();
        this.fillEntity(table, rs, entity);
        return entity;
    }

    public void doDelete(Class<?> table, String criteria) throws Exception {
        String tableName = table.getAnnotation(Entity.class).name();
        if (criteria==null||criteria.equals("")){
            criteria="1";
        }
        String query = "DELETE FROM " + tableName + " WHERE " + criteria;
        this.connection.prepareStatement(query).execute();
    }

    public void doDropTable(Class<?> table) throws Exception {
        String tableName = table.getAnnotation(Entity.class).name();
        String query = "DROP TABLE `" + tableName + "` ;";
        this.connection.prepareStatement(query).execute();
    }

    private<E> boolean doUpdate(E entity, Field primary) throws SQLException, IllegalAccessException {
        primary.setAccessible(true);
        String tableName = this.getTableName(entity.getClass());
        StringBuilder updatePairsStr = new StringBuilder();
        Field[] fields = entity.getClass().getDeclaredFields();

        for (int i = 0; i<fields.length;i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)){
                continue;
            }
            if (field.isAnnotationPresent(Column.class)) {
                String fieldName = this.getFieldName(field);
                String fieldValue = this.getFieldValue(field, entity);
                updatePairsStr.append(String.format(" `%s` = '%s' ",
                        fieldName,
                        fieldValue));
                if (i < fields.length - 1) {
                    updatePairsStr.append(", ");
                }
            }
        }

        String query = String.format("UPDATE `%s` SET %s WHERE `%s` = '%s' ;",
                tableName,
                updatePairsStr,
                primary.getAnnotation(Column.class).name(),
                primary.get(entity).toString() );
        return connection.prepareStatement(query).execute();
    }
    
    private<E> boolean doInsert(E entity) throws SQLException, IllegalAccessException {
        String tableName = this.getTableName(entity.getClass());

        StringBuilder columnNameBuilder = new StringBuilder();
        StringBuilder valuesBuilder = new StringBuilder();
        Field[] fields = entity.getClass().getDeclaredFields();

        for (int i = 0; i<fields.length;i++) {
            Field field = fields[i];
            field.setAccessible(true);
            if (field.isAnnotationPresent(Id.class)){
                continue;
            }
            if (field.isAnnotationPresent(Column.class)) {
                String fieldName = this.getFieldName(field);
                String fieldValue = this.getFieldValue(field, entity);

                columnNameBuilder.append(String.format("`%s`",fieldName));
                valuesBuilder.append(String.format("'%s'",fieldValue));
                if (i < fields.length - 1) {
                    columnNameBuilder.append(", ");
                    valuesBuilder.append(", ");
                }
            }
        }

        String query = String.format("INSERT INTO `%s` ( %s ) VALUES ( %s );",
                tableName,
                columnNameBuilder.toString(),
                valuesBuilder.toString());
        return connection.prepareStatement(query).execute();
    }

    private<E> String getFieldValue(Field entityField, E entity) throws IllegalAccessException {
        String value;
        switch (entityField.getType().getSimpleName()){
            case "Date":
                SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
                value = format.format(entityField.get(entity));
                break;
            default:
                value = entityField.get(entity).toString();
                break;
        }
        return value;
    }

    private<E> void fillEntity(Class<E> table, ResultSet rs, E entity) throws SQLException, IllegalAccessException {
        Field[] fields = table.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            this.fillField(field, entity, rs, field.getAnnotation(Column.class).name());
        }
    }

    private<E> void fillField(Field field, E instance, ResultSet rs, String fieldName) throws SQLException, IllegalAccessException {
        field.setAccessible(true);
        if (field.getType()==int.class||field.getType()==Integer.class){
            field.set(instance, rs.getInt(fieldName));
        } else if(field.getType()==long.class||field.getType()==Long.class){
            field.set(instance, rs.getLong(fieldName));
        } else if(field.getType()==double.class||field.getType()==Double.class){
            field.set(instance,rs.getDouble(fieldName));
        } else if(field.getType()==float.class||field.getType()==Float.class){
            field.set(instance, rs.getFloat(fieldName));
        } else if(field.getType()==short.class||field.getType()==Short.class){
            field.set(instance, rs.getShort(fieldName));
        } else if(field.getType()==byte.class||field.getType()==Byte.class) {
            field.set(instance, rs.getByte(fieldName));
        } else if(field.getType()== BigDecimal.class) {
            field.set(instance, rs.getBigDecimal(fieldName));
        } else if(field.getType()== boolean.class||field.getType()==Boolean.class) {
            field.set(instance, rs.getBoolean(fieldName));
        } else if(field.getType()==String.class){
            field.set(instance, rs.getString(fieldName));
        } else if(field.getType()==Date.class){
            field.set(instance, rs.getDate(fieldName));
        } else if(field.getType().isArray()){
            field.set(instance, rs.getArray(fieldName));
        } else{
            throw new UnsupportedOperationException("Unknown data type");
        }
    }

    private Field getId(Class entity){
        return Arrays.stream(entity.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(()->new UnsupportedOperationException(
                        "Entity does not have primary key"));
    }

    private <E> String getTableName(Class<E> entity) {
        String tableName = "";
        if (entity.isAnnotationPresent(Entity.class)) {
            Entity annotation = entity.getAnnotation(Entity.class);
            tableName = annotation.name();
        }
        if (tableName.equals("")) {
            tableName = entity.getSimpleName();
        }
        return tableName;
    }

    private String getFieldName(Field field) {
        String fieldName = "";

        if (field.isAnnotationPresent(Column.class)) {
            Column columnAnnotation = field.getAnnotation(Column.class);
            fieldName = columnAnnotation.name();
        }

        if (fieldName.equals("")) {
            fieldName = field.getName();
        }

        return fieldName;
    }
}
