import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class IntroScreen extends JPanel {
    private static final String[] LORE = {
        "Welcome to Endless Night, where the sun's descent has led to the beginning of humanity's greatest trial.",
        "Following a catastrophic celestial event that plunged the world into darkness, primordial monsters once hidden in the Earth's crevices have awoken from their slumber. Drawn to the surface by the absence of light, they began hunting human populations to feast upon and feed their strength.",
        "As part of one of the last surviving members of an elite order of light-wielders, you are among the rare few who possess the mystical skills needed to fight the darkness and the horrible creatures it has unleashed.",
        "The monsters grow more powerful and numerous every passing hour, and with your entire squad wiped out in a sudden ambush attack, it will only be through mastering the arts of light that you have the hope of surviving until dawn - if it ever comes again.",
        "Good luck, and may the forgotten light guide your path through the Endless Night."
    };

    private final GamePanel PARENT;
    private final Font TITLE_FONT;
    private final JLabel TITLE_LABEL;
    private final JLabel lore_label;
    private BufferedImage background_image;
    private int index;

    public IntroScreen(GamePanel parent) {
        PARENT = parent;
        setLayout(null);
        setOpaque(false);
        TITLE_FONT = loadFont("/assets/gamefont.ttf", 64f);

        TITLE_LABEL = new JLabel("Endless Night", SwingConstants.CENTER);
        TITLE_LABEL.setFont(TITLE_FONT.deriveFont(72f));
        TITLE_LABEL.setForeground(new Color(137, 100, 255));
        add(TITLE_LABEL);

        lore_label = new JLabel();
        lore_label.setFont(TITLE_FONT.deriveFont(28f));
        lore_label.setForeground(Color.WHITE);
        lore_label.setHorizontalAlignment(SwingConstants.CENTER);
        add(lore_label);

        loadBackground();
        setupButtons();
        refreshContent();
    }

    private void loadBackground() {
        try {
            background_image = ImageIO.read(getClass().getResourceAsStream("/assets/images/backgrounds/introbg.jpg"));
        } catch (IOException e) {
            background_image = null;
        }
    }

    private void setupButtons() {
        int btn_w = 180;
        int btn_h = 60;
        int gap = 20;
        int total_w = btn_w * 3 + gap * 2;
        int start_x = (GamePanel.GAME_WIDTH - total_w) / 2;
        int btn_y = GamePanel.GAME_HEIGHT - btn_h - 60;

        addNavButton("Back", start_x, btn_y, btn_w, btn_h, () -> changeIndex(-1));
        addNavButton("Next", start_x + btn_w + gap, btn_y, btn_w, btn_h, () -> changeIndex(1));
        addNavButton("Skip", start_x + (btn_w + gap) * 2, btn_y, btn_w, btn_h, this::finishIntro);
    }

    private void addNavButton(String text, int x, int y, int w, int h, Runnable action) {
        GameButton button = new GameButton(text);
        button.setFont(TITLE_FONT.deriveFont(30f));
        button.setBounds(x, y, w, h);
        button.addActionListener(e -> action.run());
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setFont(TITLE_FONT.deriveFont(35f));
            }
            public void mouseExited(MouseEvent e) {
                button.setFont(TITLE_FONT.deriveFont(30f));
            }
        });
        add(button);
    }

    private void changeIndex(int delta) {
        int new_index = index + delta;
        if (new_index < 0 || new_index >= LORE.length) {
            finishIntro();
        } else {
            index = new_index;
            refreshContent();
        }
    }

    private void refreshContent() {
        TITLE_LABEL.setText("Endless Night");
        String family = TITLE_FONT.getFamily();
        String html =
            "<html><div style='" +
            "font-family:" + family + ";" +
            "font-size:28px;" +
            "text-align:center;" +
            "width:825px;'>" +
            LORE[index] +
            "</div></html>";
        lore_label.setText(html);
        positionComponents();
    }

    private void finishIntro() {
        PARENT.showScreen("play");
    }

    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH;
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT;

        FontMetrics tfm = TITLE_LABEL.getFontMetrics(TITLE_LABEL.getFont());
        int tw = tfm.stringWidth(TITLE_LABEL.getText());
        int th = tfm.getHeight();
        TITLE_LABEL.setBounds((w - tw) / 2, h / 6 - th / 2, tw, th);

        Dimension lore_dim = lore_label.getPreferredSize();
        lore_label.setBounds((w - lore_dim.width) / 2, h / 2 - lore_dim.height / 2, lore_dim.width, lore_dim.height);
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

    private static Font loadFont(String path, float size) {
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, IntroScreen.class.getResourceAsStream(path));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(f);
            return f.deriveFont(size);
        } catch (Exception e) {
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }
}
