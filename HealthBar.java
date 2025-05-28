import java.awt.*;
import java.awt.geom.*;

public class HealthBar {
    // Constants for the dimensions and positioning of the health bar
    private static final int BAR_WIDTH = 80; // Width of the health bar
    private static final int BAR_HEIGHT = 10; // Height of the health bar
    private static final int Y_OFFSET = 15; // Distance below the player

    // Reference to the player whose health is being displayed
    private final Player PLAYER;

    // Colors used for the health bar
    private final Color BORDER_COLOR = Color.WHITE; // Color of the border
    private final Color BACKGROUND_COLOR = new Color(60, 60, 60, 200); // Background color of the bar
    private final Color HEALTH_COLOR = new Color(200, 150, 255); // Color representing the health

    // Constructor to initialize the health bar with the player
    public HealthBar(Player player) {
        this.PLAYER = player;
    }

    // Method to draw the health bar
    public void draw(Graphics2D g2d) {
        // Enable anti-aliasing for smoother rendering
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Compute the position of the health bar centered below the player
        int barX = (int) (PLAYER.getX() + PLAYER.getWidth() / 2.0 - BAR_WIDTH / 2.0);
        int barY = (int) (PLAYER.getY() + PLAYER.getHeight() + Y_OFFSET);

        // Calculate the fraction of health remaining (clamped between 0 and 1)
        float fraction = (float) PLAYER.getHp() / PLAYER.getMaxHp();
        fraction = Math.max(0f, Math.min(1f, fraction));

        // Rounded corner radius and shadow offset
        int arc = 10;
        int shadowOffset = 3;
        Color shadowColor = new Color(0, 0, 0, 80); // Shadow color

        // Draw shadow for the health bar
        g2d.setColor(shadowColor);
        g2d.fillRoundRect(barX + shadowOffset,
                barY + shadowOffset,
                BAR_WIDTH,
                BAR_HEIGHT,
                arc, arc);

        // Draw the background track of the health bar
        RoundRectangle2D track = new RoundRectangle2D.Float(
                barX, barY, BAR_WIDTH, BAR_HEIGHT, arc, arc);
        g2d.setColor(BACKGROUND_COLOR.darker());
        g2d.fill(track);

        // Calculate the width of the filled portion based on health fraction
        int fillWidth = (int) (BAR_WIDTH * fraction);
        if (fillWidth > 0) {
            // Create a gradient for the health fill
            GradientPaint grad = new GradientPaint(
                    barX, barY, HEALTH_COLOR.brighter(),
                    barX, barY + BAR_HEIGHT, HEALTH_COLOR.darker());
            g2d.setPaint(grad);
            g2d.fillRoundRect(barX, barY, fillWidth, BAR_HEIGHT, arc, arc);
        }

        // Add a glossy effect to the health bar
        int glossHeight = BAR_HEIGHT / 3;
        GradientPaint gloss = new GradientPaint(
                barX, barY, new Color(255, 255, 255, 180),
                barX, barY + glossHeight, new Color(255, 255, 255, 30));
        g2d.setPaint(gloss);
        g2d.fillRoundRect(barX, barY, BAR_WIDTH, glossHeight + arc / 2, arc, arc);

        // Draw the border of the health bar
        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(track);

        // Reset the paint to default
        g2d.setPaint(null);
    }
}
