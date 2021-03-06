package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю.
     * Если учетки с таким логином и паролем нет то вернет
     * Если учетка есть то вернет никнейм.
     * @return никнейм если есть совпадение по логину и паролю, null если нет совпадения
     * */
    String getNicknameByLoginAndPassword(String login, String password );

    //TODO добавить описание методов
    /**
     * Попытка регистрации новой учетной записи
     * */
    boolean registration(String login, String password, String nickname);

    boolean changeNick(String newNickname, String login);

    boolean checkPassword(String password, String login);
}
