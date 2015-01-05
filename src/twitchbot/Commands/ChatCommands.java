package twitchbot.Commands;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;
import twitchbot.Viewers.Viewer;

public class ChatCommands {

    private final TwitchBot bot;
    private Map<String, ChatFunction> commands;

    public ChatCommands(TwitchBot bot) {
        this.bot = bot;
        setupCommands();
    }

    private void setupCommands() {
        commands = new HashMap<>();
        commands.put("!hello", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                bot.sendMessage(channel, "Hello " + sender + ", how are you?");
            }

        });
        commands.put("!date", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                bot.sendMessage(channel, "The current date is " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + ".");
            }

        });
        commands.put("!time", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                bot.sendMessage(channel, "The current time is " + new SimpleDateFormat("HH:mm:ss").format(new Date()) + " GMT.");
            }

        });
        commands.put("!commands", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                String cmd = commands.keySet().toString();
                bot.sendMessage(channel, "Available commands are: " + cmd.substring(1, cmd.length() - 1) + ".");
            }

        });
        commands.put("!uptime", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                long elapsedTime = System.nanoTime() - bot.getConnectedTimestamp();
                final long hr = TimeUnit.NANOSECONDS.toHours(elapsedTime);
                final long min = TimeUnit.NANOSECONDS.toMinutes(elapsedTime);
                bot.sendMessage(channel, "I've been up for " + String.format("%02d hours, %02d minutes", hr, min) + ".");
            }

        });
        commands.put("!debug", new ChatFunction(Permission.BROADCASTER) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                bot.viewers.listViewers();
            }

        });
        commands.put("!quit", new ChatFunction(Permission.BROADCASTER) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                bot.quitAndExit();
            }

        });
        commands.put("!topic", new ChatFunction(Permission.NORMAL) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                if (bot.topic.isTopicSet()) {
                    bot.sendMessage(channel, "Topic: " + bot.topic.getTopic());
                }
            }

        });
        commands.put("!settopic", new ChatFunction(Permission.BROADCASTER) {

            @Override
            public void doFunction(String channel, String sender, String login, String hostname, String message) {
                if (message.trim().length() > 10) {
                    bot.topic.setTopic(message.substring(10));
                } else {
                    bot.topic.deleteTopic();
                }
            }
            
        });
    }

    public void runCommand(String channel, String sender, String login, String hostname, String message) {
        String[] tokens = message.split(" ");
        String cmd = tokens[0];
        if (commands.containsKey(cmd)) {
            new Thread(() -> {
                ChatFunction func = commands.get(cmd);
                if (func.getPermission() != Permission.NORMAL) {
                    Viewer u = bot.viewers.getViewer(sender);
                    if (u != null) {
                        if (func.getPermission().getValue() <= u.getPermissionLevel().getValue()) {
                            func.doFunction(channel, sender, login, hostname, message);
                        } else {
                            bot.sendMessage(channel, "You don't have permission to run this command, " + sender + ".");
                        }
                    } else {
                        System.err.println("Error processing " + sender + "'s command (" + cmd + "): Viewer not listed.");
                    }
                } else {
                    func.doFunction(channel, sender, login, hostname, message);
                }
            }).start();
        }
    }

}
