package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import static server.StartServer.logger;

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
                    try {
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")) {
                                logger.info("Клиент прислал служебное сообщение: " + str);
                                if (str.equals("/q")) {
                                    out.writeUTF("/q");
                                    logger.info("Клиент решил отключиться");
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
                                            sendMsg("/auth_ok " + nickname + " " + getLogin());
                                            server.subscribe(this);
                                            //System.out.println("Client authenticated. nick: " + nickname +
                                            //        " Address: " + socket.getRemoteSocketAddress());
                                            logger.info("Client authenticated. nick: " + nickname +
                                                    " Address: " + socket.getRemoteSocketAddress());
                                            //после авторизации таймаут 15 мин, ибо нефиг сидеть в чате просто так
                                            socket.setSoTimeout(900000);
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


                        }
                    }catch (SocketTimeoutException e){
                        sendMsg("/q");
                    }

                    //цикл работы
                    try {
                        while (true) {
                            String str = in.readUTF();

                            if (str.startsWith("/")) {
                                logger.info("Клиент прислал служебное сообщение: " + str);
                                //выход
                                if (str.equals("/q")) {
                                    out.writeUTF("/q");
                                    break;
                                }
                                //смена ника
                                if (str.startsWith("/chgnick")) {
                                    String[] token = str.split("\\s+", 3);
                                    if (token.length < 3 || token[2].equals("")) {
                                        sendMsg("/chgnick Заполните все поля.");
                                        continue;
                                    }
                                    //возможно проверка пароля при смене ника излишне, но пусть будет
                                    else if (server.getAuthService().checkPassword(token[2], getLogin())) {
                                        if (server.getAuthService().changeNick(token[1], getLogin())) {
                                            sendMsg("/chgnick_ok " + token[1] + " Вы сменили никнейм на " + token[1]);
                                            this.nickname=token[1];
                                            server.broadcastClientList();
                                        } else {
                                            sendMsg("/chgnick Никнейм " + token[1] + " уже занят, попробуйте придумать другой.");
                                        }
                                    } else {
                                        sendMsg("/chgnick Вы ввели не верный пароль.");
                                    }
                                }
                                //Приватные сообщения
                                if (str.startsWith("/w")) {
                                    String[] whisp = str.split("\\s+", 3);
                                    //костыль чтоб не падало при "/w login"
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
                                }
                            }else
                                logger.info("Клиент прислал сообщение для всех: " + str);
                                //Сообщения всем
                                server.broadcastMsg(this, str);
                        }
                    }catch (SocketTimeoutException e) {
                        sendMsg("/logout");
                        logger.info("Клиент отключен по таймауту");
                    }
                } catch (IOException e) {
                    logger.warning("Ошибка " + e.getStackTrace());
                } finally {
                    server.unsubscribe(this);
                    //System.out.println("client disconnect " + socket.getRemoteSocketAddress());
                    logger.info("client disconnect " + socket.getRemoteSocketAddress());
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

