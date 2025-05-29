import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Player extends Entity {
    // Player's current level
    private int level;

    // Array to store the player's high scores
    private int high_scores[];

    // Map to store the player's skills and their levels
    private Map<Skill, Integer> skills;

    // Constant speed value for the player's movement
    private static final int SPEED = 5;

    // Constructor to initialize the player with position, size, health, speed, and
    // sprites
    public Player(int x, int y, int width, int height, int max_hp, int max_speed, BufferedImage[] sprites) {
        super(x, y, width, height, max_hp, max_speed, sprites);
        this.level = 1; // Initialize the player's level to 1
        this.high_scores = new int[5]; // Initialize the high scores array with a size of 5
        this.skills = new HashMap<>(); // Initialize the skills map
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
    }

    // Override the draw method to render the player on the screen
    @Override
    public void draw(Graphics g) {
        // Get the current mouse position
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();

        // Calculate the angle between the player and the mouse pointer
        double angle = Math.atan2(mousePosition.y - (y + HEIGHT / 2), mousePosition.x - (x + WIDTH / 2));

        Graphics2D g2d = (Graphics2D) g; // Cast Graphics to Graphics2D for advanced drawing
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
}
