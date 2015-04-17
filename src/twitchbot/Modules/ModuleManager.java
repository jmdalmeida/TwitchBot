package twitchbot.Modules;

import java.util.HashMap;
import java.util.Map;
import org.jibble.pircbot.User;
import twitchbot.Commands.ChatCommands;
import twitchbot.Commands.DefaultCommands;
import twitchbot.Modules.Clock.Clock;
import twitchbot.Modules.TimedMessages.TimedMessages;
import twitchbot.Modules.QuestionsAndAnswers.QuestionsAndAnswers;
import twitchbot.Modules.TextFilter.TextFilter;
import twitchbot.Modules.Topic.Topic;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Viewers;

public class ModuleManager {

    private Map<String, BotModule> modules;

    public void setupModules(TwitchBot bot) {
        modules = new HashMap<>();
        modules.put("DefaultCommands", new DefaultCommands(bot));
        modules.put("ChatCommands", new ChatCommands(bot));
        modules.put("TextFilter", new TextFilter(bot));
        modules.put("Viewers", new Viewers(bot));
        modules.put("Topic", new Topic(bot));
        modules.put("Clock", new Clock(bot));
        modules.put("QuestionsAndAnswers", new QuestionsAndAnswers(bot));
        modules.put("TimedMessages", new TimedMessages(bot));
    }

    public BotModule getModule(String key) {
        return modules.get(key);
    }

    public BotModule[] getAllModules() {
        return modules.values().toArray(new BotModule[modules.size()]);
    }

    public void broadcastOnMessage(String channel, String sender, String login, String hostname, String message) {
        String normalizedMsg = ChatCommands.normalize(message);
        modules.values().stream().forEach((m) -> {
            m.onMessage(channel, sender, login, hostname, normalizedMsg);
        });
    }

    public void broadcastOnUserList(String channel, User[] users) {
        modules.values().stream().forEach((m) -> {
            m.onUserList(channel, users);
        });
    }

    public void broadcastOnJoin(String channel, String sender, String login, String hostname) {
        modules.values().stream().forEach((m) -> {
            m.onJoin(channel, sender, login, hostname);
        });
    }

    public void broadcastOnQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
        modules.values().stream().forEach((m) -> {
            m.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
        });
    }

    public void broadcastOnUserMode(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
        modules.values().stream().forEach((m) -> {
            m.onUserMode(targetNick, sourceNick, sourceLogin, sourceHostname, mode);
        });
    }

    public void broadcastOnDisconnect() {
        modules.values().stream().forEach((m) -> {
            m.onDisconnect();
        });
    }

}
