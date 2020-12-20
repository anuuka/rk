import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// Each client will be managed in a dedicated thread
public class HttpServer implements Runnable {
    static final File WEB_ROOT = new File(".");
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "not_supported.html";

    static final int PORT = 8080;

    static final boolean verbose = true;

    // Client connection via Socket class
    private final Socket socketConnect;

    public HttpServer(Socket c) {
        socketConnect = c;
    }

    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            System.out.println("Server started.");
            System.out.println("Listening for connection on port " + PORT + " ...\n");

            // listen until the user halts the server connection
            while (true) {
                HttpServer server = new HttpServer(serverSocket.accept());
                if (verbose) {
                    System.out.println("Connection opened. (" + new Date() + ")");
                }
                // create dedicated thread to manage the client connection
                Thread thread = new Thread(server);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Server Connection Error: " + e.getMessage());
        }
    }

    @Override
    public void run() {

        // manage particular client connection
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // read characters from the client via input stream on the socket
            in = new BufferedReader(new InputStreamReader(socketConnect.getInputStream()));

            // get character output stream to client (for headers)
            out = new PrintWriter(socketConnect.getOutputStream());

            // get binary output stream stream to client (for requested data)
            dataOut = new BufferedOutputStream(socketConnect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();

            // parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // get the HTTP method from the client

            // get file requested
            fileRequested = parse.nextToken().toLowerCase();

            // support only GET and HEAD method
            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (verbose) {
                    System.out.println("501 Not Implemented: " + method + " method.");
                }

                // return the not supported file to the client
                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";

                // read content to return to client
                byte[] fileData = readFileData(file, fileLength);

                // send HTTP Headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: HTTP Server from FaBris: 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMimeType);
                out.println("Content-length: " + fileLength);
                out.println(); // blank line between headers and content
                out.flush(); // flush character output stream buffer

                //file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();
            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) {
                    byte[] fileData = readFileData(file, fileLength);

                    // send HTTP headers
                    out.println("HTTP/1.1 200 OK");
                    out.println("Server: HTTP Server from FaBris : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("content-length: " + fileLength);
                    out.println();
                    out.flush();

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                }
                if (verbose) {
                    System.out.println("File" + fileRequested + " of type " + content + " returned");
                }

            }
        } catch (FileNotFoundException fe) {
            try {
                fileNotFound(out, dataOut, fileRequested);
            } catch (IOException ioe) {
                System.err.println("Error with file not found exception: " + ioe.getMessage());
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e);
        } finally {
            try {
                in.close();
                out.close();
                dataOut.close();
                socketConnect.close();
            } catch (Exception e) {
                System.err.println("Error closing stream: " + e.getMessage());
            }
            if (verbose) {
                System.out.println("Connection closed.\n");
            }
        }

    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte[] fileData = new byte[fileLength];

        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
        return fileData;
    }

    // return supported MIME types
    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith("htm") || fileRequested.endsWith("html"))
            return "text/html";
        else
            return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        out.println("Server: Http Server from FaBris : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("content-length: " + fileLength);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File" + fileRequested + " not found");
        }
    }
}
