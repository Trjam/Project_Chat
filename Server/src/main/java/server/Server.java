package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static server.StartServer.logger;

public class Server {
    private static final int PORT=8189;
    private final List<ClientHandler> clients;
    private final AuthService authService;

    private static ServerSocket server;
    private static Socket socket;


    public Server() {
        clients = new CopyOnWriteArrayList<>();
        //authService = new SimpleAuthService();

        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключиться к БД");
        }
        authService = new DBAuthServise();

        try {
            server = new ServerSocket(PORT);
            //System.out.println("Server started");
            logger.info("Server started");

            while(true){
                socket = server.accept();
                System.out.println(socket.getLocalSocketAddress());
                //System.out.println("Client connect: "+ socket.getRemoteSocketAddress());
                logger.info("Client connect: "+ socket.getRemoteSocketAddress());
                new ClientHandler(this, socket);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();
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


    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService () {
        return authService;
    }

    public void whisperingMsg (ClientHandler sender, String nickName, String msg) {
        String message = String.format("%s whispering: %s", sender.getNickname(), msg);

        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickName)) {
                c.sendMsg(message);
                if (sender.equals(c)) {
                    return;
                }
                sender.sendMsg(message);
                return;
            }
        }
        sender.sendMsg("not found user: " + nickName);
    }


    public boolean isLoginAuthenticated (String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }

        String msg = sb.toString();

        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

}

