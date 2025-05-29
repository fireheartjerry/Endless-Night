import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

// IntroScreen class represents the introductory screen of the game "Endless Night".
// It displays a title, a series of lore text, and navigation buttons to move through the lore or skip to the main menu.
public class IntroScreen extends JPanel {

    // Array of lore text to be displayed sequentially on the screen.
    private static final String[] LORE = {
            "Welcome to Endless Night, where the sun's descent has led to the beginning of humanity's greatest trial.",
            "Following a catastrophic celestial event that plunged the world into darkness, primordial monsters once hidden in the Earth's crevices have awoken from their slumber. Drawn to the surface by the absence of light, they began hunting human populations to feast upon and feed their strength.",
            "As part of one of the last surviving members of an elite order of light-wielders, you are among the rare few who possess the mystical skills needed to fight the darkness and the horrible creatures it has unleashed.",
            "The monsters grow more powerful and numerous every passing hour, and with your entire squad wiped out in a sudden ambush attack, it will only be through mastering the arts of light that you have the hope of surviving until dawn - if it ever comes again.",
            "Good luck, and may the forgotten light guide your path through the Endless Night."
    };

    // Reference to the parent GamePanel that manages the game's screens.
    private final GamePanel PARENT;

    // Font used for the title text.
    private final Font TITLE_FONT;

    // JLabel for displaying the game title.
    private final JLabel TITLE_LABEL;

    // JLabel for displaying the lore text.
    private final JLabel lore_label;

    // Background image for the intro screen.
    private BufferedImage background_image;

    // Index of the currently displayed lore text.
    private int index;

    // Constructor initializes the intro screen, sets up components, and loads
    // resources.
    public IntroScreen(GamePanel parent) {
        PARENT = parent;
        setLayout(null); // Use absolute positioning for components.
        setOpaque(false); // Make the panel transparent.
        TITLE_FONT = parent.getGameFont().deriveFont(Font.BOLD, 72f); // Derive a bold font for the title.

        // Initialize the title label with custom rendering for gradient text.
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
                // Gradient from purple to gold.
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(137, 100, 255),
                        tw, 0, new Color(140, 143, 187));
                g2.setPaint(gp);
                g2.drawString(text, 0, fm.getAscent());
                g2.dispose();
            }
        };
        add(TITLE_LABEL); // Add the title label to the panel.

        // Initialize the lore label for displaying the lore text.
        lore_label = new JLabel("", SwingConstants.CENTER);
        lore_label.setFont(parent.getGameFont().deriveFont(28f)); // Set font size for lore text.
        lore_label.setForeground(Color.WHITE); // Set text color to white.
        add(lore_label); // Add the lore label to the panel.

        loadBackground(); // Load the background image.
        setupButtons(); // Set up navigation buttons.
        refreshContent(); // Display the initial content.
    }

    // Loads the background image for the intro screen.
    private void loadBackground() {
        try {
            background_image = ImageIO.read(
                    getClass().getResourceAsStream("/assets/images/backgrounds/introbg.jpg"));
        } catch (IOException e) {
            background_image = null; // Set to null if the image cannot be loaded.
        }
    }

    // Sets up the navigation buttons (Back, Next, Skip) for the intro screen.
    private void setupButtons() {
        int btnW = 180, btnH = 60, gap = 20; // Button dimensions and gap between buttons.
        int totalW = btnW * 3 + gap * 2; // Total width of all buttons and gaps.
        int startX = (GamePanel.GAME_WIDTH - totalW) / 2; // Starting X position for buttons.
        int y = GamePanel.GAME_HEIGHT - btnH - 60; // Y position for buttons.

        // Add "Back" button to navigate to the previous lore text.
        addNav("Back", startX, y, btnW, btnH, () -> changeIndex(-1));
        // Add "Next" button to navigate to the next lore text.
        addNav("Next", startX + (btnW + gap), y, btnW, btnH, () -> changeIndex(1));
        // Add "Skip" button to skip the intro and go to the main menu.
        addNav("Skip", startX + 2 * (btnW + gap), y, btnW, btnH, this::finishIntro);
    }

    // Helper method to create and add a navigation button.
    private void addNav(String txt, int x, int y, int w, int h, Runnable action) {
        GameButton btn = new GameButton(txt); // Create a new button with the specified text.
        btn.setFont(TITLE_FONT.deriveFont(30f)); // Set the font size for the button text.
        btn.setBounds(x, y, w, h); // Set the button's position and size.
        btn.setForeground(Color.WHITE); // Set the button text color to white.
        btn.addActionListener(e -> action.run()); // Add an action listener to execute the specified action.
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setFont(TITLE_FONT.deriveFont(35f)); // Increase font size on hover.
            }

            public void mouseExited(MouseEvent e) {
                btn.setFont(TITLE_FONT.deriveFont(30f)); // Reset font size when hover ends.
            }
        });
        add(btn); // Add the button to the panel.
    }

    // Changes the index of the currently displayed lore text and refreshes the
    // content.
    private void changeIndex(int delta) {
        int ni = index + delta; // Calculate the new index.
        if (ni < 0 || ni >= LORE.length) { // If the index is out of bounds, finish the intro.
            finishIntro();
        } else {
            index = ni; // Update the index.
            refreshContent(); // Refresh the displayed content.
        }
    }

    // Refreshes the content of the lore label based on the current index.
    private void refreshContent() {
        String family = TITLE_FONT.getFamily(); // Get the font family for the title font.
        String html = "<html><div style='"
                + "font-family:" + family + ";"
                + "font-size:28px;"
                + "text-align:center;"
                + "width:825px;'>"
                + LORE[index] // Display the current lore text.
                + "</div></html>";
        lore_label.setText(html); // Set the lore label's text.
        positionComponents(); // Reposition components on the screen.
    }

    // Finishes the intro and switches to the main menu screen.
    private void finishIntro() {
        PARENT.showScreen("main_menu"); // Show the main menu screen.
    }

    // Positions the components (title and lore labels) on the screen.
    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH; // Get the panel width.
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT; // Get the panel height.

        // Position the title label.
        FontMetrics fm = TITLE_LABEL.getFontMetrics(TITLE_FONT);
        int tw = fm.stringWidth(TITLE_LABEL.getText());
        int th = fm.getHeight();
        TITLE_LABEL.setBounds((w - tw) / 2, h / 6 - th / 2, tw, th);

        // Position the lore label.
        Dimension ld = lore_label.getPreferredSize();
        lore_label.setBounds((w - ld.width) / 2, h / 2 - ld.height / 2, ld.width, ld.height);
    }

    // Paints the background image and applies transparency.
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        if (background_image != null) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.5f)); // Apply transparency to the background image.
            g2.drawImage(background_image, 0, 0, getWidth(), getHeight(), null); // Draw the background image.
        }
        g2.dispose();
        super.paintComponent(g); // Call the superclass's paintComponent method.
    }

    // Invalidates the panel and repositions components when the layout changes.
    @Override
    public void invalidate() {
        super.invalidate();
        positionComponents(); // Reposition components.
    }
}
