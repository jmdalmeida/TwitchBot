package twitchbot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import twitchbot.Commands.ChatCommands;
import twitchbot.Config.Configuration;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;
import twitchbot.Modules.WordFilter.WordFilter;
import twitchbot.Viewers.Viewers;

public class TwitchBot extends PircBot {

    public final Viewers viewers;
    private final ChatCommands commands;
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
        channel = Configuration.getInstance().getValue("CHANNEL_name");
        this.joinChannel("#" + channel);
        commands = new ChatCommands(this);
        viewers = new Viewers();
        setupWordFilter();
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        commands.runCommand(channel, sender, login, hostname, message);
        String[] msg = message.split(" ");
        if (wordFilter.isRunning() && !wordFilter.validateWords(msg)) {
            this.sendMessage(channel, "/" + Configuration.getInstance().getValue("WORDFILTER_action") + " " + sender);
            this.sendMessage(channel, Configuration.getInstance().getValue("WORDFILTER_output").replace("$sender$", sender));
        }
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        viewers.addNewViewer(sender);
        if (!sender.equalsIgnoreCase(Configuration.getInstance().getValue("BOT_username"))) {
            sendMessage(channel, "Hello " + sender + ", how are you?");
        } else {
            this.sendMessage(channel, "Up and running!");
        }
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        viewers.removeViewer(sourceNick);
    }

    @Override
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        //#Channel +o user
        String[] params = mode.split(" ");
        String chn = params[0];
        String operation = params[1];
        String username = params[2];
        if (chn.equalsIgnoreCase("#" + channel) && viewers.exists(username)) {
            if (!username.equalsIgnoreCase(Configuration.getInstance().getValue("BROADCASTER_username"))) {
                switch (operation) {
                    case "+o":
                        viewers.set(username, Permission.MODERATOR);
                        break;
                    case "-o":
                        viewers.set(username, Permission.NORMAL);
                        break;
                }
            } else {
                viewers.set(username, Permission.BROADCASTER);
            }
        }
    }

    private void setupWordFilter() {
        wordFilter = new WordFilter(Configuration.getInstance().getValue("WORDFILTER_status"));
    }

    public void quitAndExit() {
        this.sendMessage("#" + channel, "I'm out, cya!");
        this.partChannel(channel);
        this.quitServer();
        try {
            Thread.sleep(1000); //give it time to send the message
        } catch (InterruptedException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dispose();
    }

    public long getConnectedTimestamp() {
        return connectedTimestamp;
    }

}
