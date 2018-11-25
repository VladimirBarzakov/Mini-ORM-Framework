package contracts;

import java.io.IOException;
import java.sql.SQLException;

public interface DbContext{
    <E>boolean persist(E entity) throws IllegalAccessException, SQLException, IOException;
    <E>Iterable<E> find(Class<E> table) throws SQLException, InstantiationException, IllegalAccessException;
    <E>Iterable<E> find(Class<E> table, String where) throws IllegalAccessException, SQLException, InstantiationException;
    <E> E findFirst(Class<E> table) throws IllegalAccessException, SQLException, InstantiationException;
    <E> E findFirst(Class<E> table, String where) throws SQLException, IllegalAccessException, InstantiationException;

}
