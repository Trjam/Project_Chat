package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private DataInputStream in;
    private DataOutputStream out;

    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    socket.setSoTimeout(120000);

                    // цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/q")) {
                            out.writeUTF("/q");
                            throw new RuntimeException("Клиент решил отключиться");
                        }

                        // Аутентификация
                        if (str.startsWith("/auth")) {
                            String[] token = str.split("\\s+", 3);
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                            if (newNick != null) {
                                login = token[1];
                                if (!server.isLoginAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMsg("/auth_ok " + nickname);
                                    server.subscribe(this);
                                    System.out.println("Client authenticated. nick: " + nickname +
                                            " Address: " + socket.getRemoteSocketAddress());
                                    break;
                                } else {
                                    sendMsg("С этим логином уже авторизовались");
                                }
                            } else {
                                sendMsg("Неверный логин / пароль");
                            }
                        }
                        // Регистрация
                        if (str.startsWith("/reg")) {
                            String[] token = str.split("\\s+", 4);
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService()
                                    .registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("/reg_ok");
                            } else {
                                sendMsg("/reg_no");
                            }
                        }

                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/q")) {
                            out.writeUTF("/q");
                            break;
                        }

                        if (str.startsWith("/w")) {
                            String[] whisp = str.split("\\s+", 3);
                            //костыль чтоб не падало при "/w qwe"
                            switch (whisp.length) {
                                case 2:
                                    server.whisperingMsg(this, whisp[1], "o/");
                                    break;
                                case 3:
                                    server.whisperingMsg(this, whisp[1], whisp[2]);
                                    break;
                                default:
                                    server.whisperingMsg(this, whisp[1], "Шепнуть не вышло, проверте формат сообщения.");
                            }
                        } else
                            server.broadcastMsg(this, str);
                    }

                //после 120 секунд простоя отсылает таймаут, по которому клиент закрывает сокет и пишет сообщение о логауте,
                //т.к. textarea разные у меня на экране логина и чата, пришлось вот так реализовать
                }catch (SocketTimeoutException e) {
                    sendMsg("/logout");
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("client disconnect " + socket.getRemoteSocketAddress());
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }


    public String getLogin() {
        return login;
    }
}

