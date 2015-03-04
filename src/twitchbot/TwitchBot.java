package twitchbot;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.*;
import twitchbot.Commands.ChatCommands;
import twitchbot.Config.Configuration;
import twitchbot.Modules.ModuleManager;

public class TwitchBot extends PircBot {

    private ModuleManager modules;

    private final String channel;
    private long connectedTimestamp;

    private boolean muted = false;

    public TwitchBot() {
        connectedTimestamp = System.nanoTime();
        channel = Configuration.getInstance().getProperty("channel");
        modules = new ModuleManager();
        modules.setupModules(this);
        ((ChatCommands) modules.getModule("ChatCommands")).setupCommands(modules.getAllModules());
        this.setName(Configuration.getInstance().getProperty("botname"));
        this.setVerbose(true);
        try {
            this.connect("irc.twitch.tv", 6667, Configuration.getInstance().getProperty("oauth"));
        } catch (IOException | IrcException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.joinChannel("#" + channel);
    }

    public String getChannel() {
        return channel;
    }

    public ModuleManager getModuleManager() {
        return modules;
    }

    public void mute() {
        botMessage("MrDestructoid OK! OK! I'll shut up...");
        muted = true;
    }

    public void unmute() {
        muted = false;
        botMessage("MrDestructoid FREEDOM!");
    }

    public void quitAndExit() {
        botMessage("I'm out, cya!");
        partChannel(channel);
        quitServer();
        try {
            Thread.sleep(1000); //give it time to send the message
        } catch (InterruptedException ex) {
            Logger.getLogger(TwitchBot.class.getName()).log(Level.SEVERE, null, ex);
        }
        dispose();
    }

    public long getConnectedTimestamp() {
        return connectedTimestamp;
    }

    public boolean isMe(String s) {
        return getName().equals(s);
    }

    public synchronized void botMessage(String msg) {
        if (!muted) {
            sendMessage("#" + channel, msg);
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String message) {
        modules.broadcastOnMessage(channel, sender, login, hostname, message);
    }

    @Override
    protected void onUserList(String channel, User[] users) {
        modules.broadcastOnUserList(channel, users);
    }

    @Override
    protected void onJoin(String channel, String sender, String login, String hostname) {
        if (sender.equalsIgnoreCase(getName())) {
            botMessage("Up and running!");
        }
        modules.broadcastOnJoin(channel, sender, login, hostname);
    }

    @Override
    protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        modules.broadcastOnQuit(sourceNick, sourceLogin, sourceHostname, reason);
    }

    @Override
    protected void onUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        modules.broadcastOnUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
    }

    @Override
    protected void onDisconnect() {
        modules.broadcastOnDisconnect();
    }

}
