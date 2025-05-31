import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The Enemy class represents an enemy entity in the game.
 * It extends the Entity class and includes behavior for movement, knockback,
 * and rendering.
 */
public final class Enemy extends Entity {
    // Constants for enemy behavior and physics
    private static final float MAX_SPEED = 2.4f; // Maximum movement speed
    private static final float ARRIVAL_RADIUS = 110f; // Radius for slowing down near the player
    private static final float RESPONSIVENESS = 0.18f; // Steering responsiveness
    private static final float WANDER_RADIUS = 0.45f; // Radius for wandering behavior
    private static final float WANDER_JITTER = 0.25f; // Jitter for wandering angle
    private static final float KNOCKBACK_FRICTION = 0.9f; // Friction during knockback
    private static final float MIN_SPEED = 0.05f; // Minimum speed threshold
    private static final float SEPARATION_WEIGHT = 1.2f; // Weight for separation behavior
    private static final float SEPARATION_RADIUS = 100f; // Radius for separation behavior
    private static final float RECOVERY_RATE = 0.05f; // Recovery rate after knockback

    // Collection of damage numbers for this enemy
    private final CopyOnWriteArrayList<DamageNumber> damageNumbers = new CopyOnWriteArrayList<>();

    private int damage = 2; // Damage dealt by the enemy
    private boolean knocked_back; // Whether the enemy is in a knockback state
    private float wander_angle = (float) (Math.random() * Math.PI * 2.0); // Angle for wandering behavior
    private GamePanel gamePanel; // Reference to the game panel

    /**
     * Constructor for the Enemy class.
     * Initializes the enemy with position, size, health, speed, and sprite data.
     */
    public Enemy(int x, int y, int w, int h, int max_hp, int max_speed, BufferedImage[] sprites, GamePanel gamePanel) {
        super(x, y, w, h, max_hp, max_speed, sprites);
        this.gamePanel = gamePanel; // Store the reference to the game panel
    }

    /**
     * Updates the enemy's state, including movement and knockback handling.
     * Handles behaviors such as steering, wandering, and separation.
     */
    public void update(float dt, Player player) {

        if (knocked_back) {
            // Handle knockback behavior if the enemy is in a knockback state
            handleKnockback(dt);
            return;
        }

        // Initialize game panel reference if not already set
        if (gamePanel == null) {
            for (Frame frame : Frame.getFrames()) {
                if (frame.isVisible() && frame instanceof GameFrame) {
                    Component comp = ((GameFrame) frame).getContentPane();
                    if (comp instanceof GamePanel) {
                        gamePanel = (GamePanel) comp;
                        break;
                    }
                }
            }
        }

        // Calculate direction to the player
        float to_x = (float) (player.getCenterX() - getCenterX());
        float to_y = (float) (player.getCenterY() - getCenterY());
        float dist = (float) Math.hypot(to_x, to_y);

        // Normalize direction vector
        if (dist > 1e-3f) {
            to_x /= dist;
            to_y /= dist;
        }

        // Calculate desired speed based on distance to the player
        float desired_speed;
        if (dist < ARRIVAL_RADIUS) {
            // Smooth slowing down when near the player
            float t = dist / ARRIVAL_RADIUS;
            desired_speed = MAX_SPEED * t * t * (3 - 2 * t);
        } else {
            desired_speed = MAX_SPEED;
        }

        // Calculate desired velocity
        float desired_x = to_x * desired_speed;
        float desired_y = to_y * desired_speed;

        // Add wandering behavior
        wander_angle += (Math.random() - 0.5f) * WANDER_JITTER * dt * 60f;
        wander_angle *= 0.98f; // Dampen wandering angle
        float wander_strength = Math.min(1.0f, dist / 200.0f);
        desired_x += Math.cos(wander_angle) * WANDER_RADIUS * wander_strength;
        desired_y += Math.sin(wander_angle) * WANDER_RADIUS * wander_strength;

        // Separation behavior to avoid crowding with other enemies
        float sep_x = 0;
        float sep_y = 0;
        int neighbors = 0;
        if (gamePanel != null) {
            List<Enemy> enemies = gamePanel.enemies;
            for (Enemy other : enemies) {
                if (other == this)
                    continue;
                float dx = (float) (getCenterX() - other.getCenterX());
                float dy = (float) (getCenterY() - other.getCenterY());
                float d = (float) Math.hypot(dx, dy);
                if (d > 0 && d < SEPARATION_RADIUS) {
                    float factor = SEPARATION_WEIGHT * (SEPARATION_RADIUS / (d * d));
                    sep_x += (dx / d) * factor;
                    sep_y += (dy / d) * factor;
                    neighbors++;
                }
            }
            if (neighbors > 0) {
                // Normalize separation vector and limit its magnitude
                float sep_mag = (float) Math.hypot(sep_x, sep_y);
                if (sep_mag > MAX_SPEED) {
                    sep_x = (sep_x / sep_mag) * MAX_SPEED;
                    sep_y = (sep_y / sep_mag) * MAX_SPEED;
                }
                // Blend separation behavior with desired velocity
                desired_x = desired_x * 0.8f + sep_x * 0.2f;
                desired_y = desired_y * 0.8f + sep_y * 0.2f;
            }
        }

        // Steering behavior to adjust velocity towards the desired velocity
        float dot = to_x * x_velocity + to_y * y_velocity;
        float dynamic_responsiveness = RESPONSIVENESS * (1.0f + (1.0f - Math.max(0, dot)) * 0.5f);
        float steer_x = desired_x - x_velocity;
        float steer_y = desired_y - y_velocity;
        x_velocity += steer_x * dynamic_responsiveness * dt * 60f;
        y_velocity += steer_y * dynamic_responsiveness * dt * 60f;

        // Limit speed to maximum
        float speed = (float) Math.hypot(x_velocity, y_velocity);
        if (speed > MAX_SPEED) {
            float reduction = 0.8f + 0.2f * (MAX_SPEED / speed);
            x_velocity *= reduction;
            y_velocity *= reduction;
        }

        // Update position based on velocity
        x += x_velocity * dt * 60f;
        y += y_velocity * dt * 60f;

        // Wrap around screen edges
        wrap();
    }

    /**
     * Moves the enemy by updating its state.
     * This method is a wrapper for the update method with a fixed delta time.
     */
    public void move(Player player) {
        if (gamePanel.game_state != GameState.PLAYING) {
            return;
        }
        update(1.0f / 60.0f, player);
    }

    /**
     * Handles knockback behavior.
     * Applies friction and recovery forces during knockback.
     */
    private void handleKnockback(float dt) {
        // Apply friction to reduce velocity over time
        float decay = (float) Math.pow(KNOCKBACK_FRICTION, dt * 60);
        x_velocity *= decay;
        y_velocity *= decay;

        // Recovery behavior during knockback
        if (gamePanel != null && Math.hypot(x_velocity, y_velocity) > MIN_SPEED) {
            Player player = gamePanel.player;
            if (player != null) {
                float tx = (float) (player.getCenterX() - getCenterX());
                float ty = (float) (player.getCenterY() - getCenterY());
                float dist = (float) Math.hypot(tx, ty);
                if (dist > 0) {
                    tx /= dist;
                    ty /= dist;
                    x_velocity += tx * RECOVERY_RATE * dt * 60f;
                    y_velocity += ty * RECOVERY_RATE * dt * 60f;
                }
            }
        }

        // Update position based on velocity
        x += x_velocity * dt * 60f;
        y += y_velocity * dt * 60f;

        // End knockback state if velocity is below threshold
        if (Math.hypot(x_velocity, y_velocity) < MIN_SPEED) {
            x_velocity = y_velocity = 0f;
            knocked_back = false;
        }

        // Wrap around screen edges
        wrap();
    }

    /**
     * Overloaded method to handle knockback with default delta time.
     */
    private void handleKnockback() {
        handleKnockback(1.0f / 60.0f);
    }

    /**
     * Wraps the enemy's position around the screen edges.
     * Ensures the enemy reappears on the opposite side of the screen if it moves
     * out of bounds.
     */
    private void wrap() {
        if (x < -WIDTH)
            x = GamePanel.GAME_WIDTH;
        else if (x > GamePanel.GAME_WIDTH)
            x = -WIDTH;
        if (y < -HEIGHT)
            y = GamePanel.GAME_HEIGHT;
        else if (y > GamePanel.GAME_HEIGHT)
            y = -HEIGHT;
    }

    /**
     * Applies knockback to the enemy.
     * Sets the velocity and marks the enemy as being in a knockback state.
     */
    public void applyKnockback(float vx, float vy) {
        x_velocity = vx;
        y_velocity = vy;
        knocked_back = true;
    }

    /**
     * Checks if the enemy is in a knockback state.
     * Returns true if the enemy is currently being knocked back.
     */
    public boolean isKnockedBack() {
        return knocked_back;
    }

    /**
     * Alias for isKnockedBack().
     * Provides an alternative method name for checking knockback state.
     */
    public boolean isInKnockbackState() {
        return knocked_back;
    }

    /**
     * Gets the damage dealt by the enemy.
     * Returns the amount of damage this enemy can inflict.
     */
    public int getDamage() {
        return damage;
    }

    /**
     * Overrides the takeDamage method to add damage number visualizations
     * 
     * @param damage The amount of damage to apply
     */
    @Override
    public void takeDamage(int damage) {
        // Create and add a damage number at enemy's position before applying damage
        // This ensures the damage number is created even if the enemy dies
        DamageNumber damageNum = new DamageNumber(
                (int) getCenterX(),
                (int) getCenterY() - 20,
                damage);
        damageNumbers.add(damageNum);

        // Apply damage after creating the damage number
        super.takeDamage(damage);
    }

    /**
     * Updates damage number effects
     * 
     * @param dt Delta time for frame-rate independence
     */
    public void updateDamageNumbers(float dt) {
        for (DamageNumber damageNumber : new java.util.ArrayList<>(damageNumbers)) {
            damageNumber.update(dt);
            if (damageNumber.isDead()) {
                damageNumbers.remove(damageNumber);
            }
        }
    }

    /**
     * Draws the enemy on the screen.
     * Renders the enemy as a red rectangle with a white outline.
     * If the enemy is in a knockback state, a yellow line indicates the knockback
     * direction.
     */
    @Override
    public void draw(Graphics g) {
        // Draw the enemy as a red rectangle
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);

        // Draw the enemy's outline
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);

        // Draw knockback indicator if in knockback state
        if (knocked_back) {
            g.setColor(Color.YELLOW);
            g.drawLine(
                    (int) getCenterX(),
                    (int) getCenterY(),
                    (int) (getCenterX() + x_velocity * 5),
                    (int) (getCenterY() + y_velocity * 5));
        }
        // Draw damage numbers with proper Graphics2D object
        if (!damageNumbers.isEmpty()) {
            Graphics2D g2d = (Graphics2D) g.create(); // Create a new graphics context to avoid affecting the main one
            for (DamageNumber damageNumber : damageNumbers) {
                damageNumber.draw(g2d);
            }
            g2d.dispose(); // Clean up
        }
    }
}
