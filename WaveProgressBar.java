import java.awt.*;

// This class represents a progress bar that visually displays the progress of enemies defeated in a wave-based game.
public class WaveProgressBar {
    // Constants for the height of the progress bar and margins around it
    private static final int BAR_HEIGHT = 20;
    private static final int TOP_MARGIN = 20;
    private static final int SIDE_MARGIN = 40;

    // Font used for displaying wave and progress information
    private final Font WAVE_FONT;

    // Variables to track the current progress and wave information
    private int currentEnemiesDefeated; // Number of enemies defeated in the current wave
    private int totalEnemiesRequired; // Total number of enemies required to complete the wave
    private int currentWave; // Current wave number

    // Colors used for the progress bar's background, progress, border, and text
    private final Color BACKGROUND_COLOR = new Color(60, 60, 60, 200); // Semi-transparent dark background
    private final Color PROGRESS_COLOR = new Color(140, 200, 255); // Light blue color for progress
    private final Color BORDER_COLOR = Color.WHITE; // White border color
    private final Color TEXT_COLOR = Color.WHITE; // White text color

    // Constructor to initialize the progress bar with a specific font
    public WaveProgressBar(Font font) {
        // Derive a slightly larger font size for the wave text
        this.WAVE_FONT = font.deriveFont(18f);
        this.currentEnemiesDefeated = 0; // Initialize defeated enemies to 0
        this.totalEnemiesRequired = 10; // Default total enemies required for a wave
        this.currentWave = 1; // Start at wave 1
    }

    // Updates the progress bar with the current number of enemies defeated and the
    // total required
    public void updateProgress(int enemiesDefeated, int totalRequired) {
        this.currentEnemiesDefeated = enemiesDefeated; // Update the number of defeated enemies
        this.totalEnemiesRequired = totalRequired; // Update the total enemies required
    }

    // Sets the current wave number
    public void setCurrentWave(int wave) {
        this.currentWave = wave; // Update the current wave number
    }

    // Draws the progress bar on the screen
    public void draw(Graphics2D g2d, int screenWidth) {
        // Calculate the width and position of the progress bar, centered at the top of
        // the screen
        int barWidth = screenWidth - (2 * SIDE_MARGIN); // Width of the bar, accounting for side margins
        int barX = SIDE_MARGIN; // X-coordinate of the bar
        int barY = TOP_MARGIN; // Y-coordinate of the bar

        // Draw the background rectangle for the progress bar
        g2d.setColor(BACKGROUND_COLOR);
        g2d.fillRect(barX, barY, barWidth, BAR_HEIGHT);

        // Calculate the progress percentage based on enemies defeated
        float progressPercentage = (float) currentEnemiesDefeated / totalEnemiesRequired;
        progressPercentage = Math.min(1.0f, progressPercentage); // Ensure the percentage does not exceed 100%
        int progressWidth = (int) (barWidth * progressPercentage); // Calculate the width of the progress portion

        // Draw the progress portion of the bar
        g2d.setColor(PROGRESS_COLOR);
        g2d.fillRect(barX, barY, progressWidth, BAR_HEIGHT);

        // Draw the border around the progress bar
        g2d.setColor(BORDER_COLOR);
        g2d.drawRect(barX, barY, barWidth, BAR_HEIGHT);

        // Create the text to display the wave and progress information
        String waveText = "Wave " + currentWave + "  -  " +
                currentEnemiesDefeated + " / " + totalEnemiesRequired;

        // Set the font and color for the text
        g2d.setFont(WAVE_FONT);
        g2d.setColor(TEXT_COLOR);

        // Center the text within the progress bar
        FontMetrics fm = g2d.getFontMetrics(); // Get font metrics for text measurement
        int textWidth = fm.stringWidth(waveText); // Calculate the width of the text
        int textX = barX + (barWidth - textWidth) / 2; // X-coordinate to center the text
        int textY = barY + ((BAR_HEIGHT - fm.getHeight()) / 2) + fm.getAscent(); // Y-coordinate for vertical centering

        // Draw the text on the progress bar
        g2d.drawString(waveText, textX, textY);
    }
}
