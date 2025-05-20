import java.awt.*;
import javax.swing.*;

public class PlayScreen extends JPanel {
    
    private final GamePanel parent;
    private Font ui_font;
    
    public PlayScreen(GamePanel parent) {
        this.parent = parent;
        setLayout(null);
        setOpaque(true);
        setBackground(Color.BLACK);
        
        load_font();
        
        JLabel title = new JLabel("Game Screen");
        title.setFont(ui_font.deriveFont(Font.BOLD, 64f));
        title.setForeground(Color.WHITE);
        title.setBounds(60, 40, 1700, 70);
        add(title);
        
        JLabel instructions = new JLabel("This is where the actual game will be implemented.");
        instructions.setFont(ui_font.deriveFont(28f));
        instructions.setForeground(Color.WHITE);
        instructions.setBounds(60, 140, 1800, 80);
        add(instructions);
        
        add_button("Back to Intro", 60, 260, () -> parent.showScreen("intro"));
    }
    
    private void load_font() {
        try {
            ui_font = Font.createFont(Font.TRUETYPE_FONT,
                    getClass().getResourceAsStream("/assets/gamefont.ttf"));
        } catch (Exception ex) {
            ui_font = new Font("SansSerif", Font.PLAIN, 28);
        }
    }
    
    private void add_button(String txt, int x, int y, Runnable on_click) {
        JButton b = new JButton(txt);
        b.setFont(ui_font.deriveFont(24f));
        b.setForeground(Color.WHITE);
        b.setBackground(new Color(120, 85, 240));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setBounds(x, y, 180, 36);
        b.setOpaque(true);
        b.addActionListener(e -> on_click.run());
        add(b);
    }
}
