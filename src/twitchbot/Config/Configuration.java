package twitchbot.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration {

    private static Configuration instance = null;

    private Properties prop;

    private Configuration() {
        setup();
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }
    
    public String getProperty(String key){
        return prop.getProperty(key);
    }

    private void setup() {
        prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = getClass().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
