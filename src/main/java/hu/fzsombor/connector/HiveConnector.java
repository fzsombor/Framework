package hu.fzsombor.connector;
import org.apache.hive.jdbc.HiveDriver;

import java.sql.*;

public class HiveConnector {
    private final static String DRIVER_CLASS = "org.apache.hive.jdbc.HiveDriver";
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
            ResultSet rs = stmt.executeQuery(sqlStatement);

            System.out.println("\n== Begin Query Results ======================");

            // print the results to the console
            while (rs.next()) {
                // the example query returns one String column
                System.out.println(rs.getString(1));
            }

            System.out.println("== End Query Results =======================\n\n");

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
