import java.awt.*;
import java.awt.image.*;

public class GameObject extends Rectangle {
    protected final int WIDTH;
    protected final int HEIGHT;
    private BufferedImage[] sprites;
    protected float x_velocity;
    protected float y_velocity;
    private int current_frame;

    public GameObject(int x, int y, int width, int height, BufferedImage[] sprites) {
        super(x, y, width, height);
        this.WIDTH = width;
        this.HEIGHT = height;
        this.sprites = sprites;
    }

    public void setXVelocity(int x_velocity) {
        this.x_velocity = x_velocity;
    }

    public void setYVelocity(int y_velocity) {
        this.y_velocity = y_velocity;
    }

    public float getXVelocity() {
        return x_velocity;
    }

    public float getYVelocity() {
        return y_velocity;
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        x += x_velocity;
        y += y_velocity;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, WIDTH, HEIGHT);
    }
}
