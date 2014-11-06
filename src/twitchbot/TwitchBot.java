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
        this.joinChannel("#" + channel);
        setupCommands();
        setupViewers();
        setupWordFilter();
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        String[] msg = message.split(" ");
        String possibleCmd = msg[0];
        if (chatFunctions.containsKey(possibleCmd)) {
            ChatFunction func = chatFunctions.get(possibleCmd);
            if (func.getPermission() != Permission.NORMAL) {
                Viewer u = viewers.get(sender);
                if (u != null) {
                    if (func.getPermission().getValue() <= u.getPermissionLevel().getValue()) {
                        func.doFunction(channel, sender, login, hostname);
                    } else {
                        this.sendMessage(channel, "You don't have permission to run this command, " + sender + ".");
                    }
                } else {
                    System.err.println("Error processing " + sender + "'s command (" + possibleCmd + "): Viewer not found.");
                }
            } else {
                func.doFunction(channel, sender, login, hostname);
            }
        }
        if (wordFilter.isRunning() && !wordFilter.validateWords(msg)) {
            this.sendMessage(channel, "/" + Configuration.getInstance().getValue("WORDFILTER_action") + " " + sender);
            this.sendMessage(channel, Configuration.getInstance().getValue("WORDFILTER_output").replace("$sender$", sender));
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        addNewViewer(sender);
        if (!sender.equalsIgnoreCase(Configuration.getInstance().getValue("BOT_username"))) {
            chatFunctions.get("!hello").doFunction(channel, sender, login, hostname);
        } else {
            this.sendMessage(channel, "Up and running!");
        }
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        removeViewer(sourceNick);
    }

    @Override
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //#Channel +o user
        String[] params = mode.split(" ");
        String chn = params[0];
        String operation = params[1];
        String username = params[2];
        if (chn.equalsIgnoreCase("#" + channel) && viewers.containsKey(username)) {
            if (!username.equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username"))) {
                switch (operation) {
                    case "+o":
                        viewers.get(username).setPermissionLevel(Permission.MODERATOR);
                        break;
                    case "-o":
                        viewers.get(username).setPermissionLevel(Permission.NORMAL);
                        break;
                }
            } else {
                viewers.get(username).setPermissionLevel(Permission.BROADCASTER);
            }
        }
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
        chatFunctions.put("!debug", new ChatFunction(Permission.BROADCASTER) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                System.out.println("*** Listing viewers ***");
                Object[] vs = viewers.values().toArray();
                for (Object o : vs) {
                    Viewer v = (Viewer) o;
                    System.out.println("* " + v.toString());
                }
                System.out.println("***********************");
            }

        });
        chatFunctions.put("!quit", new ChatFunction(Permission.BROADCASTER) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname) {
                quitAndExit();
            }

        });
    }

    private void setupViewers() {
        viewers = new HashMap<>();
    }

    private void setupWordFilter() {
        wordFilter = new WordFilter(Configuration.getInstance().getValue("WORDFILTER_status"));
    }

    private void addNewViewer(String username) {
        Permission p = Permission.NORMAL;
        String nick = username.toLowerCase();
        Viewer v = new Viewer(nick, p, System.nanoTime());
        viewers.put(nick, v);
        System.out.println("Users list: Adding " + nick + "(" + p.toString() + ")");
    }

    private void removeViewer(String sourceNick) {
        viewers.remove(sourceNick);
        System.out.println("Users list: Removing " + sourceNick + "");
    }

    private void quitAndExit() {
        this.sendMessage("#" + channel, "I'm out, cya!");
        this.partChannel(channel);
        this.quitServer();
    }

}
