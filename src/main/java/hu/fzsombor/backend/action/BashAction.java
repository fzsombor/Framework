package hu.fzsombor.backend.action;

import hu.fzsombor.Main;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.DocumentTraversal;
import org.w3c.dom.traversal.NodeFilter;
import org.w3c.dom.traversal.NodeIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BashAction {
    private String path;
    private List<String> commands = new ArrayList<>();

    public BashAction(DocumentTraversal traversal, Node n, int duration, int size, String format) {
        System.out.println("====Creating Bash action====");
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
                    case "path":
                        path = text;
                        break;
                    case "command":
                        commands.add(text);
                        break;
                    default:
                        System.err.println("Wrong syntax for Bash action");
                        System.exit(10);
                }

                if (!text.isEmpty()) {
                    System.out.println(name + ": " + text);
                }
            }
        }
    }

    public void executeAction(String id) {
        try {
            Process process = Runtime.getRuntime().exec("cd " + path);
            printResults(process);

            for (String command : commands) {
                Instant start = Instant.now();
                /*=======TIMER START=======*/
                process = Runtime.getRuntime().exec(command);
                printResults(process);
                /*========TIMER END========*/
                Instant end = Instant.now();
                Main.DB.runQuery("insert into action_runs(workload_run_id, `action`, command ,duration, created_at, updated_at) VALUES('" +
                        id + "', " +
                        "'Bash command', '" +
                        command + "', " +
                        Duration.between(start, end).toMillis() + ",NOW(), NOW());");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void printResults(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = "";
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
