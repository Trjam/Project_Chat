package client;

import javafx.application.Platform;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea loginTextArea;

    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    public TextField loginField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public VBox authPanel;

    @FXML
    public VBox msgPanel;

    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;

    private Stage stage;
    private Stage regStage;
    private Stage changeNicknameStage;
    private RegController regController;
    private ChangeNickController changeNickController;
    private boolean timeout = false;

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
        msgPanel.setVisible(authenticated);
        msgPanel.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }
        setTitle(nickname);
        textArea.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Platform.runLater(() -> {
            stage = (Stage) loginTextArea.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                System.out.println("bye");
                if (socket != null && !socket.isClosed()) {
                    try {
                        out.writeUTF("/q");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);
    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл аутентификации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/q")) {
                                System.out.println("disconnect");
                                out.writeUTF("/q");
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                nickname = str.split("\\s+")[1];
                                setAuthenticated(true);
                                break;
                            }
                            if (str.startsWith("/reg_ok")) {
                                regController.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")) {
                                regController.showResult("/reg_no");
                            }
                        } else {
                            loginTextArea.appendText(str + "\n");
                        }
                    }

                    //цикл работы
                    textArea.setWrapText(true);
                    textArea.appendText("Добро пожаловать в чат" + "\nЕсли вы хотите сменить свой никнейм, " +
                            "то кликните по нему в списке пользователей справа.\n");
                    while (authenticated) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/q")) {
                                out.writeUTF("/q");
                                break;
                            }
                            // Обновление списка клиентов
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split("\\s+");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }
                            //логаут при таймауте сессии
                            if (str.startsWith("/logout")) {
                                timeout = true;
                                break;
                            }


                            if (str.startsWith("/chgnick ")) {
                                String[] token = str.split("\\s+",2 );
                                changeNickController.setTextArea(token[1]);
                                setTitle(nickname);
                            }
                            //TODO подумать, а нужны ли эти, если с сервера все что надо приходит,
/*
                            if (str.startsWith("/chgnick_ok")) {
                                changeNickController.showResult("/chgnick_ok");
                            }
                            if (str.startsWith("/chgnick_no")) {
                                changeNickController.showResult("/chgnick_no");
                            }*/
                        } else {
                            textArea.appendText(str + "\n");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    setAuthenticated(false);
                    try {
                        socket.close();
                        if (timeout) {
                            timeout = false;
                            loginTextArea.appendText("Session is over. Logout.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void tryToAuth() {
        loginTextArea.clear();
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s",
                loginField.getText().trim(), passwordField.getText().trim());

        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("Open chat");
            } else {
                stage.setTitle(String.format("Open chat: [ %s ]", nickname));
            }
        });
    }

    public void clickClientList() {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        if (receiver.equals(nickname)) {
            if (changeNicknameStage == null) {
                createChangeNicknameWindow();
            }
            Platform.runLater(() -> {
                changeNicknameStage.show();
            });
        } else {
            textField.setText("/w " + receiver + " ");
        }
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Open chat registration");
            regStage.setScene(new Scene(root, 400, 320));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createChangeNicknameWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/changeNickname.fxml"));
            Parent root = fxmlLoader.load();
            changeNicknameStage = new Stage();
            changeNicknameStage.setTitle("Open chat change nickname");
            changeNicknameStage.setScene(new Scene(root, 400, 320));

            changeNicknameStage.initModality(Modality.APPLICATION_MODAL);
            changeNicknameStage.initStyle(StageStyle.UTILITY);

            changeNickController = fxmlLoader.getController();
            changeNickController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg() {
        if (regStage == null) {
            createRegWindow();
        }
        Platform.runLater(() -> {
            regStage.show();
        });
    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeNickname(String nickname, String password) {

        String msg = String.format("/chgnick %s %s", nickname, password );
        try {
            out.writeUTF(msg);

            //TODO поменять костыль на что нибудь нормальное
            this.nickname=nickname;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}

