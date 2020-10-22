package hu.fzsombor.backend.action;

import hu.fzsombor.Main;
import hu.fzsombor.connector.HiveConnector;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HiveAction {
    private String host;
    private List<String> queries;

    public HiveAction(DocumentTraversal traversal, Node n, int duration, int size, String format) {
        System.out.println("====Creating Hive action====");
        queries = new ArrayList<>();
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
                    case "query":
                        queries.add(text);
                    default:
                        System.err.println("Wrong syntax for Hive action");
                        System.exit(10);
                }

                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }
    }

    public void executeAction(String id) {
        HiveConnector hiveConnector = new HiveConnector();
        hiveConnector.createConnection(host);
        for (String query : queries) {
            Instant start = Instant.now();
            /*=======TIMER START=======*/
            hiveConnector.runQuery(query);
            /*========TIMER END========*/
            Instant end = Instant.now();
            Main.DB.runQuery("insert into action_runs(workload_run_id, `action`, command ,duration, created_at, updated_at) VALUES('" +
                    id + "', " +
                    "'Hive query', '" +
                    query + "', " +
                    Duration.between(start, end).toMillis() + ",NOW(), NOW());");
        }
        hiveConnector.closeConnection();

    }
}
