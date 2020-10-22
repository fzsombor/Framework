package hu.fzsombor.backend.action;

import hu.fzsombor.Main;
import hu.fzsombor.connector.HDFSConnector;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.time.Duration;
import java.time.Instant;


public class HDFSAction {
    private String host;
    private String user;
    private String password;
    private String filePath;
    private String destinationPath;

    public HDFSAction(DocumentTraversal traversal, Node n, int duration, int size, String format) {
        System.out.println("====Creating HDFS action====");
        NodeIterator iter = traversal.createNodeIterator(
                n, NodeFilter.SHOW_ELEMENT, null, false);
        iter.nextNode();
        for (Node node = iter.nextNode(); node != null; node = iter.nextNode()) {
            if (node.getNodeName() != null) {
                String name = node.getNodeName();
                String text = node.getTextContent().trim();
                text = text.replaceAll("$scale", String.valueOf(size));
                text = text.replaceAll("$format", format);

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
                    case "file":
                        filePath = text;
                        break;
                    case "destination":
                        destinationPath = text;
                    default:
                        System.err.println("Wrong syntax for HDFS action");
                        System.exit(10);
                }

                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }
    }

    public void executeAction(String id) {
        HDFSConnector hdfsConnector = new HDFSConnector();
        Instant start = Instant.now();
        /*=======TIMER START=======*/
        try {
            hdfsConnector.copyToHDFS(user, password, host, filePath, destinationPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*========TIMER END========*/
        Instant end = Instant.now();
        Main.DB.runQuery("insert into action_runs(workload_run_id, `action`, command ,duration, created_at, updated_at) VALUES('" +
                id + "', " +
                "'HDFS Put', '" +
                filePath + "', " +
                Duration.between(start, end).toMillis() + ",NOW(), NOW());");
    }
}