import java.awt.*;

public class HUD {
    private final HealthBar healthBar;
    private final WaveProgressBar waveProgressBar;
    private final GameTimer gameTimer;
    
    public HUD(Player player, Font font) {
        this.healthBar = new HealthBar(player);
        this.waveProgressBar = new WaveProgressBar(font);
        this.gameTimer = new GameTimer(font);
    }
    
    public void startTimer() {
        gameTimer.start();
    }
    
    public void stopTimer() {
        gameTimer.stop();
    }
    
    public void resetTimer() {
        gameTimer.reset();
    }
    
    public void updateWaveProgress(int enemiesDefeated, int totalRequired) {
        waveProgressBar.updateProgress(enemiesDefeated, totalRequired);
    }
    
    public void setCurrentWave(int wave) {
        waveProgressBar.setCurrentWave(wave);
    }
    
    public long getElapsedTimeMillis() {
        return gameTimer.getElapsedTimeMillis();
    }
    
    public void draw(Graphics2D g2d, int screenWidth) {
        healthBar.draw(g2d);
        waveProgressBar.draw(g2d, screenWidth);
        gameTimer.draw(g2d, screenWidth);
    }
}
