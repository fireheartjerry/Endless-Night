import java.awt.*;
import java.text.DecimalFormat;

public class GameTimer {
    private static final int TOP_MARGIN = 50;
    private static final int RIGHT_MARGIN = 20;
    
    private long startTimeMillis;
    private boolean running;
    private final Font TIMER_FONT;
    private final Color TEXT_COLOR = Color.WHITE;
    private final Color SHADOW_COLOR = new Color(0, 0, 0, 150);
    
    private final DecimalFormat TIME_FORMAT = new DecimalFormat("00");
    
    public GameTimer(Font font) {
        this.TIMER_FONT = font.deriveFont(24f);
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
            timeString = hours + ":" + TIME_FORMAT.format(minutes) + ":" + TIME_FORMAT.format(seconds);
        } else {
            timeString = minutes + ":" + TIME_FORMAT.format(seconds);
        }
        
        g2d.setFont(TIMER_FONT);
        
        // Calculate position (top-right of screen)
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(timeString);
        int textX = screenWidth - textWidth - RIGHT_MARGIN;
        int textY = TOP_MARGIN + fm.getAscent();
        
        // Draw text shadow for better visibility
        g2d.setColor(SHADOW_COLOR);
        g2d.drawString(timeString, textX + 2, textY + 2);
        
        // Draw timer text
        g2d.setColor(TEXT_COLOR);
        g2d.drawString(timeString, textX, textY);
    }
}
