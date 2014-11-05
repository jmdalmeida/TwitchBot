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
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;
import twitchbot.WordFilter.WordFilter;

public class TwitchBot extends PircBot {

    private Map<String, ChatFunction> chatFunctions;
    private Map<String, Viewer> viewers;
    private WordFilter wordFilter;

    private final String channel;
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
        channel = Configuration.getInstance().getValue("CHANNEL_join");
        System.out.println("Channel " + channel);
        this.joinChannel("#" + channel);
        setupCommands();
        setupViewers();
        setupWordFilter();
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

    @Override
    protected void onUserList(String channel, User[] users) {
        super.onUserList(channel, users);
        viewers.clear();
        for (User u : users) {
            addNewViewer(u);
        }
        System.out.println("User List updated!");
    }

    private void setupCommands() {
        chatFunctions = new HashMap<>();
        chatFunctions.put("!hello", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "Hello " + sender + ", how are you?");
            }

        });
        chatFunctions.put("!date", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "The current date is " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + ".");
            }

        });
        chatFunctions.put("!time", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                sendMessage(channel, "The current time is " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " GMT.");
            }

        });
        chatFunctions.put("!commands", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                String commands = chatFunctions.keySet().toString();
                sendMessage(channel, "Available commands are: " + commands.substring(1, commands.length() - 1) + ".");
            }

        });
        chatFunctions.put("!uptime", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                long elapsedTime = System.nanoTime() - connectedTimestamp;
                final long hr = TimeUnit.NANOSECONDS.toHours(elapsedTime);
                final long min = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
                sendMessage(channel, "I've been up for " + String.format("%02d hours, %02d minutes", hr, min) + ".");
            }

        });
    }

    private void setupViewers() {
        viewers = new HashMap<>();
    }

    private void setupWordFilter() {
        wordFilter = new WordFilter(Configuration.getInstance().getValue("WORDFILTER_status"));
    }

    private void addNewViewer(User user) {
        Permission p = null;
        if (user.getNick().equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username"))) {
            p = Permission.BROADCASTER;
        } else {
            p = user.isOp() ? Permission.MOD : Permission.NORMAL;
        }
        Viewer v = new Viewer(user.getNick(), p, System.nanoTime());
        viewers.put(user.getNick(), v);
    }

}
