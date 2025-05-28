import java.awt.*;
import java.awt.geom.*;

public class HealthBar {
    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 10;
    private static final int Y_OFFSET = 15; // Distance below the player

    private final Player PLAYER;
    private final Color BORDER_COLOR = Color.WHITE;
    private final Color BACKGROUND_COLOR = new Color(60, 60, 60, 200);
    private final Color HEALTH_COLOR = new Color(200, 150, 255);

    public HealthBar(Player player) {
        this.PLAYER = player;
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Compute bar position centered under the player
        int barX = (int) (PLAYER.getX() + PLAYER.getWidth() / 2.0 - BAR_WIDTH / 2.0);
        int barY = (int) (PLAYER.getY() + PLAYER.getHeight() + Y_OFFSET);

        float fraction = (float) PLAYER.getHp() / PLAYER.getMaxHp();
        fraction = Math.max(0f, Math.min(1f, fraction));

        int arc = 10;
        int shadowOffset = 3;
        Color shadowColor = new Color(0, 0, 0, 80);

        g2d.setColor(shadowColor);
        g2d.fillRoundRect(barX + shadowOffset,
                barY + shadowOffset,
                BAR_WIDTH,
                BAR_HEIGHT,
                arc, arc);

        // 2) Draw background track
        RoundRectangle2D track = new RoundRectangle2D.Float(
                barX, barY, BAR_WIDTH, BAR_HEIGHT, arc, arc);
        g2d.setColor(BACKGROUND_COLOR.darker());
        g2d.fill(track);

        int fillWidth = (int) (BAR_WIDTH * fraction);
        if (fillWidth > 0) {
            GradientPaint grad = new GradientPaint(
                    barX, barY, HEALTH_COLOR.brighter(),
                    barX, barY + BAR_HEIGHT, HEALTH_COLOR.darker());
            g2d.setPaint(grad);
            g2d.fillRoundRect(barX, barY, fillWidth, BAR_HEIGHT, arc, arc);
        }

        int glossHeight = BAR_HEIGHT / 3;
        GradientPaint gloss = new GradientPaint(
                barX, barY, new Color(255, 255, 255, 180),
                barX, barY + glossHeight, new Color(255, 255, 255, 30));
        g2d.setPaint(gloss);
        g2d.fillRoundRect(barX, barY, BAR_WIDTH, glossHeight + arc / 2, arc, arc);

        g2d.setColor(BORDER_COLOR);
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(track);

        g2d.setPaint(null);
    }

}
