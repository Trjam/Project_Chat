package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Logs {

    static BufferedWriter writer;

    public static void createLog(String login) throws IOException {
        if (Files.notExists(Paths.get("Logs/Log_" + login + ".txt"))) {
            Files.createFile(Paths.get("Logs/Log_" + login + ".txt"));
        }
    }

    public static void writeToLog(String login, String str) throws IOException {
            createLog(login);
            writer = new BufferedWriter(new FileWriter("Logs/Log_" + login + ".txt", true));
            writer.write(str + "\n");
    }

    public static String readLast100FromLog(String login) throws IOException {
        createLog(login);
        BufferedReader reader= new BufferedReader(new FileReader("Logs/Log_" + login + ".txt"));
        LinkedList<String> lines = new LinkedList<>();
        String line = null;
        while ((line = reader.readLine())!=null) {
            lines.add(line);
        }
        if (lines.size()>100) {
            for (int i = lines.size()-100; i != 0 ; i--) {
                lines.remove();
            }
        }
        reader.close();
        for (String str: lines) {
            line+=str +"\n" ;     //при выходе с цикла while line==null, поэтому чтоб не пропадало добро, пускаем опять в ход
        }
        return line;
    }

    public static void writerClose() throws IOException {
        if (writer!=null) {
            writer.close();
        }
    }

}
