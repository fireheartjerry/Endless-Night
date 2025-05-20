import java.awt.*;
import javax.swing.*;

public class GameFrame extends JFrame {

    GamePanel panel;
    public GameFrame() {
        panel = new GamePanel();
        this.add(panel);
        this.setTitle("Endless Night");
        this.setResizable(false);
        this.setBackground(Color.BLACK);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ImageIcon icon = new ImageIcon("./assets/images/logo.png");
        this.setIconImage(icon.getImage());

        this.pack();
        this.setVisible(true);
        this.setLocationRelativeTo(null);
    }
}
