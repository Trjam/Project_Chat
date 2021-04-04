package client;

import java.io.*;
import java.net.Socket;


public class client {
    private static final int PORT = 8189;
    private static final String IP_ADDRESS = "localhost";
    private static DataInputStream in;
    private static DataOutputStream out;
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        try (Socket socket = new Socket(IP_ADDRESS, PORT)) {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(()->{

                while (true) {
                    try {
                        out.writeUTF(reader.readLine());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }).start();

            while (true) {
                String server = in.readUTF();
                if (server.equals("/q")) {
                    System.out.println("Server down");
                    out.writeUTF("/q");
                    break;
                }
                else if (!server.equals(""))
                    System.out.println("Server: " + server);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}