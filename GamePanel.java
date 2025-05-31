import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable, KeyListener, MouseListener, MouseMotionListener {
    // Constants for game dimensions
    public static final int GAME_WIDTH = 1283;
    public static final int GAME_HEIGHT = 720;

    // Game state and core components
    public GameState game_state;
    public Player player;
    public CopyOnWriteArrayList<Enemy> enemies;
    public Map<String, Map<String, Object>> skill_map;

    // Heads-up display (HUD) and wave tracking
    public HUD hud;
    private int currentWave = 1;
    private int enemiesDefeated = 0;
    private int enemiesRequiredForNextWave = 10;

    // Font and screen management
    private final Font GAME_FONT;
    private final CardLayout SCREEN_MANAGER = new CardLayout();
    private final Thread GAME_THREAD;

    // Sound manager for background music and effects
    private final SoundManager SOUND_MANAGER; // Constructor initializes the game panel and its components

    public GamePanel() {
        game_state = GameState.INTRODUCTION;
        SOUND_MANAGER = new SoundManager();
        GAME_FONT = loadFont("/assets/gamefont.ttf", 64f);

        // Initialize skills map
        init();

        // Initialize player and enemies
        player = new Player(GAME_WIDTH / 2, GAME_HEIGHT / 2, 70, 70, 100, 10, null, this);
        enemies = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 10; i++) {
            enemies.add(createEnemy());
        }

        // Initialize HUD and wave progress
        hud = new HUD(player, GAME_FONT);
        hud.setCurrentWave(currentWave);
        hud.updateWaveProgress(enemiesDefeated, enemiesRequiredForNextWave);

        // Set up panel properties
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setFocusable(true);
        setBackground(Color.BLACK);
        setLayout(SCREEN_MANAGER);

        // Configure layout first before adding listeners to avoid leaking 'this'
        configureScreens();

        // Start the game thread
        GAME_THREAD = new Thread(this);
        GAME_THREAD.start();
    }

    // Configure screens and add listeners after constructor is complete
    private void configureScreens() {
        // Add key and mouse listeners
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);

        // Add screens to the card layout
        add(new IntroScreen(this), GameState.INTRODUCTION.name());
        add(new MainMenu(this), GameState.MAIN_MENU.name());
        add(new HowToPlayScreen(this), GameState.HOW_TO_PLAY.name());
        add(new PlayScreen(this), GameState.PLAYING.name());

        // Show the initial screen and start background music
        SCREEN_MANAGER.show(this, game_state.name());
        SOUND_MANAGER.playBackgroundMusic("intro");
    }

    // Initializes the skill map with predefined skills
    private void init() {
        skill_map = new HashMap<>();
        skill_map.put("Luminous Pulse", new HashMap<>());
        skill_map.put("Light Lance", new HashMap<>());
        skill_map.put("Photon Orbs", new HashMap<>());
        skill_map.put("Angelic Summons", new HashMap<>());
        skill_map.put("Starfall Ritual", new HashMap<>());
    }

    // Creates a new enemy at a random position around the player
    private Enemy createEnemy() {
        double centerX = GAME_WIDTH / 2.0;
        double centerY = GAME_HEIGHT / 2.0;

        // Randomize angle and radius for enemy spawn
        double angle = ThreadLocalRandom.current().nextDouble(0, Math.PI * 2);
        double radius = ThreadLocalRandom.current().nextDouble(350, 400);

        // Calculate enemy position
        int x = (int) Math.round(centerX + radius * Math.cos(angle));
        int y = (int) Math.round(centerY + radius * Math.sin(angle));

        return new Enemy(x, y, 20, 20, 50, 1, null, this);
    }

    // Paints the game components on the screen
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (game_state == GameState.PLAYING) {
            player.draw(g2);
            for (Enemy enemy : enemies) {
                enemy.draw(g2);
            }
            hud.draw(g2, GAME_WIDTH);
        }
    } // Updates the positions of the player and enemies

    public void move(float dt) {
        if (game_state == GameState.PLAYING) {
            player.move();
            player.update(dt, enemies);
            for (Enemy enemy : enemies) {
                enemy.update(dt, player);
                enemy.updateDamageNumbers(dt);
            }
        }
    } // Checks for collisions between game entities

    public void checkCollisions(float dt) {
        Physics.resolveCollisions(game_state, player, enemies, dt);

        // Remove dead enemies and spawn new ones
        updateEnemies();
    }

    // Handles removing dead enemies and spawning new ones
    private void updateEnemies() {
        List<Enemy> deadEnemies = new ArrayList<>();
        List<Enemy> newEnemies = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (enemy.isDead()) {
                deadEnemies.add(enemy);
                enemiesDefeated++;

                // Collect a new enemy to be added later
                newEnemies.add(createEnemy());

                // Update wave progress
                hud.updateWaveProgress(enemiesDefeated, enemiesRequiredForNextWave);

                // Check if wave is complete
                if (enemiesDefeated >= enemiesRequiredForNextWave) {
                    currentWave++;
                    enemiesDefeated = 0;
                    enemiesRequiredForNextWave += 5; // Increase enemies required for next wave
                    hud.setCurrentWave(currentWave);
                    hud.updateWaveProgress(enemiesDefeated, enemiesRequiredForNextWave);
                }
            }
        }

        // Remove dead enemies and add new ones
        enemies.removeAll(deadEnemies);
        enemies.addAll(newEnemies);
    }

    // Main game loop
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

            // Cap delta time to avoid spiral of death
            deltaTime = Math.min(deltaTime, MAX_DELTA_TIME);
            accumulator += deltaTime;

            if (game_state == GameState.PLAYING) {
                // Update game logic at fixed intervals
                while (accumulator >= FRAME_TIME) {
                    move(fixedDT);
                    checkCollisions(fixedDT);
                    accumulator -= FRAME_TIME;
                }
            }

            // Update the screen and repaint
            SCREEN_MANAGER.show(this, game_state.name());
            repaint();

            // Sleep to maintain consistent frame rate
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

    // Switches to a different screen based on the game state
    public void showScreen(String name) {
        try {
            GameState oldState = game_state;
            game_state = GameState.valueOf(name.toUpperCase());

            // Start or stop the HUD timer based on the game state
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

    // Returns the game font
    public Font getGameFont() {
        return GAME_FONT;
    }

    // Loads a custom font from a file
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

    // Handles key press events
    @Override
    public void keyPressed(KeyEvent e) {
        if (game_state == GameState.PLAYING) {
            player.keyPressed(e);
        }
    }

    // Handles key release events
    @Override
    public void keyReleased(KeyEvent e) {
        if (game_state == GameState.PLAYING) {
            player.keyReleased(e);
        }
    }

    // Handles key typed events (not used)
    @Override
    public void keyTyped(KeyEvent e) {
    }

    // Mouse position tracking
    private Point mousePosition = new Point(0, 0);

    // Mouse input handling @Override
    public void mouseClicked(MouseEvent e) {
        // The Light Lance now automatically fires, no need to handle mouse clicks
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public GamePanel getInstance() {
        return this;
    }
}
