package twitchbot.Viewers;

public class Viewer {
    
    private final String username;
    private Permission permissionLevel;

    public Viewer(String username, Permission permissionLevel) {
        this.username = username;
        this.permissionLevel = permissionLevel;
    }

    public String getUsername() {
        return username;
    }

    public Permission getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Permission permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    @Override
    public String toString() {
        return username + " (" + permissionLevel.toString() + ")";
    }

}