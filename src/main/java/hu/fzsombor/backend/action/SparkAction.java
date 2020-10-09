package hu.fzsombor.backend.action;

import hu.fzsombor.connector.ImpalaConnector;
import hu.fzsombor.connector.SparkConnector;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.util.ArrayList;
import java.util.List;

public class SparkAction {
    private String host;
    private String user;
    private String password;
    private String filePath;
    private String destinationPath;
    private String args;
    private List<String> queries;

    public SparkAction(DocumentTraversal traversal, Node n, int duration, int size, String format) {
        System.out.println("====Creating Spark action====");
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
                    case "user":
                        user = text;
                        break;
                    case "password":
                        password = text;
                        break;
                    case "filename":
                        filePath = text;
                        break;
                    case "args":
                        args = text;
                        break;
                    case "destination":
                        destinationPath = text;
                        break;
                    default:
                        System.err.println("Wrong syntax for Spark action");
                        System.exit(10);
                }

                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }
    }

    public void executeAction() {
        SparkConnector sparkConnector = new SparkConnector();
        sparkConnector.submitToSpark(host, user, password, filePath, destinationPath, args);
    }
}
