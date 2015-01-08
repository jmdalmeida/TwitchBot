package twitchbot.Commands;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.jibble.pircbot.User;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewers;

public class DefaultCommands extends BotModule {

    public DefaultCommands(TwitchBot bot) {
        super(bot, PriorityLevel.NORMAL);
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!uptime", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                long elapsedTime = System.nanoTime() - bot.getConnectedTimestamp();
                final long hr = TimeUnit.NANOSECONDS.toHours(elapsedTime);
                final long min = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
                bot.sendMessage(channel, "I've been up for " + String.format("%02d hours, %02d minutes", hr, min) + ".");
            }

        });
        cmds.put("!debug", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                ((Viewers) bot.getModule("Viewers")).listViewers();
                for (User u : bot.getUsers(channel)) {
                    System.out.println(u.toString() + " isOP: " + u.isOp() + " hasVoice: " + u.hasVoice());
                }
            }

        });
        cmds.put("!quit", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                bot.quitAndExit();
            }

        });
        return cmds;
    }

}
