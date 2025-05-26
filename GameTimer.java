import java.awt.*;
import java.text.DecimalFormat;

public class GameTimer {
    private static final int TOP_MARGIN = 50;
    private static final int RIGHT_MARGIN = 20;
    
    private long startTimeMillis;
    private boolean running;
    private final Font timerFont;
    private final Color textColor = Color.WHITE;
    private final Color shadowColor = new Color(0, 0, 0, 150);
    
    private final DecimalFormat timeFormat = new DecimalFormat("00");
    
    public GameTimer(Font font) {
        this.timerFont = font.deriveFont(24f);
        this.running = false;
    }
    
    public void start() {
        startTimeMillis = System.currentTimeMillis();
        running = true;
    }
    
    public void stop() {
        running = false;
    }
    
    public void reset() {
        startTimeMillis = System.currentTimeMillis();
    }
    
    public long getElapsedTimeMillis() {
        if (!running) {
            return 0;
        }
        return System.currentTimeMillis() - startTimeMillis;
    }
    
    public void draw(Graphics2D g2d, int screenWidth) {
        if (!running) {
            return;
        }
        
        long elapsedMillis = getElapsedTimeMillis();
        long seconds = (elapsedMillis / 1000) % 60;
        long minutes = (elapsedMillis / (1000 * 60)) % 60;
        long hours = elapsedMillis / (1000 * 60 * 60);
        
        String timeString;
        if (hours > 0) {
            timeString = hours + ":" + timeFormat.format(minutes) + ":" + timeFormat.format(seconds);
        } else {
            timeString = minutes + ":" + timeFormat.format(seconds);
        }
        
        g2d.setFont(timerFont);
        
        // Calculate position (top-right of screen)
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeString);
        int textX = screenWidth - textWidth - RIGHT_MARGIN;
        int textY = TOP_MARGIN + fm.getAscent();
        
        // Draw text shadow for better visibility
        g2d.setColor(shadowColor);
        g2d.drawString(timeString, textX + 2, textY + 2);
        
        // Draw timer text
        g2d.setColor(textColor);
        g2d.drawString(timeString, textX, textY);
    }
}
