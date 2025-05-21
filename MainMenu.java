
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainMenu extends JPanel {

    private static final int HS_W = 200, HS_H = 50;
    private static final int BTN_H_SMALL = 100, BTN_W_SMALL = 300;
    private static final int BTN_H_BIG = 140, BTN_W_BIG = 400;

    private final GamePanel PARENT;
    private final Font UI_FONT;
    private final JLabel TITLE_LABEL;
    private final GameButton highscores_button;
    private final GameButton how_button;
    private final GameButton credits_button;
    private final GameButton start_button;
    private BufferedImage background_image;

    public MainMenu(GamePanel parent) {
        PARENT = parent;
        setLayout(null);
        setOpaque(false);
        UI_FONT = parent.getGameFont();

        TITLE_LABEL = new JLabel("Endless Night");
        TITLE_LABEL.setFont(UI_FONT.deriveFont(Font.BOLD, 90f));
        TITLE_LABEL.setForeground(Color.WHITE);
        add(TITLE_LABEL);

        highscores_button = makeMenuButton("Highscores", 20f, e -> PARENT.showScreen("scores"));
        highscores_button.setColor(new Color(135, 123, 182));
        add(highscores_button);
        how_button = makeMenuButton("HOW 2 PLAY", 35f, e -> PARENT.game_state = GameState.HOW_TO_PLAY);
        add(how_button);

        credits_button = makeMenuButton("CREDITS", 40f, e -> PARENT.game_state = GameState.CREDITS);
        add(credits_button);

        start_button = makeMenuButton("START", 64f, e -> {
            PARENT.game_state = GameState.PLAYING;
        });
        start_button.setColor(new Color(76, 72, 144));
        add(start_button);

        loadBackground();
    }

    private void loadBackground() {
        try {
            background_image = ImageIO.read(getClass().getResourceAsStream("/assets/images/backgrounds/mainmenubg.jpg"));
        } catch (IOException e) {
            background_image = null;
        }
    }

    private GameButton makeMenuButton(String text, float size, ActionListener action) {
        GameButton b = new GameButton(text);
        b.setFont(UI_FONT.deriveFont(Font.BOLD, size));
        b.addActionListener(action);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(Font.BOLD, size*1.15f));
            }

            public void mouseExited(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(Font.BOLD, size));
            }
        });
        return b;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (background_image != null) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            g2.drawImage(background_image, 0, 0, getWidth(), getHeight(), null);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        positionComponents();
    }

    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH;
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT;

        // title top-left
        TITLE_LABEL.setBounds(20, 10, TITLE_LABEL.getPreferredSize().width, TITLE_LABEL.getPreferredSize().height);

        // highscores top-right
        highscores_button.setBounds(w - HS_W - 60, 40, HS_W, HS_H);

        // bottom buttons
        int y_small = h - BTN_H_SMALL - 60;
        how_button.setBounds(60, y_small, BTN_W_SMALL, BTN_H_SMALL);
        credits_button.setBounds(w - BTN_W_SMALL - 60, y_small, BTN_W_SMALL, BTN_H_SMALL);

        // start button centre bottom
        int x_start = (w - BTN_W_BIG) / 2;
        int y_start = y_small - (BTN_H_BIG - BTN_H_SMALL) / 2 - 20;
        start_button.setBounds(x_start, y_start, BTN_W_BIG, BTN_H_BIG);
    }
}
