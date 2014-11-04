package twitchbot;

interface DoFunction {
    void doFunction(String channel, String sender, String login, String hostname);
}

public abstract class ChatFunction implements DoFunction {}
