package twitchbot.Modules.CycleMessages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import twitchbot.Commands.ChatCommands;
import twitchbot.Commands.ChatFunction;
import twitchbot.Modules.BotModule;
import twitchbot.Modules.PriorityLevel;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class CycleMessages extends BotModule {

    private final Map<String, CycleCommand> messages;
    private final Object lock = new Object();

    public CycleMessages(TwitchBot bot) {
        super(bot, PriorityLevel.NORMAL);
        messages = new HashMap<>();
    }

    public synchronized void addNewCommand(String name, String msg, long interval) {
        CycleCommand cc = new CycleCommand(name, msg, interval);
        new Thread(cc).start();
        messages.put(name, cc);
    }

    public synchronized void removeCommand(String name) {
        CycleCommand cc = messages.get(name);
        if (cc != null) {
            cc.stop();
            messages.remove(name);
        }
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        //!addcyclemessage <name> <interval> <message>
        cmds.put("!addcyclemessage", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                String msg = ChatCommands.extractMessage(message).substring(tokens[1].length() + tokens[2].length() + 2);
                addNewCommand(tokens[1], msg, Long.parseLong(tokens[2]));
            }

        });
        //!removecyclemessage <name>
        cmds.put("!stopcyclemessage", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                if (tokens[1] != null) {
                    removeCommand(tokens[1]);
                }
            }

        });
        return cmds;
    }

    @Override
    public void onDisconnect() {
        Object[] ccs = messages.values().toArray();
        for (int i = 0; i < ccs.length; i++) {
            CycleCommand cc = (CycleCommand) ccs[0];
            removeCommand(cc.getName());
        }
    }

    private class CycleCommand implements Runnable {

        private final long interval;
        private final String name, msg;
        private boolean cycling;

        public CycleCommand(String name, String msg, long interval) {
            this.name = name;
            this.msg = msg;
            this.interval = interval;
            cycling = true;
        }

        public String getName() {
            return name;
        }

        private void stop() {
            cycling = false;
        }

        @Override
        public void run() {
            while (cycling && bot.isConnected()) {
                bot.sendMessage(msg);
                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException ex) {
                }
            }
        }

    }

}
