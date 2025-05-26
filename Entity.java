import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Entity extends GameObject {
    public final int MAX_HP;
    public final int MAX_SPEED;

    private float center_x, center_y;
    private float radius;

    private int hp;
    private String name;
    private boolean dead;

    public Entity(
            int x,
            int y,
            int width,
            int height,
            int max_hp,
            int max_speed,
            BufferedImage[] sprites) {
        super(x, y, width, height, sprites);
        this.MAX_HP = max_hp;
        this.MAX_SPEED = max_speed;
        this.center_x = x + width * 0.5f;
        this.center_y = y + height * 0.5f;
        this.radius = Math.max(width, height) * 0.5f;
        this.hp = max_hp;
    }

    public void update(float dt) {
        cx += vx * dt;
        cy += vy * dt;
        vx *= 0.90f;
        vy *= 0.90f; // <-- linear damping
        setPos(cx, cy);
    }

    public void setPos(double x_val, double y_val) {
        center_x = (float) x_val;
        center_y = (float) y_val;
        x = (int) (center_x - width * 0.5f);
        y = (int) (center_y - height * 0.5f);
    }

    public void setVelocity(float vx, float vy) {
        x_velocity = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, vx));
        y_velocity = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, vy));
    }

    public int getMaxHp() {
        return MAX_HP;
    }

    float cx, cy; // centre
    float vx, vy; // velocity

    public Rectangle2D.Float getCircleBounds() {
        return new Rectangle2D.Float(center_x - radius, center_y - radius, radius * 2f, radius * 2f);
    }

    public float getRadius() {
        return radius;
    }

    public void takeDamage(int damage) {
        hp -= damage;
        if (hp <= 0) {
            hp = 0;
            dead = true;
        }
    }

    public boolean isDead() {
        return dead;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp_val) {
        hp = Math.min(MAX_HP, Math.max(0, hp_val));
        dead = hp == 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
