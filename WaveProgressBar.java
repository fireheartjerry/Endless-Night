import java.awt.*;

public class WaveProgressBar {
    private static final int BAR_HEIGHT = 20;
    private static final int TOP_MARGIN = 20;
    private static final int SIDE_MARGIN = 40;
    
    private final Font WAVE_FONT;
    private int currentEnemiesDefeated;
    private int totalEnemiesRequired;
    private int currentWave;
    
    private final Color BACKGROUND_COLOR = new Color(60, 60, 60, 200);
    private final Color PROGRESS_COLOR = new Color(140, 200, 255);
    private final Color BORDER_COLOR = Color.WHITE;
    private final Color TEXT_COLOR = Color.WHITE;
    
    public WaveProgressBar(Font font) {
        this.wave_Font = font.deriveFont(18f);
        this.currentEnemiesDefeated = 0;
        this.totalEnemiesRequired = 10; // Default, can be updated
        this.currentWave = 1;
    }
    
    public void updateProgress(int enemiesDefeated, int totalRequired) {
        this.currentEnemiesDefeated = enemiesDefeated;
        this.totalEnemiesRequired = totalRequired;
    }
    
    public void setCurrentWave(int wave) {
        this.currentWave = wave;
    }
    
    public void draw(Graphics2D g2d, int screenWidth) {
        // Calculate position (centered at top of screen)
        int barWidth = screenWidth - (2 * SIDE_MARGIN);
        int barX = SIDE_MARGIN;
        int barY = TOP_MARGIN;
        
        // Draw the background
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(barX, barY, barWidth, BAR_HEIGHT);
        
        // Calculate progress percentage
        float progressPercentage = (float) currentEnemiesDefeated / totalEnemiesRequired;
        progressPercentage = Math.min(1.0f, progressPercentage); // Cap at 100%
        int progressWidth = (int) (barWidth * progressPercentage);
        
        // Draw the progress portion
        g2d.setColor(PROGRESS_COLOR);
        g2d.fillRect(barX, barY, progressWidth, BAR_HEIGHT);
        
        // Draw the border
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(barX, barY, barWidth, BAR_HEIGHT);
        
        // Draw text
        String waveText = "Wave " + currentWave + "  -  " + 
                           currentEnemiesDefeated + " / " + totalEnemiesRequired;
        
        g2d.setFont(WAVE_FONT);
        g2d.setColor(TEXT_COLOR);
        
        // Center the text
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(waveText);
        int textX = barX + (barWidth - textWidth) / 2;
        int textY = barY + ((BAR_HEIGHT - fm.getHeight()) / 2) + fm.getAscent();
        
        g2d.drawString(waveText, textX, textY);
    }
}
