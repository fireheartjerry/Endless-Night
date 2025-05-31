/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class defines a skill with things like levels, cooldown, and instantaneous behaviour in game
*/

public class Skill {
    private String name;
    private int level;
    private int cooldownTime;
    private long lastActivationTime;
    private boolean isPassive;
    
    public Skill(String name, int level, int cooldownTime, boolean isPassive) {
        this.name = name;
        this.level = level;
        this.cooldownTime = cooldownTime;
        this.isPassive = isPassive;
        this.lastActivationTime = 0;
    }
    
    public String getName() {
        return name;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public boolean isPassive() {
        return isPassive;
    }
    
    public boolean isReady() {
        return System.currentTimeMillis() - lastActivationTime >= cooldownTime;
    }
    
    public void activate() {
        lastActivationTime = System.currentTimeMillis();
    }
    
    public int getCooldownTime() {
        return cooldownTime;
    }
}
