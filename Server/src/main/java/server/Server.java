package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT=8189;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            try (Socket socket = serverSocket.accept()) {
                System.out.println("Client connected");

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
                    String client = in.readUTF();
                        if (!client.equals(""))
                        System.out.println("Client: " + client);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
