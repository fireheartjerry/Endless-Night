/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class represents the game main menu screen
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainMenu extends JPanel {

    // Constants for button dimensions and highscore button dimensions.
    private static final int HS_W = 200, HS_H = 50;
    private static final int BTN_H_SMALL = 100, BTN_W_SMALL = 300;
    private static final int BTN_H_BIG = 140, BTN_W_BIG = 400;

    // Reference to the parent GamePanel.
    private final GamePanel PARENT;

    // Font used for UI elements.
    private final Font UI_FONT;

    // Title label for the main menu.
    private final JLabel TITLE_LABEL;

    // Buttons for different menu options.
    private final GameButton highscores_button;
    private final GameButton how_button;
    private final GameButton credits_button;
    private final GameButton start_button;

    // Background image for the main menu.
    private BufferedImage background_image;

    // Constructor initializes the main menu and its components.
    public MainMenu(GamePanel parent) {
        PARENT = parent; // Set the parent GamePanel reference.
        setLayout(null); // Use absolute positioning for components.
        setOpaque(false); // Make the panel transparent.
        UI_FONT = parent.getGameFont(); // Retrieve the font from the parent.

        // Initialize and configure the title label.
        TITLE_LABEL = new JLabel("Endless Night");
        TITLE_LABEL.setFont(UI_FONT.deriveFont(Font.BOLD, 90f)); // Set font size and style.
        TITLE_LABEL.setForeground(Color.WHITE); // Set text color to white.
        add(TITLE_LABEL); // Add the title label to the panel.

        // Initialize and configure the highscores button.
        highscores_button = makeMenuButton("Highscores", 20f, e -> PARENT.showScreen("scores"));
        highscores_button.setColor(new Color(135, 123, 182)); // Set custom color for the button.
        add(highscores_button); // Add the button to the panel.

        // Initialize and configure the "How to Play" button.
        how_button = makeMenuButton("HOW 2 PLAY", 35f, e -> PARENT.game_state = GameState.HOW_TO_PLAY);
        add(how_button); // Add the button to the panel.

        // Initialize and configure the credits button.
        credits_button = makeMenuButton("CREDITS", 40f, e -> PARENT.game_state = GameState.CREDITS);
        add(credits_button); // Add the button to the panel.

        // Initialize and configure the start button.
        start_button = makeMenuButton("START", 64f, e -> {
            PARENT.game_state = GameState.PLAYING; // Change game state to "PLAYING" when clicked.
        });
        start_button.setColor(new Color(76, 72, 144)); // Set custom color for the button.
        add(start_button); // Add the button to the panel.

        // Load the background image for the main menu.
        loadBackground();
    }

    // Loads the background image from the resources folder.
    private void loadBackground() {
        try {
            background_image = ImageIO
                    .read(getClass().getResourceAsStream("/assets/images/backgrounds/mainmenubg.jpg"));
        } catch (IOException e) {
            background_image = null; // Set to null if the image cannot be loaded.
        }
    }

    // Creates a menu button with the specified text, font size, and action
    // listener.
    private GameButton makeMenuButton(String text, float size, ActionListener action) {
        GameButton b = new GameButton(text); // Create a new GameButton with the specified text.
        b.setFont(UI_FONT.deriveFont(Font.BOLD, size)); // Set the font size and style.
        b.addActionListener(action); // Add the action listener to the button.
        b.addMouseListener(new MouseAdapter() {
            // Increase font size when the mouse enters the button.
            public void mouseEntered(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(Font.BOLD, size * 1.15f));
            }

            // Reset font size when the mouse exits the button.
            public void mouseExited(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(Font.BOLD, size));
            }
        });
        return b; // Return the configured button.
    }

    // Paints the background image and applies transparency.
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create(); // Create a copy of the Graphics object.
        if (background_image != null) {
            g2.setComposite(AlphaComposite.SrcOver.derive(0.5f)); // Set transparency to 50%.
            g2.drawImage(background_image, 0, 0, getWidth(), getHeight(), null); // Draw the background image.
        }
        g2.dispose(); // Dispose of the Graphics2D object.
        super.paintComponent(g); // Call the superclass method to paint other components.
    }

    // Repositions components when the panel is invalidated.
    @Override
    public void invalidate() {
        super.invalidate(); // Call the superclass method.
        positionComponents(); // Reposition the components.
    }

    // Positions the components on the panel based on its dimensions.
    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH; // Get the panel width or default to game width.
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT; // Get the panel height or default to game
                                                                       // height.

        // Position the title label at the top-left corner.
        TITLE_LABEL.setBounds(20, 10, TITLE_LABEL.getPreferredSize().width, TITLE_LABEL.getPreferredSize().height);

        // Position the highscores button at the top-right corner.
        highscores_button.setBounds(w - HS_W - 60, 40, HS_W, HS_H);

        // Position the "How to Play" and credits buttons at the bottom.
        int y_small = h - BTN_H_SMALL - 60; // Calculate the vertical position for small buttons.
        how_button.setBounds(60, y_small, BTN_W_SMALL, BTN_H_SMALL); // Position the "How to Play" button.
        credits_button.setBounds(w - BTN_W_SMALL - 60, y_small, BTN_W_SMALL, BTN_H_SMALL); // Position the credits
                                                                                           // button.

        // Position the start button at the center-bottom of the panel.
        int x_start = (w - BTN_W_BIG) / 2; // Calculate the horizontal position for the start button.
        int y_start = y_small - (BTN_H_BIG - BTN_H_SMALL) / 2 - 20; // Adjust the vertical position for the start
                                                                    // button.
        start_button.setBounds(x_start, y_start, BTN_W_BIG, BTN_H_BIG); // Set the bounds for the start button.
    }
}
