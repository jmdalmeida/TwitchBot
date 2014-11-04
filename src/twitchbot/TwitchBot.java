package twitchbot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;

public class TwitchBot extends PircBot {

    private static final String BOT_USERNAME = "DieselTheBot";
    private static final String OAUTH = "oauth:ljkr0pyzjgxs6pq1qkovgas8hvt44p";
    
    private Map<String, ChatFunction> chatFunctions;

    private final String channel;

    public TwitchBot(String channel) {
        this.channel = channel;
        this.setName(BOT_USERNAME);
        this.setVerbose(true);
        try {
            this.connect("irc.twitch.tv", 6667, OAUTH);
        } catch (IOException | IrcException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        setupCommands();
        this.joinChannel("#" + channel);
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        super.onMessage(channel, sender, login, hostname, message);
        String[] msg = message.split(" ");
        String possibleCmd = msg[0];
        if(chatFunctions.containsKey(possibleCmd)){
            chatFunctions.get(possibleCmd).doFunction(channel, sender, login, hostname);
        }
    }

    @Override
    protected void onConnect() {
        this.sendMessage("#" + channel, BOT_USERNAME + " is up and running!");
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        super.onJoin(channel, sender, login, hostname);
        if (!sender.equalsIgnoreCase(BOT_USERNAME)) {
            chatFunctions.get("!hello").doFunction(channel, sender, login, hostname);
        }
    }

    private void setupCommands() {
        chatFunctions = new HashMap<>();
        chatFunctions.put("!hello", new ChatFunction() {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "Hello " + sender + ", how are you?");
            }
            
        });
        chatFunctions.put("!date", new ChatFunction() {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "The current date is " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + ".");
            }
            
        });
        chatFunctions.put("!time", new ChatFunction() {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "The current time is " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " GMT.");
            }
            
        });
        chatFunctions.put("!commands", new ChatFunction() {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
               String commands = chatFunctions.keySet().toString();
               sendMessage(channel, "Available commands are: " + commands.substring(1, commands.length() - 1) + ".");
            }
            
        });
    }

}
