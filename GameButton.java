import java.awt.*;
import javax.swing.*;

public class GameButton extends JButton {

    private Color bg_colour;

    GameButton(String txt) {
        super(txt);
        bg_colour = new Color(140, 82, 255);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setForeground(Color.WHITE);
    }

    public void setColor(Color color) {
        bg_colour = color;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        g2.setColor(bg_colour);
        g2.fillRect(0, 0, w, h);

        super.paintComponent(g2); // Call super to ensure text and other components are painted

        g2.setStroke(new BasicStroke(4f));
        g2.setColor(Color.WHITE);
        g2.drawRect(0, 0, w - 1, h - 1); // Draw the black border

        g2.dispose();
    }
}
