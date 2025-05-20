
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
        TITLE_FONT = parent.getGameFont().deriveFont(Font.BOLD, 72f);

        TITLE_LABEL = new JLabel("Endless Night", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2.setFont(TITLE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                String text = getText();
                int tw = fm.stringWidth(text);
                // gradient from purple to gold
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(137, 100, 255),
                        tw, 0, new Color(140, 143, 187)
                );
                g2.setPaint(gp);
                g2.drawString(text, 0, fm.getAscent());
                g2.dispose();
            }
        };
        add(TITLE_LABEL);

        lore_label = new JLabel("", SwingConstants.CENTER);
        lore_label.setFont(parent.getGameFont().deriveFont(28f));
        lore_label.setForeground(Color.WHITE);
        add(lore_label);

        loadBackground();
        setupButtons();
        refreshContent();
    }

    private void loadBackground() {
        try {
            background_image = ImageIO.read(
                    getClass().getResourceAsStream("/assets/images/backgrounds/introbg.jpg")
            );
        } catch (IOException e) {
            background_image = null;
        }
    }

    private void setupButtons() {
        int btnW = 180, btnH = 60, gap = 20;
        int totalW = btnW * 3 + gap * 2;
        int startX = (GamePanel.GAME_WIDTH - totalW) / 2;
        int y = GamePanel.GAME_HEIGHT - btnH - 60;

        addNav("Back", startX, y, btnW, btnH, () -> changeIndex(-1));
        addNav("Next", startX + (btnW + gap), y, btnW, btnH, () -> changeIndex(1));
        addNav("Skip", startX + 2 * (btnW + gap), y, btnW, btnH, this::finishIntro);
    }

    private void addNav(String txt, int x, int y, int w, int h, Runnable action) {
        GameButton btn = new GameButton(txt);
        btn.setFont(TITLE_FONT.deriveFont(30f));
        btn.setBounds(x, y, w, h);
        btn.setForeground(Color.WHITE);
        btn.addActionListener(e -> action.run());
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setFont(TITLE_FONT.deriveFont(35f));
            }

            public void mouseExited(MouseEvent e) {
                btn.setFont(TITLE_FONT.deriveFont(30f));
            }
        });
        add(btn);
    }

    private void changeIndex(int delta) {
        int ni = index + delta;
        if (ni < 0 || ni >= LORE.length) {
            finishIntro();
        } else {
            index = ni;
            refreshContent();
        }
    }

    private void refreshContent() {
        String family = TITLE_FONT.getFamily();
        String html = "<html><div style='"
                + "font-family:" + family + ";"
                + "font-size:28px;"
                + "text-align:center;"
                + "width:825px;'>"
                + LORE[index]
                + "</div></html>";
        lore_label.setText(html);
        positionComponents();
    }

    private void finishIntro() {
        PARENT.showScreen("main_menu");
    }

    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH;
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT;

        // Title
        FontMetrics fm = TITLE_LABEL.getFontMetrics(TITLE_FONT);
        int tw = fm.stringWidth(TITLE_LABEL.getText());
        int th = fm.getHeight();
        TITLE_LABEL.setBounds((w - tw) / 2, h / 6 - th / 2, tw, th);

        // Lore
        Dimension ld = lore_label.getPreferredSize();
        lore_label.setBounds((w - ld.width) / 2, h / 2 - ld.height / 2, ld.width, ld.height);
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
}
