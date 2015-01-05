package twitchbot.Modules.Topic;

public class Topic {
    private String topic;
    private boolean topicSet;

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
    
}
