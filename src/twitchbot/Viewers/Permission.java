package twitchbot.Viewers;

public enum Permission {
    BROADCASTER(3), MODERATOR(2), SUBSCRIBER(1), NORMAL(0);
    
    private int value;

    private Permission(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
}
