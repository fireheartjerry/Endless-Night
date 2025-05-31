/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class draws a timer on the top right of the player screen
*/

import java.awt.*;
import java.text.DecimalFormat;

public class GameTimer {
    // Constants for positioning the timer on the screen
    private static final int TOP_MARGIN = 50; // Distance from the top of the screen
    private static final int RIGHT_MARGIN = 20; // Distance from the right of the screen (missing initialization)

    private long startTimeMillis; // Stores the start time in milliseconds
    private boolean running; // Indicates whether the timer is running
    private final Font TIMER_FONT; // Font used to display the timer
    private final Color TEXT_COLOR = Color.WHITE; // Color of the timer text
    private final Color SHADOW_COLOR = new Color(0, 0, 0, 150); // Semi-transparent shadow color for text

    private final DecimalFormat TIME_FORMAT = new DecimalFormat("00"); // Format for displaying time in two digits

    // Constructor to initialize the timer with a specific font
    public GameTimer(Font font) {
        this.TIMER_FONT = font.deriveFont(24f); // Set font size to 24
        this.running = false; // Timer starts in a stopped state
    }

    // Starts the timer by recording the current time
    public void start() {
        startTimeMillis = System.currentTimeMillis();
        running = true;
    }

    // Stops the timer
    public void stop() {
        running = false;
    }

    // Resets the timer to the current time
    public void reset() {
        startTimeMillis = System.currentTimeMillis();
    }

    // Calculates the elapsed time in milliseconds since the timer started
    public long getElapsedTimeMillis() {
        if (!running) {
            return 0; // If the timer is not running, return 0
        }
        return System.currentTimeMillis() - startTimeMillis;
    }

    // Draws the timer on the screen
    public void draw(Graphics2D g2d, int screenWidth) {
        if (!running) {
            return; // Do nothing if the timer is not running
        }

        // Calculate elapsed time in hours, minutes, and seconds
        long elapsedMillis = getElapsedTimeMillis();
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = elapsedMillis / (1000 * 60 * 60);

        // Format the time string based on whether hours are present
        String timeString;
        if (hours > 0) {
            timeString = hours + ":" + TIME_FORMAT.format(minutes) + ":" + TIME_FORMAT.format(seconds);
        } else {
            timeString = minutes + ":" + TIME_FORMAT.format(seconds);
        }

        g2d.setFont(TIMER_FONT); // Set the font for the timer text

        // Calculate the position of the timer text (top-right corner)
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeString);
        int textX = screenWidth - textWidth - RIGHT_MARGIN;
        int textY = TOP_MARGIN + fm.getAscent();

        // Draw a shadow for the text to improve visibility
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(timeString, textX + 2, textY + 2);

        // Draw the timer text
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(timeString, textX, textY);
    }
}
