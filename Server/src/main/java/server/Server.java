package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {
    private static final int PORT=8189;
    private final List<ClientHandler> clients;
    private final AuthService authService;

    private static ServerSocket server;
    private static Socket socket;


    public Server() {
        clients = new CopyOnWriteArrayList<>();
        authService = new SimpleAuthService();

        try {
            server = new ServerSocket(PORT);
            System.out.println("Server started");

            while(true){
                socket = server.accept();
                System.out.println(socket.getLocalSocketAddress());
                System.out.println("Client connect: "+ socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastMsg (ClientHandler sender, String msg){
        String message = String.format("%s : %s", sender.getNickname(), msg);

        for (ClientHandler c : clients) {
                c.sendMsg(message);
            }
        }

    public void subscribe (ClientHandler clientHandler){
        clients.add(clientHandler);
    }

    public void unsubscribe (ClientHandler clientHandler){
        clients.remove(clientHandler);
    }

    public AuthService getAuthService () {
        return authService;
    }

    //TODO подумать как бы вкрячить отбивку, если нет онлайн того, кому шепчем
    public void whisperingMsg (ClientHandler sender, String nickName, String msg){
        String message = String.format("%s whispering: %s", sender.getNickname(), msg);

        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickName)||c.getNickname().equals(sender.getNickname())) {
                c.sendMsg(message);
            }
        }
    }

}

