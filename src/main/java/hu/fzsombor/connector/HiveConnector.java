package hu.fzsombor.connector;

import java.sql.*;

public class HiveConnector {
    private final static String DRIVER_CLASS = "com.cloudera.hive.jdbc.HS2Driver";
    Connection con;

    public void createConnection(String host) {
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
        {
            try {
                con = DriverManager.getConnection("jdbc:hive2://" + host + ":10000/default;AuthMech=0;transportMode=binary;");
                Statement stmt = con.createStatement();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
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
