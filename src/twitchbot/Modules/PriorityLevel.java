/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package twitchbot.Modules;

/**
 *
 * @author Joao
 */
public enum PriorityLevel {

    MAJOR(3), NORMAL(2), MINOR(1);
    int level;

    private PriorityLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

}
