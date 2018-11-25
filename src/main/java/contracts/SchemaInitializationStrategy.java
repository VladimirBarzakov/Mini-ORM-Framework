package contracts;

import java.sql.SQLException;

public interface SchemaInitializationStrategy {
    void execute() throws SQLException;
}
