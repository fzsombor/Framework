package hu.fzsombor.backend;

import hu.fzsombor.backend.action.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.DocumentTraversal;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class WorkloadParser {


    public void readAndExecuteWorkload(String filePath, int size, int duration, String format, String id) {

        try {
            File fXmlFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            DocumentTraversal traversal = (DocumentTraversal) doc;

            doc.getDocumentElement().normalize();
            NodeList workflow = doc.getElementsByTagName("workflow");

            for (Node n = workflow.item(0).getFirstChild().getNextSibling(); n != null; n = n.getNextSibling()) {
                if (n.getNodeName() != "#text") {
                    System.out.println("Command name :" + n.getNodeName());

                    switch (n.getNodeName()) {
                        case "ssh":
                            SSHAction sshAction = new SSHAction(traversal, n, duration,size, format);
                            sshAction.executeAction(id);
                            break;
                        case "scp":
                            SCPAction scpAction = new SCPAction(traversal, n, duration, size, format);
                            scpAction.executeAction(id);
                            break;
                        case "impala":
                            ImpalaAction impalaAction = new ImpalaAction(traversal, n, duration, size, format);
                            impalaAction.executeAction(id);
                            break;
                        case "hive":
                            HiveAction hiveAction = new HiveAction(traversal, n, duration, size, format);
                            hiveAction.executeAction(id);
                            break;
                        case "spark":
                            SparkAction sparkAction = new SparkAction(traversal, n, duration, size, format);
                            sparkAction.executeAction(id);
                            break;
                        case "hdfs":
                            HDFSAction hdfsAction = new HDFSAction(traversal, n, duration, size, format);
                            hdfsAction.executeAction(id);
                            break;
                        case "bash":
                            BashAction bashAction = new BashAction(traversal, n, duration, size, format);
                            bashAction.executeAction(id);
                            break;
                        default:
                            System.err.println("Wrong XML tag syntax");
                            System.exit(10);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
