import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public final class Enemy extends Entity {
    private static final float MAX_SPEED = 2.4f;
    private static final float ARRIVAL_RADIUS = 110f;
    private static final float RESPONSIVENESS = 0.18f;
    private static final float WANDER_RADIUS = 0.45f;
    private static final float WANDER_JITTER = 0.25f;
    private static final float KNOCKBACK_FRICTION = 0.9f;
    private static final float MIN_SPEED = 0.05f;
    private static final float SEPARATION_WEIGHT = 1.2f;
    private static final float SEPARATION_RADIUS = 100f;
    private static final float RECOVERY_RATE = 0.05f;

    private int damage = 2;
    private boolean knocked_back;
    private float wander_angle = (float) (Math.random() * Math.PI * 2.0);
    private GamePanel gamePanel;

    public Enemy(int x, int y, int w, int h, int max_hp, int max_speed, BufferedImage[] sprites) {
        super(x, y, w, h, max_hp, max_speed, sprites);
    }

    public void update(float dt, Player player) {
        if (knocked_back) {
            handleKnockback(dt);
            return;
        }
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
        float to_x = (float) (player.getCenterX() - getCenterX());
        float to_y = (float) (player.getCenterY() - getCenterY());
        float dist = (float) Math.hypot(to_x, to_y);
        if (dist > 1e-3f) {
            to_x /= dist;
            to_y /= dist;
        }
        float desired_speed;
        if (dist < ARRIVAL_RADIUS) {
            float t = dist / ARRIVAL_RADIUS;
            desired_speed = MAX_SPEED * t * t * (3 - 2 * t);
        } else {
            desired_speed = MAX_SPEED;
        }
        float desired_x = to_x * desired_speed;
        float desired_y = to_y * desired_speed;
        wander_angle += (Math.random() - 0.5f) * WANDER_JITTER * dt * 60f;
        wander_angle *= 0.98f;
        float wander_strength = Math.min(1.0f, dist / 200.0f);
        desired_x += Math.cos(wander_angle) * WANDER_RADIUS * wander_strength;
        desired_y += Math.sin(wander_angle) * WANDER_RADIUS * wander_strength;
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
                float sep_mag = (float) Math.hypot(sep_x, sep_y);
                if (sep_mag > MAX_SPEED) {
                    sep_x = (sep_x / sep_mag) * MAX_SPEED;
                    sep_y = (sep_y / sep_mag) * MAX_SPEED;
                }
                desired_x = desired_x * 0.8f + sep_x * 0.2f;
                desired_y = desired_y * 0.8f + sep_y * 0.2f;
            }
        }
        float dot = to_x * x_velocity + to_y * y_velocity;
        float dynamic_responsiveness = RESPONSIVENESS * (1.0f + (1.0f - Math.max(0, dot)) * 0.5f);
        float steer_x = desired_x - x_velocity;
        float steer_y = desired_y - y_velocity;
        x_velocity += steer_x * dynamic_responsiveness * dt * 60f;
        y_velocity += steer_y * dynamic_responsiveness * dt * 60f;
        float speed = (float) Math.hypot(x_velocity, y_velocity);
        if (speed > MAX_SPEED) {
            float reduction = 0.8f + 0.2f * (MAX_SPEED / speed);
            x_velocity *= reduction;
            y_velocity *= reduction;
        }
        x += x_velocity * dt * 60f;
        y += y_velocity * dt * 60f;
        wrap();
    }

    public void move(Player player) {
        update(1.0f / 60.0f, player);
    }

    private void handleKnockback(float dt) {
        float decay = (float) Math.pow(KNOCKBACK_FRICTION, dt * 60);
        x_velocity *= decay;
        y_velocity *= decay;
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
        x += x_velocity * dt * 60f;
        y += y_velocity * dt * 60f;
        if (Math.hypot(x_velocity, y_velocity) < MIN_SPEED) {
            x_velocity = y_velocity = 0f;
            knocked_back = false;
        }
        wrap();
    }

    private void handleKnockback() {
        handleKnockback(1.0f / 60.0f);
    }

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

    public void applyKnockback(float vx, float vy) {
        x_velocity = vx;
        y_velocity = vy;
        knocked_back = true;
    }

    public boolean isKnockedBack() {
        return knocked_back;
    }

    public boolean isInKnockbackState() {
        return knocked_back;
    }

    public int getDamage() {
        return damage;
    }

    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
        if (knocked_back) {
            g.setColor(Color.YELLOW);
            g.drawLine(
                    (int) getCenterX(),
                    (int) getCenterY(),
                    (int) (getCenterX() + x_velocity * 5),
                    (int) (getCenterY() + y_velocity * 5));
        }
    }
}
