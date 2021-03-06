package hu.fzsombor.backend.action;

import hu.fzsombor.Main;
import hu.fzsombor.connector.SSHConnector;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class SSHAction {
    private String host;
    private String user;
    private String password;
    private boolean background = false;
    private List<String> commands;

    public SSHAction(DocumentTraversal traversal, Node n, int duration, int size, String format, int cluster) throws SQLException {
        System.out.println("====Creating SSH action====");
        commands = new ArrayList<>();
        NodeIterator iter = traversal.createNodeIterator(
                n, NodeFilter.SHOW_ELEMENT, null, false);
        iter.nextNode();
        for (Node node = iter.nextNode(); node != null; node = iter.nextNode()) {
            if (node.getNodeName() != null) {
                String name = node.getNodeName();
                String text = node.getTextContent().trim();
                text = text.replaceAll("\\$scale", String.valueOf(size));
                text = text.replaceAll("\\$format", format);

                switch (name) {
                    case "host":
                        host = text;
                        break;
                    case "user":
                        user = text;
                        break;
                    case "password":
                        password = text;
                        break;
                    case "command":
                        commands.add(text);
                        break;
                    case "background":
                        background = Boolean.parseBoolean(text);
                        break;
                    default:
                        System.err.println("Wrong syntax for SSH action");
                        System.exit(10);
                }


                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }

        if (host == null) {
            ResultSet result = Main.DB.runQuery("select * from clusters where id=" + cluster + ";");
            result.next();
            host = result.getString("ssh_host");
            user = result.getString("ssh_user");
            password = result.getString("ssh_password");


        }


    }

    public void executeAction(String id) {

        System.out.println("====Starting SSH action====");
        SSHConnector sshConnector = new SSHConnector(user, password, host, "");
        String errorMessage = sshConnector.connect();

        if (errorMessage != null) {
            System.out.println(errorMessage);
        }

        for (String command : commands) {
            System.out.println("Executing command: "+ command);
            Instant start = Instant.now();
            /*=======TIMER START=======*/
            String result = sshConnector.sendCommand(command);
            System.out.println(result);
            /*========TIMER END========*/
            Instant end = Instant.now();
            Main.DB.runUpdate("insert into action_runs(workload_run_id, `action`, command ,duration, started, finished) VALUES('" +
                    id + "', " +
                    "'SSH command', '" +
                    command + "', " +
                    Duration.between(start, end).toMillis() + ","+ start + "," + end +");");
            System.out.println("Took: " + Duration.between(start, end).toMillis() + "ms");
        }

        sshConnector.close();

    }

    public void executeActionInBackground(String id) {

        Runnable r = new Runnable() {
            public void run() {
                SSHConnector sshConnector = new SSHConnector(user, password, host, "");
                String errorMessage = sshConnector.connect();

                if (errorMessage != null) {
                    System.out.println(errorMessage);
                }

                for (String command : commands) {
                    Instant start = Instant.now();
                    /*=======TIMER START=======*/
                    String result = sshConnector.sendCommand(command);
                    System.out.println(result);
                    /*========TIMER END========*/
                    Instant end = Instant.now();
                    Main.DB.runUpdate("insert into action_runs(workload_run_id, `action`, command ,duration, started, finished) VALUES('" +
                            id + "', " +
                            "'SSH command', '" +
                            command + "', " +
                            Duration.between(start, end).toMillis() + "," + start + "," + end + ");");
                }

                sshConnector.close();
            }
        };
        Thread t = new Thread(r);
        t.start();
        Main.threads.add(t);


    }

    public boolean isBackground() {
        return background;
    }
}
