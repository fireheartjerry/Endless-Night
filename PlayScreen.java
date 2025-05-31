/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class represents teh main gameplay screen
*/

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class PlayScreen extends JPanel {

    // Frames per second for the game rendering.
    private static final int FPS = 60;

    // Padding for the HUD (Heads-Up Display).
    private static final int HUD_PADDING = 20;

    // Dimensions for small buttons.
    private static final int BTN_W_SMALL = 100, BTN_H_SMALL = 40;

    // Dimensions for big buttons.
    private static final int BTN_W_BIG = 200, BTN_H_BIG = 60;

    // Reference to the parent GamePanel, which contains shared game data and logic.
    private final GamePanel PARENT;

    // Font used for rendering UI elements.
    private final Font UI_FONT;

    // Background image for the play screen.
    private BufferedImage backgroundImage;

    // Constructor for the PlayScreen class.
    // Initializes the parent reference, layout, and font.
    public PlayScreen(GamePanel parent) {
        PARENT = parent; // Assign the parent GamePanel.
        setLayout(null); // Disable layout management for custom positioning.
        setOpaque(false); // Make the panel transparent.
        UI_FONT = PARENT.getGameFont(); // Retrieve the game font from the parent.

        setFocusable(true); // Allow the panel to receive focus for input handling.
    }

    // Custom rendering logic for the play screen.
    @Override
    protected void paintComponent(Graphics g) {
        // Create a Graphics2D object for advanced rendering.
        Graphics2D g2 = (Graphics2D) g.create();

        // Fill the background with a dark gray color.
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getWidth(), getHeight());

        // Draw the player character.
        PARENT.player.draw(g2);

        // Draw all enemy entities.
        for (Enemy enemy : PARENT.enemies) {
            enemy.draw(g2);
        }

        // Draw the HUD (Heads-Up Display) with the current screen width.
        PARENT.hud.draw(g2, getWidth());
    }

    // Override the invalidate method to handle layout invalidation.
    @Override
    public void invalidate() {
        super.invalidate(); // Call the superclass implementation.
    }
}
