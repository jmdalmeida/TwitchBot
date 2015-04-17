package twitchbot.Modules.Topic;

import java.util.HashMap;
import java.util.Map;
import twitchbot.Commands.ChatFunction;
import twitchbot.Modules.BotModule;
import twitchbot.TwitchBot;
import twitchbot.Viewers.Permission;

public class Topic extends BotModule {

    private String topic;
    private boolean topicSet;

    public Topic(TwitchBot bot) {
        super(bot);
    }

    public void setTopic(String topic) {
        topicSet = true;
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }

    public void deleteTopic() {
        topicSet = false;
        topic = null;
    }

    public boolean isTopicSet() {
        return topicSet;
    }

    @Override
    public Map<String, ChatFunction> getModuleCommands() {
        Map<String, ChatFunction> cmds = new HashMap<>();
        cmds.put("!topic", new ChatFunction(Permission.NORMAL, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                if (isTopicSet()) {
                    bot.botMessage("Topic: " + getTopic());
                }
            }

        });
        cmds.put("!settopic", new ChatFunction(Permission.BROADCASTER, true) {

            @Override
            public void function(String channel, String sender, String login, String hostname, String message) {
                if (message.trim().length() > 10) {
                    setTopic(message.substring(10));
                } else {
                    deleteTopic();
                }
            }

        });
        return cmds;
    }

}
