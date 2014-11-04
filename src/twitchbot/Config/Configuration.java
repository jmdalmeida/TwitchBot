package twitchbot.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

    private static Configuration instance = null;
    private Map<String, String> configMap;

    protected Configuration() {
        try {
            setup();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

    public String getValue(String key) {
        return configMap.get(key);
    }

    private void setup() throws FileNotFoundException {
        configMap = new HashMap<>();
        String currentParent = "";
        URL url = getClass().getResource("config");
        try (Scanner scanner = new Scanner(new File(url.getPath()))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.charAt(0) == '[') {
                    currentParent = line;
                } else {
                    String[] l = line.split("=");
                    configMap.put(currentParent.substring(1, currentParent.length() - 1) + "_" + l[0], l[1]);
                }
            }
        };
    }

}
