package hu.fzsombor;

import hu.fzsombor.backend.WorkloadParser;
import org.apache.commons.cli.*;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Main {
    public static class DB {
        private static Connection connection = null;
        private static final String connectionUrl = "jdbc:mysql://localhost:8889/frontend?serverTimezone=UTC";
        private static final String connectionUser = "root";
        private static final String connectionPassword = "root";

        public DB() {
        }

        public static Connection getConnection() {

            if (connection != null) {
                return connection;
            }
            try {

                Class.forName("com.mysql.jdbc.Driver").newInstance();
                connection = DriverManager.getConnection(connectionUrl, connectionUser, connectionPassword);
                return connection;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public static ResultSet runQuery(String sqlStatement) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = getConnection().createStatement();
                rs = stmt.executeQuery(sqlStatement);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return rs;
        }

        public static int runUpdate(String sqlStatement) {
            Statement stmt = null;
            int numUpdated = 0;
            try {
                stmt = getConnection().createStatement();
                 numUpdated= stmt.executeUpdate(sqlStatement);

            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            return numUpdated;
        }
    }

    public static List<Thread> threads = new ArrayList<>();

    public static void main(String[] args) {

        Options options = new Options();

        Option benchmarkIDOpt = new Option("b", "benchmark", true, "Benchmark ID that started the workload");
        benchmarkIDOpt.setRequired(true);
        options.addOption(benchmarkIDOpt);

        Option workloadsOpt = new Option("wl", "workloads", true, "Workloads passed as a list of workload_dir=percentage separated by whitespaces");
        workloadsOpt.setArgs(Option.UNLIMITED_VALUES);
        workloadsOpt.setRequired(true);
        options.addOption(workloadsOpt);

        Option formatOpt = new Option("f", "format", true, "File format. One of the following text, parquet, orc or kudu");
        formatOpt.setRequired(true);
        options.addOption(formatOpt);

        Option sizeOpt = new Option("s", "size", true, "Size of the total space can be used to run the benchmark. Recommended value is 80% of total free HDFS space.");
        sizeOpt.setRequired(true);
        options.addOption(sizeOpt);

        Option durationOpt = new Option("d", "duration", true, "The duration of the benchmark. Workloads will restart if the total elapsed time is lower than the duration. After duration elapsed, no new workloads will start, but the already running ones will finish before exiting");
        options.addOption(durationOpt);

        Option clusterOpt = new Option("c", "cluster", true, "Cluster ID if the credentials and hosts are not in the XML");
        options.addOption(clusterOpt);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String workload = cmd.getOptionValue("wl");
            int size = Integer.parseInt(cmd.getOptionValue("s"));
            int cluster = Integer.parseInt(cmd.getOptionValue("c"));
            int duration;
            if (cmd.getOptionValue("d") != null)
                duration = Integer.parseInt(cmd.getOptionValue("d"));
            else
                duration = -1;
            String format = cmd.getOptionValue("f");
            int benchmark_id = Integer.parseInt(cmd.getOptionValue("b"));
            WorkloadParser workloadParser = new WorkloadParser();
            int percentage = Integer.parseInt(workload.split("=")[1]);
            workload = workload.split("=")[0];
            int elapsed = 0;
            do {
                String id = UUID.randomUUID().toString();
                Instant start = Instant.now();
                size = (int)((double) percentage / 100.00 * (double)size);
                // TODO: read wl from DB
                workloadParser.readAndExecuteWorkload("workloads/" + workload + "/a.xml", size, duration, format, id, cluster);
                Instant end = Instant.now();
                Duration.between(start, end).toMillis();
                DB.runUpdate("INSERT into workload_runs(id, workload_name, benchmarks_id, duration, started, finished) VALUES" +
                        "('" + id + "','" +
                        workload + "', " +
                        benchmark_id + ", " +
                        Duration.between(start, end).toMillis() +
                        ",'"+ start.toString().substring(0, start.toString().length() - 1) + "','" + end.toString().substring(0, end.toString().length() - 1)+ "');");
                for (Thread t : threads) {
                    t.join();
                }
                elapsed += Duration.between(start, end).toMillis() / 1000;

            }while (duration > elapsed);

        } catch (ParseException | InterruptedException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Big data benchmark framework backend", options);
            System.exit(1);
        }
    }
}
