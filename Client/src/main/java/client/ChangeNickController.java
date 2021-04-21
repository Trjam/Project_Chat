package client;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChangeNickController {
    private Controller controller;


    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nickField;
    @FXML
    private  TextArea textArea;


    @FXML
    public void tryChangeNick() {

        String password = passwordField.getText().trim();
        String nickname = nickField.getText().trim();

        controller.changeNickname(nickname, password);
    }

    public void showResult(String result) {
        if (result.equals("/chgnick_ok")) {
            textArea.appendText("Ваш никнейм был успешно изменён.\n");
        } else {
            textArea.appendText("Сменить никнейм не удалась. \nВозможно никнейм занят, либо вы ввели не верный пароль.\n");
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public  void setTextArea(String str) {
        textArea.setWrapText(true);
        textArea.clear();
        textArea.appendText(str);
    }
}
