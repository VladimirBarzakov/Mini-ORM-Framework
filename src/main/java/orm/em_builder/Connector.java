package orm.em_builder;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class Connector {
    private EntityManagerBuilder builder;
    private String adapter;
    private String driver;
    private String host;
    private String port;
    private String user;
    private String pass;



    public Connector(EntityManagerBuilder builder) {
        this.builder=builder;
    }

    public Connector setAdapter(String adapter) {
        this.adapter = adapter;
        return this;
    }

    public Connector setDriver(String driver) {
        this.driver = driver;
        return this;
    }

    public Connector setHost(String host) {
        this.host = host;
        return this;
    }

    public Connector setPort(String port) {
        this.port = port;
        return this;
    }

    public Connector setUser(String user) {
        this.user = user;
        return this;
    }

    public Connector setPass(String pass) {
        this.pass = pass;
        return this;
    }

    public EntityManagerBuilder createConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", this.user);
        props.setProperty("password", this.pass);

        this.builder.setConnection(DriverManager.getConnection(String.format("%s:%s://%s:%s?useSSL=false",
                this.adapter,this.driver,this.host,this.port),props));
        String query = String.format("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '%s'",
                this.builder.getDataSource());
        ResultSet rs = this.builder.getConnection().prepareStatement(query).executeQuery();

        if (!rs.first()){
            query=String.format("create database `%s` ;",this.builder.getDataSource());
            this.builder.getConnection().prepareStatement(query).execute();
        }
        this.builder.getConnection().setCatalog(this.builder.getDataSource());
        //this.builder.getConnection().close();
        //this.builder.setConnection(DriverManager.getConnection(String.format("%s:%s://%s:%s/%s?useSSL=false",
        //        this.adapter,this.driver,this.host,this.port,this.builder.getDataSource()),props));
        return this.builder;
    }
}
