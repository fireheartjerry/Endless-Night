import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

// The Entity class represents a game entity with properties such as position, velocity, health, and more.
// It extends the GameObject class and adds additional functionality specific to entities.
public class Entity extends GameObject {
    // The maximum health points of the entity.
    public final int MAX_HP;
    // The maximum speed of the entity.
    public final int MAX_SPEED;

    // The x and y coordinates of the center of the entity.
    private float center_x, center_y;
    // The radius of the entity, used for circular bounds.
    private float radius;

    // The current health points of the entity.
    private int hp;
    // The name of the entity.
    private String name;
    // A flag indicating whether the entity is dead.
    private boolean dead;

    // The current position of the entity.
    float cx, cy;
    // The current velocity of the entity.
    float vx, vy;

    // Constructor to initialize the entity with its position, size, maximum health,
    // maximum speed, and sprites.
    public Entity(int x, int y, int width, int height, int max_hp, int max_speed, BufferedImage[] sprites) {
        super(x, y, width, height, sprites); // Call the superclass constructor.
        this.MAX_HP = max_hp; // Set the maximum health.
        this.MAX_SPEED = max_speed; // Set the maximum speed.
        this.center_x = x + width * 0.5f; // Calculate the center x-coordinate.
        this.center_y = y + height * 0.5f; // Calculate the center y-coordinate.
        this.radius = Math.max(width, height) * 0.5f; // Calculate the radius based on the larger dimension.
        this.hp = max_hp; // Initialize the health to the maximum health.
    }

    // Updates the entity's position and velocity over time.
    public void update(float dt) {
        cx += vx * dt; // Update the x-coordinate based on velocity and time.
        cy += vy * dt; // Update the y-coordinate based on velocity and time.
        vx *= 0.90f; // Apply friction to the x-velocity.
        vy *= 0.90f; // Apply friction to the y-velocity.
        setPos(cx, cy); // Update the entity's position.
    }

    // Sets the position of the entity based on the given x and y values.
    public void setPos(double x_val, double y_val) {
        center_x = (float) x_val; // Update the center x-coordinate.
        center_y = (float) y_val; // Update the center y-coordinate.
        x = (int) (center_x - width * 0.5f); // Update the top-left x-coordinate.
        y = (int) (center_y - height * 0.5f); // Update the top-left y-coordinate.
    }

    // Sets the velocity of the entity, clamping it to the maximum speed.
    public void setVelocity(float vx, float vy) {
        x_velocity = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, vx)); // Clamp the x-velocity.
        y_velocity = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, vy)); // Clamp the y-velocity.
    }

    // Returns the maximum health points of the entity.
    public int getMaxHp() {
        return MAX_HP;
    }

    // Returns the circular bounds of the entity as a Rectangle2D.Float object.
    public Rectangle2D.Float getCircleBounds() {
        return new Rectangle2D.Float(center_x - radius, center_y - radius, radius * 2f, radius * 2f);
    }

    // Returns the radius of the entity.
    public float getRadius() {
        return radius;
    }

    // Reduces the entity's health by the specified damage amount and marks it as
    // dead if health reaches zero.
    public void takeDamage(int damage) {
        hp -= damage; // Subtract the damage from the current health.
        if (hp <= 0) { // Check if the health is zero or below.
            hp = 0; // Set health to zero.
            dead = true; // Mark the entity as dead.
        }
    }

    // Returns whether the entity is dead.
    public boolean isDead() {
        return dead;
    }

    // Returns the current health points of the entity.
    public int getHp() {
        return hp;
    }

    // Sets the health points of the entity, clamping it between 0 and the maximum
    // health.
    public void setHp(int hp_val) {
        hp = Math.min(MAX_HP, Math.max(0, hp_val)); // Clamp the health value.
        dead = hp == 0; // Update the dead flag based on the health value.
    }

    // Returns the name of the entity.
    public String getName() {
        return name;
    }

    // Sets the name of the entity.
    public void setName(String name) {
        this.name = name;
    }
}
