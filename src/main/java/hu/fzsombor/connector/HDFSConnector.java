package hu.fzsombor.connector;

public class HDFSConnector {

    public void copyToHDFS(String destinationUser, String destinationPassword, String destinationHost,
                           String filePath, String destinationHDFSPath) {

        SCPConnector scpConnector = new SCPConnector();
        try {
            scpConnector.copyTo(destinationUser, destinationPassword, destinationHost, filePath, "/tmp/");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String filename = filePath.substring(filePath.lastIndexOf('/') + 1);
        SSHConnector sshConnector = new SSHConnector(destinationUser, destinationPassword, destinationHost, "");
        String errorMessage = sshConnector.connect();

        if (errorMessage != null) {
            System.out.println(errorMessage);
        }

        String result = sshConnector.sendCommand("hdfs dfs -put /tmp/" + filename + " " + destinationHDFSPath);
        System.out.println(result);
        result = sshConnector.sendCommand("hdfs dfs -ls " + destinationHDFSPath);
        System.out.println(result);

        sshConnector.close();


    }

}
