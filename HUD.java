/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class manages and renders UI elements like the health bar, wave progress bar, timer, etc.
*/

import java.awt.*;

public class HUD {
    // The health bar component of the HUD, which displays the player's health.
    private final HealthBar HEALTH_BAR;
    // The wave progress bar component of the HUD, which shows the progress of the
    // current wave.
    private final WaveProgressBar WAVE_PROGRESS_BAR;
    // The game timer component of the HUD, which tracks and displays elapsed time.
    private final GameTimer GAME_TIMER;

    /**
     * Constructs the HUD with the specified player and font.
     * Initializes the health bar, wave progress bar, and game timer.
     */
    public HUD(Player player, Font font) {
        this.HEALTH_BAR = new HealthBar(player);
        this.WAVE_PROGRESS_BAR = new WaveProgressBar(font);
        this.GAME_TIMER = new GameTimer(font);
    }

    /**
     * Starts the game timer.
     */
    public void startTimer() {
        GAME_TIMER.start();
    }

    /**
     * Stops the game timer.
     */
    public void stopTimer() {
        GAME_TIMER.stop();
    }

    /**
     * Resets the game timer to its initial state.
     */
    public void resetTimer() {
        GAME_TIMER.reset();
    }

    /**
     * Updates the wave progress bar with the number of enemies defeated
     * and the total number of enemies required to complete the wave.
     */
    public void updateWaveProgress(int enemiesDefeated, int totalRequired) {
        WAVE_PROGRESS_BAR.updateProgress(enemiesDefeated, totalRequired);
    }

    /**
     * Sets the current wave number in the wave progress bar.
     */
    public void setCurrentWave(int wave) {
        WAVE_PROGRESS_BAR.setCurrentWave(wave);
    }

    /**
     * Retrieves the elapsed time in milliseconds from the game timer.
     */
    public long getElapsedTimeMillis() {
        return GAME_TIMER.getElapsedTimeMillis();
    }

    /**
     * Draws the HUD components (health bar, wave progress bar, and game timer)
     * on the screen using the provided Graphics2D object and screen width.
     */
    public void draw(Graphics2D g2d, int screenWidth) {
        HEALTH_BAR.draw(g2d);
        WAVE_PROGRESS_BAR.draw(g2d, screenWidth);
        GAME_TIMER.draw(g2d, screenWidth);
    }
}
