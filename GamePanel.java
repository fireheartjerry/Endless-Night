import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int GAME_WIDTH = 1283;
    public static final int GAME_HEIGHT = 720;

    public GameState game_state;
    
    private final Font game_font;
    private final CardLayout screen_manager = new CardLayout();
    private final Thread game_thread;

    private final SoundManager sound_manager;

    public GamePanel() {
        game_state = GameState.INTRODUCTION;
        sound_manager = new SoundManager();
        game_font = loadFont("/assets/gamefont.ttf", 64f);
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setLayout(screen_manager);

        add(new IntroScreen(this), GameState.INTRODUCTION.name());
        add(new MainMenu(this), GameState.MAIN_MENU.name());
        add(new HowToPlayScreen(this), GameState.HOW_TO_PLAY.name());

        screen_manager.show(this, game_state.name());
        sound_manager.playBackgroundMusic("intro");
        game_thread = new Thread(this);
        game_thread.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
    }

    @Override
    public void run() {
        final double FPS = 60.0;
        final double NS_PER_UPDATE = 1_000_000_000.0 / FPS;
        long last_time = System.nanoTime();

        while (true) {
            long now = System.nanoTime();

            screen_manager.show(this, game_state.name());
            repaint();

            long sleep = (long)(last_time + NS_PER_UPDATE - now);
            if (sleep > 0) {
                try {
                    Thread.sleep(sleep / 1_000_000L, (int)(sleep % 1_000_000L));
                } catch (InterruptedException ignored) {}
            }
            last_time += NS_PER_UPDATE;
        }
    }    public void showScreen(String name) {
        try {
            game_state = GameState.valueOf(name.toUpperCase());
            
            // Check if the screen exists before switching
            if (game_state == GameState.PLAYING || 
                game_state == GameState.HIGH_SCORES || 
                game_state == GameState.CREDITS) {
                System.out.println("Screen not yet implemented: " + name);
                return;
            }
            
            screen_manager.show(this, game_state.name());
            requestFocusInWindow();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid screen name: " + name);
            return;
        }
    }

    public Font getGameFont() {
        return game_font;
    }

    private Font loadFont(String path, float size) {
        try {
            Font f = Font.createFont(Font.TRUETYPE_FONT, IntroScreen.class.getResourceAsStream(path));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(f);
            return f.deriveFont(size);
        } catch (Exception e) {
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }
    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}
}
