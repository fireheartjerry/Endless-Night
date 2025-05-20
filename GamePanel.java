
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {

    public static final int GAME_WIDTH = 1920;
    public static final int GAME_HEIGHT = 720;

    private final CardLayout screen_manager = new CardLayout();
    private Thread game_thread;

    public GamePanel() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setLayout(screen_manager);

        // Add the intro screen
        add(new IntroScreen(this), "intro");
        // Add the play screen
        add(new PlayScreen(this), "play");

        screen_manager.show(this, "intro");

        game_thread = new Thread(this);
        game_thread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // Do not dispose g2 here as it's needed for child components
    }

    @Override
    public void run() {
        final double FPS = 60.0;
        final double NS_PER_UPDATE = 1_000_000_000.0 / FPS;
        long last_time = System.nanoTime();

        while (true) {
            long now = System.nanoTime();
            repaint();
            long sleep = (long) (last_time + NS_PER_UPDATE - now);
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep / 1_000_000L, (int) (sleep % 1_000_000L));
                } catch (InterruptedException ignored) {
                }
            }
            last_time += NS_PER_UPDATE;
        }
    }

    public void showScreen(String name) {
        screen_manager.show(this, name);
        requestFocusInWindow();
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
