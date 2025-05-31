import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class Player extends Entity {
    // Player's skills
    private final Map<String, Skill> skills;
    private final LuminousPulse luminousPulse;
    private final LightLance lightLance;
    private final GamePanel parent;

    // Constant speed value for the player's movement
    private static final int SPEED = 5; // Constructor to initialize the player with position, size, health, speed, and
    // sprites

    public Player(int x, int y, int width, int height, int max_hp, int max_speed, BufferedImage[] sprites,
            GamePanel parent) {
        super(x, y, width, height, max_hp, max_speed, sprites);
        this.skills = new HashMap<>(); // Initialize the skills map

        // Initialize the Luminous Pulse skill
        this.luminousPulse = new LuminousPulse(this);
        this.skills.put("Luminous Pulse", luminousPulse);

        // Initialize the Light Lance skill
        this.lightLance = new LightLance(this);
        this.skills.put("Light Lance", lightLance);
        this.parent = parent; // Store the reference to the parent GamePanel
    }

    // Update method to handle skills and effects
    public void update(float dt, List<Enemy> enemies) {
        // Update the Luminous Pulse skill
        if (luminousPulse != null) {
            luminousPulse.update(dt, enemies);
        }

        // Update the Light Lance skill
        if (lightLance != null) {
            lightLance.update(dt, enemies);
        }
    }

    // Method to handle key press events for player movement
    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'd') { // Move right when 'd' is pressed
            setXVelocity(SPEED);
            move();
        }

        if (e.getKeyChar() == 'a') { // Move left when 'a' is pressed
            setXVelocity(SPEED * -1);
            move();
        }

        if (e.getKeyChar() == 'w') { // Move up when 'w' is pressed
            setYVelocity(SPEED * -1);
            move();
        }

        if (e.getKeyChar() == 's') { // Move down when 's' is pressed
            setYVelocity(SPEED);
            move();
        }
    }

    // Method to handle key release events to stop player movement
    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'd') { // Stop moving right when 'd' is released
            setXVelocity(0);
            move();
        }

        if (e.getKeyChar() == 'a') { // Stop moving left when 'a' is released
            setXVelocity(0);
            move();
        }

        if (e.getKeyChar() == 'w') { // Stop moving up when 'w' is released
            setYVelocity(0);
            move();
        }

        if (e.getKeyChar() == 's') { // Stop moving down when 's' is released
            setYVelocity(0);
            move();
        }
    }

    // Override the move method to include screen wrapping logic
    @Override
    public void move() {
        super.move(); // Call the parent class's move method

        // If the player moves off the left edge, wrap to the right edge
        if (x < WIDTH * -1) {
            x = GamePanel.GAME_WIDTH;
        }
        // If the player moves off the right edge, wrap to the left edge
        else if (x > GamePanel.GAME_WIDTH) {
            x = -WIDTH;
        }

        // If the player moves off the top edge, wrap to the bottom edge
        if (y < HEIGHT * -1) {
            y = GamePanel.GAME_HEIGHT;
        }
        // If the player moves off the bottom edge, wrap to the top edge
        else if (y > GamePanel.GAME_HEIGHT) {
            y = -HEIGHT;
        }
    } // Override the draw method to render the player on the screen

    @Override
    public void draw(Graphics g) {
        // Get the mouse position relative to the game panel
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePosition, parent);

        // Calculate the angle between the player and the mouse pointer
        double angle = Math.atan2(mousePosition.y - (y + HEIGHT / 2), mousePosition.x - (x + WIDTH / 2));

        Graphics2D g2d = (Graphics2D) g; // Cast Graphics to Graphics2D for advanced drawing

        // Draw the Luminous Pulse effect beneath the player
        if (luminousPulse != null) {
            luminousPulse.draw(g2d);
        } // Update Light Lance aim angle and draw it
        if (lightLance != null) {
            lightLance.setMousePosition(mousePosition);
            lightLance.draw(g2d);
        }

        g2d.setColor(Color.BLUE); // Set the player's color to blue

        // Save the current transformation of the graphics context
        AffineTransform oldTransform = g2d.getTransform();

        // Rotate the player to face the mouse pointer
        g2d.rotate(angle, x + WIDTH / 2, y + HEIGHT / 2);

        // Draw the player as a rectangle
        g2d.fillRect(x, y, WIDTH, HEIGHT);

        // Restore the original transformation
        g2d.setTransform(oldTransform);
    }

    // Method to get a skill by name
    public Skill getSkill(String skillName) {
        return skills.get(skillName);
    }
}
