package hu.fzsombor.backend.action;

import hu.fzsombor.Main;
import hu.fzsombor.connector.ImpalaConnector;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ImpalaAction {
    private String host;
    private List<String> queries;
    private boolean background = false;

    public ImpalaAction(DocumentTraversal traversal, Node n, int duration, int size, String format) {
        System.out.println("====Creating Impala action====");
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
                        break;
                    case "background":
                        background = Boolean.parseBoolean(text);
                        break;
                    default:
                        System.err.println("Wrong syntax for Impala action");
                        System.exit(10);
                }

                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }
    }

    public void executeAction(String id) {
        ImpalaConnector impalaConnector = new ImpalaConnector();
        impalaConnector.createConnection(host);
        for (String query : queries) {
            Instant start = Instant.now();
            /*=======TIMER START=======*/
            impalaConnector.runQuery(query);
            Instant end = Instant.now();
            /*========TIMER END========*/
            Main.DB.runUpdate("insert into action_runs(workload_run_id, `action`, command ,duration, started, finished) VALUES('" +
                    id + "', " +
                    "'Impala query', '" +
                    query + "', " +
                    Duration.between(start, end).toMillis() + ","+ start + "," + end +");");
        }
        impalaConnector.closeConnection();

    }
    public void executeActionInBackground(String id) {
        Runnable r = new Runnable() {
            public void run() {
                ImpalaConnector impalaConnector = new ImpalaConnector();
                impalaConnector.createConnection(host);
                for (String query : queries) {
                    Instant start = Instant.now();
                    /*=======TIMER START=======*/
                    impalaConnector.runQuery(query);
                    Instant end = Instant.now();
                    /*========TIMER END========*/
                    Main.DB.runUpdate("insert into action_runs(workload_run_id, `action`, command ,duration, started, finished) VALUES('" +
                            id + "', " +
                            "'Impala query', '" +
                            query + "', " +
                            Duration.between(start, end).toMillis() + ","+ start + "," + end +");");
                }
                impalaConnector.closeConnection();
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
