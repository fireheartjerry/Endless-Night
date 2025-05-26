import java.awt.*;
import java.awt.geom.*;

public class HealthBar {
    private static final int BAR_WIDTH = 80;
    private static final int BAR_HEIGHT = 10;
    private static final int Y_OFFSET = 15; // Distance below the player

    private final Player player;
    private final Color borderColor = Color.WHITE;
    private final Color backgroundColor = new Color(60, 60, 60, 200);
    private final Color healthColor = new Color(200, 150, 255);

    public HealthBar(Player player) {
        this.player = player;
    }

    public void draw(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Compute bar position centered under the player
        int barX = (int) (player.getX() + player.getWidth() / 2.0 - BAR_WIDTH / 2.0);
        int barY = (int) (player.getY() + player.getHeight() + Y_OFFSET);

        float fraction = (float) player.getHp() / player.getMaxHp();
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
        g2d.setColor(backgroundColor.darker());
        g2d.fill(track);

        int fillWidth = (int) (BAR_WIDTH * fraction);
        if (fillWidth > 0) {
            GradientPaint grad = new GradientPaint(
                    barX, barY, healthColor.brighter(),
                    barX, barY + BAR_HEIGHT, healthColor.darker());
            g2d.setPaint(grad);
            g2d.fillRoundRect(barX, barY, fillWidth, BAR_HEIGHT, arc, arc);
        }

        int glossHeight = BAR_HEIGHT / 3;
        GradientPaint gloss = new GradientPaint(
                barX, barY, new Color(255, 255, 255, 180),
                barX, barY + glossHeight, new Color(255, 255, 255, 30));
        g2d.setPaint(gloss);
        g2d.fillRoundRect(barX, barY, BAR_WIDTH, glossHeight + arc / 2, arc, arc);

        g2d.setColor(borderColor);
        g2d.setStroke(new BasicStroke(2f));
        g2d.draw(track);

        g2d.setPaint(null);
    }

}
