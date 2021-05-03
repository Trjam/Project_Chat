package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class StartServer {
    protected static final Logger logger = Logger.getLogger("");

    public static void main(String[] args) {



        new Server();
    }
}
