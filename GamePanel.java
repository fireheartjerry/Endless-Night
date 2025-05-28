import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int GAME_WIDTH = 1283;
    public static final int GAME_HEIGHT = 720;
    public GameState game_state;
    public Player player;
    public List<Enemy> enemies;
    public Map<String, Map<String, Object>> skill_map;

    public HUD hud;
    private int currentWave = 1;
    private int enemiesDefeated = 0;
    private int enemiesRequiredForNextWave = 10;

    private final Font GAME_FONT;
    private final CardLayout SCREEN_MANAGER = new CardLayout();
    private final Thread GAME_THREAD;

    private final SoundManager SOUND_MANAGER;

    public GamePanel() {
        game_state = GameState.PLAYING;
        SOUND_MANAGER = new SoundManager();
        game_font = loadFont("/assets/GAME_FONT.ttf", 64f);
        player = new Player(GAME_WIDTH / 2, GAME_HEIGHT / 2, 50, 50, 100, 10, null);
        enemies = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            enemies.add(createEnemy());
        }

        hud = new HUD(player, game_font);
        hud.setCurrentWave(currentWave);
        hud.updateWaveProgress(enemiesDefeated, enemiesRequiredForNextWave);

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        setBackground(Color.BLACK);
        setLayout(SCREEN_MANAGER);

        add(new IntroScreen(this), GameState.INTRODUCTION.name());
        add(new MainMenu(this), GameState.MAIN_MENU.name());
        add(new HowToPlayScreen(this), GameState.HOW_TO_PLAY.name());
        add(new PlayScreen(this), GameState.PLAYING.name());

        SCREEN_MANAGER.show(this, game_state.name());
        SOUND_MANAGER.playBackgroundMusic("intro");
        GAME_THREAD = new Thread(this);
        GAME_THREAD.start();
    }

    private void init() {
        skill_map = new HashMap<>();
        skill_map.put("Luminous Pulse", new HashMap<>());
        skill_map.put("Light Lance", new HashMap<>());
        skill_map.put("Photon Orbs", new HashMap<>());
        skill_map.put("Angelic Summons", new HashMap<>());
        skill_map.put("Starfall Ritual", new HashMap<>());
    }

    private Enemy createEnemy() {
        double centerX = GAME_WIDTH / 2.0;
        double centerY = GAME_HEIGHT / 2.0;

        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        double radius = ThreadLocalRandom.current().nextDouble(350, 400);

        int x = (int) Math.round(centerX + radius * Math.cos(angle));
        int y = (int) Math.round(centerY + radius * Math.sin(angle));

        return new Enemy(x, y, 40, 40, 100, 1, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (game_state == GameState.PLAYING) {
            player.draw(g2);
            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }
            hud.draw(g2, GAME_WIDTH);
        }
    }

    public void move(float dt) {
        if (game_state == GameState.PLAYING) {
            player.move();
            for (Enemy enemy : enemies) {
                enemy.update(dt, player);
            }
        }
    }

    public void checkCollisions(float dt) {
        Physics.resolveCollisions(game_state, player, enemies, dt);
    }

    @Override
    public void run() {
        final double TARGET_FPS = 60.0;
        final double FRAME_TIME = 1.0 / TARGET_FPS;
        final double MAX_DELTA_TIME = 0.25;

        final long NS_PER_UPDATE = (long) (1_000_000_000.0 / TARGET_FPS);
        final float fixedDT = (float) FRAME_TIME;

        long lastTime = System.nanoTime();
        double accumulator = 0.0;

        while (true) {
            long currentTime = System.nanoTime();
            double deltaTime = (currentTime - lastTime) / 1_000_000_000.0;
            lastTime = currentTime;

            deltaTime = Math.min(deltaTime, MAX_DELTA_TIME);
            accumulator += deltaTime;

            if (game_state == GameState.PLAYING) {
                while (accumulator >= FRAME_TIME) {
                    move(fixedDT);
                    checkCollisions(fixedDT);
                    accumulator -= FRAME_TIME;
                }
            }

            SCREEN_MANAGER.show(this, game_state.name());
            repaint();

            long elapsed = System.nanoTime() - currentTime;
            long sleepTime = NS_PER_UPDATE - elapsed;

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1_000_000L, (int) (sleepTime % 1_000_000L));
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    public void showScreen(String name) {
        try {
            GameState oldState = game_state;
            game_state = GameState.valueOf(name.toUpperCase());

            if (game_state == GameState.PLAYING && oldState != GameState.PLAYING) {
                hud.startTimer();
            } else if (oldState == GameState.PLAYING && game_state != GameState.PLAYING) {
                hud.stopTimer();
            }

            SCREEN_MANAGER.show(this, game_state.name());
            requestFocusInWindow();
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid screen name: " + name);
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
        } catch (java.awt.FontFormatException | java.io.IOException e) {
            System.err.println("Could not load font from " + path + ": " + e.getMessage());
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (game_state == GameState.PLAYING) {
            player.keyPressed(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (game_state == GameState.PLAYING) {
            player.keyReleased(e);
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }
}
