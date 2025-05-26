import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;

public class Player extends Entity {
    private int level;
    private int high_scores[];
    private Map<Skill, Integer> skills;
    private static final int SPEED = 5;

    public Player(int x, int y, int width, int height, int max_hp, int max_speed, BufferedImage[] sprites) {
        super(x, y, width, height, max_hp, max_speed, sprites);
        this.level = 1;
        this.high_scores = new int[5];
        this.skills = new HashMap<>();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyChar() == 'd') {
            setXVelocity(SPEED);
            move();
        }

        if (e.getKeyChar() == 'a') {
            setXVelocity(SPEED * -1);
            move();
        }

        if (e.getKeyChar() == 'w') {
            setYVelocity(SPEED * -1);
            move();
        }

        if (e.getKeyChar() == 's') {
            setYVelocity(SPEED);
            move();
        }
    }

    public void keyReleased(KeyEvent e) {
        if (e.getKeyChar() == 'd') {
            setXVelocity(0);
            move();
        }

        if (e.getKeyChar() == 'a') {
            setXVelocity(0);
            move();
        }

        if (e.getKeyChar() == 'w') {
            setYVelocity(0);
            move();
        }

        if (e.getKeyChar() == 's') {
            setYVelocity(0);
            move();
        }
    }

    @Override
    public void move() {
        super.move();

        // If the player goes below border, loop back to the opposite side
        if (x < WIDTH*-1) {
            x = GamePanel.GAME_WIDTH;
        } else if (x > GamePanel.GAME_WIDTH) {
            x = -WIDTH;
        }
        if (y < HEIGHT*-1) {
            y = GamePanel.GAME_HEIGHT;
        } else if (y > GamePanel.GAME_HEIGHT) {
            y = -HEIGHT;
        }
    }

    @Override
    public void draw(Graphics g) {
        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        double angle = Math.atan2(mousePosition.y - (y + HEIGHT / 2), mousePosition.x - (x + WIDTH / 2));

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);

        AffineTransform oldTransform = g2d.getTransform();

        g2d.rotate(angle, x + WIDTH / 2, y + HEIGHT / 2);
        g2d.fillRect(x, y, WIDTH, HEIGHT);
        g2d.setTransform(oldTransform);
    }
}
