
import java.awt.*;
import javax.swing.*;

public class GameButton extends JButton {

    private static final Color PURPLE = new Color(137, 100, 255);
    private static final int ARC = 40;

    GameButton(String txt) {
        super(txt);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(PURPLE);
        g2.fillRoundRect(0, 0, w, h, ARC, ARC);

        g2.setStroke(new BasicStroke(4f));
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(0, 0, w, h, ARC, ARC);

        super.paintComponent(g2);
        g2.dispose();
    }
}
