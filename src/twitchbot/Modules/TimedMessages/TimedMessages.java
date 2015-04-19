package twitchbot.Modules.TimedMessages;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import twitchbot.Commands.ChatCommands;
import twitchbot.Commands.ChatFunction;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class TimedMessages extends BotModule {

    private static final long IDLE_SLEEP = 2 * 60 * 60 * 1000;

    //private final Map<String, CycleCommand> messages;
    private final Map<String, Message> messages;

    private Thread thread;
    private long lastTime;

    public TimedMessages(TwitchBot bot) {
        super(bot);
        lastTime = new Date().getTime();
        messages = new HashMap<>();
        setupThread();
    }

    private void setupThread() {
        thread = new Thread(() -> {
            while (bot.isConnected()) {
                long to_sleep = IDLE_SLEEP;
                long now = new Date().getTime();
                if (!messages.isEmpty()) {
                    // Process current message
                    messages.values().stream().forEach((m) -> {
                        if (m.getTimeleft() <= 0) {
                            bot.botMessage(m.getMsg());
                            m.reset();
                        } else {
                            m.update(now);
                        }
                    });
                    // Prepare next message
                    long closerTime = 0;
                    for (Message m : messages.values()) {
                        if (closerTime == 0) {
                            closerTime = m.getTimeleft();
                        } else {
                            if (m.getTimeleft() < closerTime) {
                                closerTime = m.getTimeleft();
                            }
                        }
                    }
                    to_sleep = closerTime;
                    if (to_sleep < 1) {
                        to_sleep = 1;
                    }
                }
                lastTime = now;
                try {
                    Thread.sleep(to_sleep);
                } catch (InterruptedException ex) {
                    System.out.println("Interrupting TimedMessages.");
                }
            }
        });
    }

    public void addNewCommand(String name, String msg, long interval) {
        String keyL = name.toLowerCase();
        if (messages.containsKey(keyL)) {
            Message m = messages.get(keyL);
            m.setMsg(msg);
            m.setInterval(interval);
        } else {
            Message m = new Message(keyL, msg, interval);
            messages.put(keyL, m);
        }
        reset();
    }

    public void removeMessage(String name) {
        String keyL = name.toLowerCase();
        if (messages.containsKey(keyL)) {
            messages.remove(keyL);
            reset();
        }
    }

    private void removeAllMessages() {
        Object[] ms = messages.values().toArray();
        for (int i = 0; i < ms.length; i++) {
            Message m = (Message) ms[0];
            messages.remove(m.getName());
        }
        reset();
    }

    private void reset() {
        thread.interrupt();
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        //!setmessage <name> <interval(seconds)> <message>
        cmds.put("!setmessage", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                if (tokens.length < 4) {
                    return;
                }
                String[] exc = {tokens[0], tokens[1], tokens[2]};
                String msg = ChatCommands.extractMessage(exc, message);
                addNewCommand(tokens[1], msg, Long.parseLong(tokens[2]));
            }

        });
        //!removemessage <name>
        cmds.put("!removemessage", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                String[] tokens = message.split(" ");
                if (tokens[1] != null) {
                    removeMessage(tokens[1]);
                }
            }

        });
        return cmds;
    }

    @Override
    public void onJoin(String channel, String sender, String login, String hostname) {
        if (bot.isMe(sender)) {
            thread.start();
        }
    }

    @Override
    public void onDisconnect() {
        removeAllMessages();
    }

    private final class Message {

        private final String name;
        private long interval;
        private String msg;
        private long timeleft;

        public Message(String name, String msg, long interval) {
            this.name = name;
            this.msg = msg;
            this.interval = interval;
            this.timeleft = interval;
        }

        public long getInterval() {
            return interval;
        }

        public void setInterval(long interval) {
            this.interval = interval;
        }

        public long getTimeleft() {
            return timeleft;
        }

        public String getName() {
            return name;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public void update(long now) {
            long diff = now - lastTime;
            this.timeleft -= diff;
        }

        public void reset() {
            this.timeleft = interval;
        }

        @Override
        public String toString() {
            return name + " (" + interval + "s): " + timeleft;
        }

    }

}
