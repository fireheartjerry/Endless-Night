/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class helps set up a game entity with position, size, velocity, and possibly sprite animations
*/

import java.awt.*;
import java.awt.image.*;

public class GameObject extends Rectangle {
    // The width of the game object.
    protected final int WIDTH;
    // The height of the game object.
    protected final int HEIGHT;
    // Array of sprites for animation or rendering.
    private BufferedImage[] sprites;
    // The horizontal velocity of the game object.
    protected float x_velocity;
    // The vertical velocity of the game object.
    protected float y_velocity;
    // The current frame index for sprite animation.
    private int current_frame;

    // Constructor to initialize the game object with position, size, and sprites.
    public GameObject(int x, int y, int width, int height, BufferedImage[] sprites) {
        super(x, y, width, height); // Initialize the Rectangle superclass.
        this.WIDTH = width;
        this.HEIGHT = height;
        this.sprites = sprites;
    }

    // Sets the horizontal velocity of the game object.
    public void setXVelocity(int x_velocity) {
        this.x_velocity = x_velocity;
    }

    // Sets the vertical velocity of the game object.
    public void setYVelocity(int y_velocity) {
        this.y_velocity = y_velocity;
    }

    // Gets the horizontal velocity of the game object.
    public float getXVelocity() {
        return x_velocity;
    }

    // Gets the vertical velocity of the game object.
    public float getYVelocity() {
        return y_velocity;
    }

    // Sets the position of the game object.
    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Updates the position of the game object based on its velocity.
    public void move() {
        x += x_velocity;
        y += y_velocity;
    }

    // Draws the game object as a blue rectangle on the screen.
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, WIDTH, HEIGHT);
    }
}
