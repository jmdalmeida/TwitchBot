package twitchbot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import twitchbot.Config.Configuration;
import twitchbot.WordFilter.WordFilter;

public class TwitchBot extends PircBot {

    private Map<String, ChatFunction> chatFunctions;
    private final WordFilter wordFilter;

    private String[] channels;
    
    private long connectedTimestamp;

    public TwitchBot() {
        connectedTimestamp = System.nanoTime();
        this.setName(Configuration.getInstance().getValue("BOT_username"));
        this.setVerbose(true);
        try {
            this.connect("irc.twitch.tv", 6667, Configuration.getInstance().getValue("BOT_oauth"));
        } catch (IOException | IrcException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        setupCommands();
        wordFilter = new WordFilter();
        channels = Configuration.getInstance().getValue("CHANNELS_join").split(",");
        for (String c : channels) {
            this.joinChannel("#" + c);
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        super.onMessage(channel, sender, login, hostname, message);
        String[] msg = message.split(" ");
        String possibleCmd = msg[0];
        if (chatFunctions.containsKey(possibleCmd)) {
            chatFunctions.get(possibleCmd).doFunction(channel, sender, login, hostname);
        }
        if (wordFilter.isRunning() && !wordFilter.validateWords(msg)) {
            this.sendMessage(channel, "/" + Configuration.getInstance().getValue("WORDFILTER_action") + " " + sender);
            this.sendMessage(channel, Configuration.getInstance().getValue("WORDFILTER_output").replace("$sender$", sender));
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        super.onJoin(channel, sender, login, hostname);
        if (!sender.equalsIgnoreCase(Configuration.getInstance().getValue("BOT_username"))) {
            chatFunctions.get("!hello").doFunction(channel, sender, login, hostname);
        } else {
            this.sendMessage(channel, Configuration.getInstance().getValue("BOT_username") + " is up and running!");
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
        chatFunctions.put("!uptime", new ChatFunction() {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                long elapsedTime = System.nanoTime() - connectedTimestamp;
                final long hr = TimeUnit.NANOSECONDS.toHours(elapsedTime);
                final long min = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
                sendMessage(channel, "I've been up for "  + String.format("%02d hours, %02d minutes", hr, min) + ".");
            }

        });
    }

}
