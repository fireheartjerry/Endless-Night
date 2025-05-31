/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class is used to create the main screen buttons
*/

import java.awt.*;
import javax.swing.*;

// Custom JButton class with a unique appearance
public class GameButton extends JButton {

    // Background color of the button
    private Color bg_colour;

    // Constructor to initialize the button with text and default styles
    GameButton(String txt) {
        super(txt); // Set the button text
        bg_colour = new Color(140, 82, 255); // Default background color
        setFocusPainted(false); // Disable focus painting
        setBorderPainted(false); // Disable border painting
        setContentAreaFilled(false); // Disable default content area fill
        setForeground(Color.WHITE); // Set text color to white
    }

    // Method to update the background color
    public void setColor(Color color) {
        bg_colour = color;
    }

    // Override the paintComponent method to customize the button's appearance
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create(); // Create a copy of the Graphics object
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Enable anti-aliasing

        int w = getWidth(), h = getHeight(); // Get the button's width and height
        g2.setColor(bg_colour); // Set the background color
        g2.fillRect(0, 0, w, h); // Fill the button's background

        super.paintComponent(g2); // Call the parent class's paintComponent to draw text and other components

        g2.setStroke(new BasicStroke(4f)); // Set the stroke width for the border
        g2.setColor(Color.WHITE); // Set the border color to white
        g2.drawRect(0, 0, w - 1, h - 1); // Draw the border around the button

        g2.dispose(); // Dispose of the Graphics2D object to release resources
    }
}
