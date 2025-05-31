/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class manages the player character and its properties
*/

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class Player extends Entity {
    // Player's skills
    private final Map<String, Skill> skills;
    private final LuminousPulse luminousPulse;
    private final LightLance lightLance;
    private final PhotonOrbs photonOrbs;
    private final GamePanel parent;
      // Animation related variables
    private BufferedImage[] runSprites;
    private BufferedImage idleSprite;
    private int currentSpriteIndex = 0;
    private long lastAnimationUpdate = 0;
    private static final long ANIMATION_DELAY = 150; // milliseconds between frames
    private boolean isMoving = false;
    
    // Direction tracking
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.RIGHT;

    // Constant speed value for the player's movement
    private static final int SPEED = 5; // Constructor to initialize the player with position, size, health, speed, and
    // sprites
    public Player(int x, int y, int width, int height, int max_hp, int max_speed, BufferedImage[] sprites,
            GamePanel parent) {
        super(x, y, width, height, max_hp, max_speed, sprites);
        this.skills = new HashMap<>(); // Initialize the skills map
        this.parent = parent; // Store the reference to the parent GamePanel

        // Initialize the Luminous Pulse skill
        this.luminousPulse = new LuminousPulse(this);
        this.skills.put("Luminous Pulse", luminousPulse);

        // Initialize the Light Lance skill
        this.lightLance = new LightLance(this);
        this.skills.put("Light Lance", lightLance);
        
        // Initialize the Photon Orbs skill
        this.photonOrbs = new PhotonOrbs(this);
        this.skills.put("Photon Orbs", photonOrbs);
        
        // Load sprite images
        loadSprites();
    }    /**
     * Loads player sprite images from the assets folder
     */
    private void loadSprites() {
        try {
            // Load the running animation sprites (3 frames)
            runSprites = new BufferedImage[3];
            runSprites[0] = ImageIO.read(getClass().getResourceAsStream("/assets/images/sprites/MCrun1.png"));
            runSprites[1] = ImageIO.read(getClass().getResourceAsStream("/assets/images/sprites/MCrun2.png"));
            runSprites[2] = ImageIO.read(getClass().getResourceAsStream("/assets/images/sprites/MCrun3.png"));
            
            // We'll use MCrun1.png as idle sprite for now
            idleSprite = ImageIO.read(getClass().getResourceAsStream("/assets/images/sprites/MCrun1.png"));
            
            // Resize sprites to match player dimensions
            for (int i = 0; i < runSprites.length; i++) {
                runSprites[i] = resizeImage(runSprites[i], WIDTH, HEIGHT);
            }
            idleSprite = resizeImage(idleSprite, WIDTH, HEIGHT);
            
            System.out.println("Successfully loaded player sprites");
        } catch (IOException e) {
            System.err.println("Error loading player sprites: " + e.getMessage());
            // Create placeholder sprites if loading fails
            createPlaceholderSprites();
        }
    }
    
    /**
     * Resizes an image to the specified width and height
     * 
     * @param originalImage The image to resize
     * @param targetWidth The target width
     * @param targetHeight The target height
     * @return The resized image
     */
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
      /**
     * Creates visually distinct placeholder sprites if image loading fails
     */
    private void createPlaceholderSprites() {
        int width = 50;
        int height = 50;
        
        // Create placeholder sprites with visual indicators for animation frames
        runSprites = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            runSprites[i] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = runSprites[i].createGraphics();
            
            // Enable anti-aliasing for smoother shapes
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw body
            g.setColor(new Color(100, 150, 255)); // Blue color
            g.fillOval(5, 5, width - 10, height - 10);
            
            // Draw indicator for which animation frame this is (1, 2, or 3 dots)
            g.setColor(Color.WHITE);
            for (int j = 0; j <= i; j++) {
                g.fillOval(20 + j * 5, 15, 4, 4);
            }
            
            // Draw direction indicator
            g.setColor(Color.RED);
            g.fillOval(width/2, 10, 8, 8);
            
            g.dispose();
        }
        
        // Create a visually distinct idle sprite
        // Create a visually distinct idle sprite
        idleSprite = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = idleSprite.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(100, 200, 150)); // Different color for idle
        g.fillOval(5, 5, width - 10, height - 10);
        g.setColor(Color.WHITE);
        g.drawString("I", width/2 - 2, height/2 + 5);
        g.dispose();
        
        // Note: We cannot update WIDTH and HEIGHT as they are final
        // We'll use the sprite dimensions directly when needed
    }
    public void update(float dt, List<Enemy> enemies) {
        // Update the Luminous Pulse skill
        if (luminousPulse != null) {
            luminousPulse.update(dt, enemies);
        }

        // Update the Light Lance skill
        if (lightLance != null) {
            lightLance.update(dt, enemies);
        }
        
        // Update the Photon Orbs skill
        if (photonOrbs != null) {
            photonOrbs.update(dt, enemies);
        }
        
        // Check if player is moving for animation purposes
        isMoving = Math.abs(getXVelocity()) > 0 || Math.abs(getYVelocity()) > 0;
        
        // Update animation frame if player is moving
        updateAnimation();
    }
    
    /**
     * Updates the animation frame based on player movement
     */
    private void updateAnimation() {
        long currentTime = System.currentTimeMillis();
        
        // Only update animation if player is moving and enough time has passed
        if (isMoving && currentTime - lastAnimationUpdate > ANIMATION_DELAY) {
            currentSpriteIndex = (currentSpriteIndex + 1) % runSprites.length;
            lastAnimationUpdate = currentTime;
        }
    }    // Method to handle key press events for player movement
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'd': // Move right when 'd' is pressed
                setXVelocity(SPEED);
                currentDirection = Direction.RIGHT;
                isMoving = true;
                move();
                break;
                
            case 'a': // Move left when 'a' is pressed
                setXVelocity(SPEED * -1);
                currentDirection = Direction.LEFT;
                isMoving = true;
                move();
                break;
                
            case 'w': // Move up when 'w' is pressed
                setYVelocity(SPEED * -1);
                currentDirection = Direction.UP;
                isMoving = true;
                move();
                break;
                
            case 's': // Move down when 's' is pressed
                setYVelocity(SPEED);
                currentDirection = Direction.DOWN;
                isMoving = true;
                move();
                break;
        }
    }// Method to handle key release events to stop player movement
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyChar()) {
            case 'd': // Stop moving right when 'd' is released
                setXVelocity(0);
                break;
                
            case 'a': // Stop moving left when 'a' is released
                setXVelocity(0);
                break;
                
            case 'w': // Stop moving up when 'w' is released
                setYVelocity(0);
                break;
                
            case 's': // Stop moving down when 's' is released
                setYVelocity(0);
                break;
        }
        
        // Check if the player is still moving after key release
        isMoving = (getXVelocity() != 0 || getYVelocity() != 0);
        move();
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
        
        Graphics2D g2d = (Graphics2D) g; // Cast Graphics to Graphics2D for advanced drawing        // Draw the Luminous Pulse effect beneath the player
        if (luminousPulse != null) {
            luminousPulse.draw(g2d);
        } 
        
        // Update Light Lance aim angle and draw it
        if (lightLance != null) {
            lightLance.setMousePosition(mousePosition);
            lightLance.draw(g2d);
        }
        
        // Draw Photon Orbs
        if (photonOrbs != null) {
            photonOrbs.draw(g2d);
        }
        
        // Get current sprite based on state
        BufferedImage currentSprite;
        if (isMoving) {
            currentSprite = runSprites[currentSpriteIndex];
        } else {
            currentSprite = idleSprite;
        }
        
        // Apply transformations based on direction
        BufferedImage directedSprite = getDirectionalSprite(currentSprite);
        
        // Draw sprite at the player's position with proper orientation
        g2d.drawImage(directedSprite, x, y, WIDTH, HEIGHT, null);
    }
    /**
     * Returns a directionally appropriate sprite based on the player's movement direction
     * 
     * @param sprite The original sprite to transform
     * @return The transformed sprite based on current direction
     */
    private BufferedImage getDirectionalSprite(BufferedImage sprite) {
        // Check horizontal direction priority
        if (getXVelocity() > 0) {
            // Moving right (or diagonally right)
            return flipImageHorizontally(sprite);
        } else if (getXVelocity() < 0) {
            // Moving left (or diagonally left)
            return sprite;
        } else {
            // No horizontal movement, use the last horizontal direction
            if (currentDirection == Direction.LEFT) {
                return sprite;
            } else {
                return flipImageHorizontally(sprite);
            }
        }
    }
    /**
     * Helper method to flip an image horizontally (for left/right facing sprites)
     * 
     * @param image The image to flip
     * @return The flipped image
     */
    private BufferedImage flipImageHorizontally(BufferedImage image) {
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        return op.filter(image, null);
    }

    // Method to get a skill by name
    public Skill getSkill(String skillName) {
        return skills.get(skillName);
    }
}
