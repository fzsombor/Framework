package hu.fzsombor.connector;

import com.jcraft.jsch.*;

import java.io.*;

public class SCPConnector {
    public void copyTo(String destinationUser, String destinationPassword, String destinationHost, String filePath, String destinationPath) throws Exception {

        FileInputStream fis = null;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(destinationUser, destinationHost, 22);
            session.setPassword(destinationPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // exec 'scp -t rfile' remotely
            String rfile = destinationPath.replace("'", "'\"'\"'");
            rfile = "'" + rfile + "'";
            String command = "scp " + " -t " + destinationPath;
            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            // get I/O streams for remote scp
            OutputStream out = channel.getOutputStream();
            InputStream in = channel.getInputStream();

            channel.connect();

            if (checkAck(in) != 0) {
                System.exit(0);
            }

            File _lfile = new File(filePath);


            long filesize = _lfile.length();
            command = "C0644 " + filesize + " ";
            if (filePath.lastIndexOf('/') > 0) {
                command += filePath.substring(filePath.lastIndexOf('/') + 1);
            } else {
                command += filePath;
            }
            command += "\n";
            out.write(command.getBytes());
            out.flush();

            fis = new FileInputStream(filePath);
            byte[] buf = new byte[1024];
            while (true) {
                int len = fis.read(buf, 0, buf.length);
                if (len <= 0) break;
                out.write(buf, 0, len); //out.flush();
            }
            fis.close();
            fis = null;
            // send '\0'
            buf[0] = 0;
            out.write(buf, 0, 1);
            out.flush();

            out.close();

            channel.disconnect();
            session.disconnect();

        } catch (Exception e) {
            System.out.println(e);
            if (fis != null) {
                fis.close();
            }
        }
    }

    static int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }


}
