package hu.fzsombor.connector;

import com.cloudera.impala.jdbc.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ImpalaConnector {

    private final static String DRIVER_CLASS = "com.cloudera.impala.jdbc.DataSource";
    Connection con = null;


    public void createConnection(String impalaDaemon) {

        try {
            Class.forName(DRIVER_CLASS);
            DataSource ds = new com.cloudera.impala.jdbc.DataSource();
            ds.setURL("jdbc:impala://" + impalaDaemon + ":21050");
            con = ds.getConnection();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

    }

    public void runQuery(String sqlStatement) {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.execute(sqlStatement);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }

    public void closeConnection() {
        try {
            con.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
