/*
 */
package ass_webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Evicka
 */
public class Webserver {

    protected ServerSocket ss;
    StaticThreadpool pool;

    public Webserver(int port, int poolSize) {
        try {
            ss = new ServerSocket(port);
            System.out.println("Server is running and waiting for client connection");
            pool = new StaticThreadpool(poolSize);
            handleNewClient();
        } catch (IOException ex) {
            Logger.getLogger(Webserver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void handleNewClient() throws IOException {
        while (true) {
            Socket cs = ss.accept();
            System.out.println("client socket accepted");
            pool.execute(new ClientHandler(cs));
        }
    }

    protected class ClientHandler implements Runnable {

        private DataOutputStream out;
        private BufferedReader in;

        public ClientHandler(Socket cs) {
            try {
                out = new DataOutputStream(cs.getOutputStream());
                in = new BufferedReader(new InputStreamReader(cs.getInputStream()));
            } catch (IOException ex) {
                Logger.getLogger(Webserver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void run() {
            StringBuilder responseBuffer = new StringBuilder();
            StringTokenizer st;

            try {
                String requestString;
                System.out.println("server is waiting for input");
                while (in.ready() && (requestString = in.readLine()) != null) {
                    responseBuffer.append("Java webserver homepage\nreceived request:\n");
                    while (in.ready()) {
                        responseBuffer.append(in.readLine()).append("\n");
                    }
                    st = new StringTokenizer(requestString);
                    String method = st.nextToken();
                    String httpQuery = st.nextToken();

                    if (method.equals("GET")) {
                        if (httpQuery.equals("/")) {
                            sendResponse(200, responseBuffer.toString(), false);
                        } else {
                            //soubor
                            String fileName = httpQuery.replaceFirst("/", "");
                            File f = new File(System.getProperty("user.dir") + "//" + URLDecoder.decode(fileName, "UTF-8"));
                            if (f.exists() && f.isFile()) {
                                sendResponse(200, fileName, true);
                            } else {
                                sendResponse(404, "The Requested resource was not found", false);
                            }
                        }
                    } else if (method.equals("HEAD")) {
                        sendResponse(200, "This is java web server", false);
                    } else {
                        sendResponse(404, "Requested method not supported", false);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(Webserver.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("client service finished");
        }

        private void sendResponse(int statusCode, String msgSource, boolean isFile) throws IOException {

            String contentLength = null;
            String contentType = "Content-Type: text/html \r\n";
            FileInputStream fin = null;

            if (isFile) {
                fin = new FileInputStream(msgSource);
                contentLength = "Content-Length: " + String.valueOf(fin.available()) + "\r\n";
                if (msgSource.endsWith(".jpg") || msgSource.endsWith(".png")) {
                    contentType = "Content-Type: image/" + msgSource.substring(msgSource.lastIndexOf("."), msgSource.length()) + "+ \r\n";
                }
            } else {
                contentLength = "Content-Length: " + msgSource.length() + "\r\n";
            }
            out.writeBytes(statusCode == 200 ? "HTTP/1.1 200 OK" + "\r\n" : "HTTP/1.1 404 Not Found" + "\r\n");
            out.writeBytes("Java server");
            out.writeBytes(contentType);
            out.writeBytes(contentLength);
            out.writeBytes("\r\n");

            if (isFile) {
                sendFile(fin, out);
            } else {
                out.writeBytes(msgSource);
            }
            out.close();
        }

        private void sendFile(FileInputStream fin, DataOutputStream out) throws IOException {
            byte[] buffer = new byte[1024];

            while ((fin.read(buffer)) != -1) {
                out.write(buffer);
            }
            fin.close();
        }
    }
}