import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class PlayScreen extends JPanel {

    private static final int FPS = 60;
    private static final int HUD_PADDING = 20;
    private static final int BTN_W_SMALL = 100, BTN_H_SMALL = 40;
    private static final int BTN_W_BIG = 200, BTN_H_BIG = 60;

    private final GamePanel PARENT;
    private final Font UI_FONT;
    private BufferedImage backgroundImage;

    public PlayScreen(GamePanel parent) {
        PARENT = parent;
        setLayout(null);
        setOpaque(false);
        UI_FONT = PARENT.getGameFont();

        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(0, 0, getWidth(), getHeight());
        // draw entities
        PARENT.player.draw(g2);
        for (Enemy enemy : PARENT.enemies) {
            enemy.draw(g2);
        }
        PARENT.hud.draw(g2, getWidth());

    }

    @Override
    public void invalidate() {
        super.invalidate();
    }
}
