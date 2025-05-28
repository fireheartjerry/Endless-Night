import java.awt.*;

public class HUD {
    private final HealthBar HEALTH_BAR;
    private final WaveProgressBar WAVE_PROGRESS_BAR;
    private final GameTimer GAME_TIMER;
    
    public HUD(Player player, Font font) {
        this.HEALTH_BAR = new HealthBar(player);
        this.WAVE_PROGRESS_BAR = new WaveProgressBar(font);
        this.GAME_TIMER = new GameTimer(font);
    }
    
    public void startTimer() {
        GAME_TIMER.start();
    }
    
    public void stopTimer() {
        GAME_TIMER.stop();
    }
    
    public void resetTimer() {
        GAME_TIMER.reset();
    }
    
    public void updateWaveProgress(int enemiesDefeated, int totalRequired) {
        WAVE_PROGRESS_BAR.updateProgress(enemiesDefeated, totalRequired);
    }
    
    public void setCurrentWave(int wave) {
        WAVE_PROGRESS_BAR.setCurrentWave(wave);
    }
    
    public long getElapsedTimeMillis() {
        return GAME_TIMER.getElapsedTimeMillis();
    }
    
    public void draw(Graphics2D g2d, int screenWidth) {
        HEALTH_BAR.draw(g2d);
        WAVE_PROGRESS_BAR.draw(g2d, screenWidth);
        GAME_TIMER.draw(g2d, screenWidth);
    }
}
