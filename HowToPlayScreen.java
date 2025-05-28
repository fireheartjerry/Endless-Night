import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.function.BiConsumer;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * This class represents the "How To Play" screen in the game. It provides
 * instructions and information about the game mechanics, monsters, bosses, and
 * skills.
 */
public class HowToPlayScreen extends JPanel {

    /**
     * Inner static class representing a single page of the "How To Play" screen.
     * Each page has a title and HTML content.
     */
    private static class Page {
        final String TITLE; // Title of the page
        final String HTML; // HTML content of the page

        // Constructor to initialize the title and HTML content of the page
        Page(String TITLE, String html) {
            this.TITLE = TITLE;
            this.HTML = html;
        }
    }

    // Array of pages to display in the "How To Play" screen
    private static final Page[] PAGES = {
            new Page(
                    "Game Mechanics Part 1",
                    "<html><div style='max-width:900px; font-size:15px; text-align:center;'>"
                            + "Endless Night is a top-down, arena-style survival game. You control a light-wielder at the center of a battlefield, fending off waves of shadowy monsters. Between waves you choose powerful upgrades and skills to improve your arsenal. Survive 50 waves to reclaim the light-or fall into eternal darkness.<br><br>"
                            + "<ul style='text-align:left; display:inline-block;'>"
                            + "<li><b>Key Controls:</b> standard WASD to move, P to pause while in-game.</li>"
                            + "<li><b>Wave System:</b> 50 total waves; each wave grows progressively harder.</li>"
                            + "<li><b>Health:</b> resets to 100% after each wave. If HP hits zero mid-wave, game over.</li>"
                            + "<li><b>Wave Completion:</b> choose one of three upgrades or new skills between waves.</li>"
                            + "<li><b>Boss Waves:</b> waves 45-50 (inclusive) are boss waves with powerful bosses to beat.</li>"
                            + "</ul>"
                            + "<br><br>Medkits spawn with a 2% chance for every monster killed. They heal 50% of your max HP.<br><br>"
                            + "</div></html>"),
            new Page(
                    "Game Mechanics Part 2",
                    "<html><div style='max-width:900px; font-size:15px; text-align:center;'>"
                            + "Monsters will randomly spawn in a roughly circular formation around you. They will always move towards you at a slow speed. If a monster comes into contact with you, you will lose HP. Stronger monsters will shoot projectiles that must be dodged or deal damage. There are 7 monsters in total, becoming stronger and stronger.<br><br>"
                            + "Bosses are a special type of monster that have greatly increased stats and are much more difficult to defeat. A unique boss spawns in each of the final five waves. In the next few pages there will be more detailed info on monsters and bosses.<br><br>"
                            + "Skills are powerful abilities that you can use to help defeat monsters. Skills attack passively and will either shoot towards your mouse (if aim), or at the nearest monster (homing). Each skill has a super version that is unlocked at level 9. All other skills must be at level 8 before level 9 is unlocked.<br><br>"
                            + "</div></html>"),
            new Page(
                    "Monsters, Bosses, & Skills",
                    "" // handled specially
            )
    };

    private final GamePanel PARENT; // Reference to the parent game panel
    private final Font UI_FONT; // Font used for UI elements
    private final JLabel TITLE_LABEL, TEXT_LABEL; // Labels for the title and text content
    private final GameButton PREV_BTN, NEXT_BTN, BACK_BTN; // Navigation buttons
    private BufferedImage bgImage; // Background image for the screen
    private int pageIndex = 0; // Current page index

    // Constructor to initialize the "How To Play" screen
    public HowToPlayScreen(GamePanel parent) {
        PARENT = parent;
        UI_FONT = PARENT.getGameFont();
        setLayout(null);
        setOpaque(false);
        loadBackground();

        // Initialize the title label
        TITLE_LABEL = new JLabel("", SwingConstants.CENTER);
        TITLE_LABEL.setFont(UI_FONT.deriveFont(Font.BOLD, 48f));
        TITLE_LABEL.setForeground(Color.WHITE);

        // Initialize the text label
        TEXT_LABEL = new JLabel("", SwingConstants.CENTER);
        TEXT_LABEL.setFont(UI_FONT.deriveFont(20f));
        TEXT_LABEL.setForeground(Color.WHITE);

        // Initialize navigation buttons
        PREV_BTN = makeNav("Previous", e -> changePage(-1));
        NEXT_BTN = makeNav("Next", e -> changePage(+1));
        BACK_BTN = makeNav("Back", e -> {
            changePage(-pageIndex);
            PARENT.showScreen("main_menu");
        });

        // Configure tooltips
        ToolTipManager.sharedInstance().setInitialDelay(100);
        UIManager.put("ToolTip.background", new Color(15, 15, 25, 230));
        UIManager.put("ToolTip.foreground", Color.WHITE);
        UIManager.put("ToolTip.font", UI_FONT.deriveFont(14f));
        UIManager.put("ToolTip.border",
                BorderFactory.createLineBorder(new Color(0, 200, 255), 1, true));
        UIManager.put("ToolTip.maxWidth", 300);

        // Register custom tooltip class
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(true);

        refreshPage();
    }

    // Loads the background image for the screen
    private void loadBackground() {
        try {
            bgImage = ImageIO.read(
                    getClass().getResourceAsStream("/assets/images/backgrounds/instructionsbg.jpg"));
        } catch (IOException e) {
            bgImage = null;
        }
    }

    // Creates a navigation button with the specified text and action listener
    private GameButton makeNav(String text, ActionListener al) {
        GameButton b = new GameButton(text);
        b.setFont(UI_FONT.deriveFont(28f));
        b.setForeground(Color.WHITE);
        b.addActionListener(al);
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(32f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setFont(UI_FONT.deriveFont(28f));
            }
        });
        return b;
    }

    // Changes the current page by the specified delta
    private void changePage(int delta) {
        pageIndex = Math.max(0, Math.min(PAGES.length - 1, pageIndex + delta));
        refreshPage();
    }

    // Refreshes the content of the current page
    private void refreshPage() {
        removeAll();

        Page p = PAGES[pageIndex];
        TITLE_LABEL.setText(p.TITLE);
        add(TITLE_LABEL);
        add(PREV_BTN);
        add(NEXT_BTN);
        add(BACK_BTN);

        if ("Monsters, Bosses, & Skills".equals(p.TITLE)) {
            showIconGrid();
        } else {
            TEXT_LABEL.setText(p.HTML);
            add(TEXT_LABEL);
        }

        positionComponents();
        PREV_BTN.setEnabled(pageIndex > 0);
        NEXT_BTN.setEnabled(pageIndex < PAGES.length - 1);
        revalidate();
        repaint();
    }

    // Creates a tooltip with the specified header and body
    private String makeTip(String header, String body) {
        return "<html><div style='text-align:left; width:280px; word-wrap:break-word;'>"
                + "<span style=\"font-size:14px;color:#00d0ff\"><b>" + header + "</b></span><br>"
                + "<span style=\"font-size:12px;color:#dddddd\">" + body + "</span></div></html>";
    }

    // Displays the icon grid for monsters, bosses, and skills
    private void showIconGrid() {
        int gap = 16;
        int size = 64;

        String[] monsterNames = {
                "Shadeling", "Gloomspawn", "Vampire Bats", "Shadow Walker",
                "Obsidian Maw", "Withering Wraith", "Midnight Abyss", "Chaos Demon"
        };
        String[] monsterTips = {
                "<i>The first to rise when the light fell. Featureless, silent, and endless. Their bodies barely hold form, but their hunger is unmistakable.</i><br><br>First wave: 1",
                "<i>Once angels of light. Now emptied of purpose, drifting aimlessly through shadow - their halos cracked, their forms unravelling.</i><br><br>First wave: 5",
                "<i>They move like torn scraps of the night sky. Their powerful wings propel them through the shadows.</i><br><br>First wave: 10<br><br><b>Increased speed.</b>",
                "<i>It doesn't run. It drifts. And when it stops, it's already behind you.</i><br><br>First wave: 15<br><b>Shoots small projectiles.</b>",
                "<i>Its jaw begins where its chest should end. When it opens, the world seems to bend inward.</i><br><br>First wave: 20<br><b>Shoots projectiles faster.</b>",
                "<i>You see it for a moment, and then something inside you feels smaller. Like it took something it shouldn't have.</i><br><br>First wave: 25<br><b>Shoots larger projectiles faster.</b>",
                "<i>It doesn't move toward you. It is movement. A gravity you can't explain pulling you into something you were never meant to see.</i><br><br>First wave: 30<br><b>Shoots 8 projectiles in a circle.</b>",
                "<i>It wasn't born. It fractured its way into being. Its limbs don't match and its existence bends space, time, and mercy.</i><br><br>First wave: 40<br><b>Shoots infrequent solid beams of void that do insane damage.</b>"
        };

        String[] bossNames = {
                "Void Titan", "Eclipse Harbinger", "Mist Stalker",
                "Night Devourer", "Anthony's Wrath"
        };
        String[] bossTips = {
                "A colossal lumbering monstrosity with ridiculously high HP and that does area of effect shadow slams.<br><br>Spawns in wave 46.",
                "A floating monster with crystals around it that charge up before releasing a devastating attack in the direction of the player. Player must hide behind a terrain object or will be killed instantly.<br><br>Spawns in wave 47.",
                "A predatory creature that shifts between gaseous and solid forms (10 seconds solid, 3 seconds mist). When intangible, it moves 50% faster, passes through obstacles, and leaves toxic mist that players are damaged when walking through.<br><br>Spawns in wave 48.",
                "A shadowy beast that turns the player's screen entirely black for 1 second every 10 seconds.<br><br>Spawns in wave 49.",
                "???"
        };

        String[] skillNames = {
                "Luminous Pulse", "Light Lance", "Photon Orbs",
                "Angelic Summons", "Starfall Ritual"
        };
        String[] skillTips = {
                "Radial area of effect region around you.<br><br>Level 9: \"solar flare\" - creates a massive burst of damage covering the entire screen and stunning all enemies for 0.5 seconds. This occurs every 10 seconds.",
                "An aimed precision beam that goes to your mouse.<br><br>Level 9: \"prismatic ray\" - beam splits into 5 colours rays upon hitting first enemy.",
                "Shoots homing projectile orbs.<br><br>Level 9: \"celestial constellation\" - orbs create devastating explosions on impact which blow up and do area damage.",
                "Spawns immortal light angels that have passive aura attack and limited lifespan.<br><br>Level 9: \"archangel\" - spawns a single very powerful immortal angel which permanently stays on the map and shoots its own projectiles and aura.",
                "Shoots omni-directional projectiles of starlight regularly.<br><br>Level 9: \"cosmic cataclysm\" - massive light meteor showers down on mouse every 5 seconds."

        };

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setOpaque(false);

        JLabel hover_label = new JLabel("Hover over icons for more info.");
        hover_label.setFont(UI_FONT.deriveFont(Font.BOLD, 24f));
        hover_label.setForeground(Color.WHITE);
        hover_label.setAlignmentX(Component.CENTER_ALIGNMENT);
        container.add(hover_label);

        BiConsumer<String[], String[]> addRow = (names, tips) -> {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, gap, gap));
            row.setOpaque(false);

            JLabel groupLabel = new JLabel(
                    names == monsterNames ? "Monsters:"
                            : names == bossNames ? "Bosses:"
                                    : "Skills:");
            groupLabel.setFont(UI_FONT.deriveFont(Font.BOLD, 24f));
            groupLabel.setForeground(Color.WHITE);
            row.add(groupLabel);
            for (int i = 0; i < names.length; i++) {
                JLabel icon = new JLabel() {
                };
                icon.setPreferredSize(new Dimension(size, size));
                icon.setBorder(BorderFactory.createLineBorder(Color.CYAN, 2));
                icon.setIcon(new ImageIcon(new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)));
                icon.setToolTipText(makeTip(names[i], tips[i]));
                row.add(icon);
            }
            container.add(row);
        };

        addRow.accept(monsterNames, monsterTips);
        addRow.accept(bossNames, bossTips);
        addRow.accept(skillNames, skillTips);

        container.setBounds(50, 130, getWidth() - 100, getHeight() - 200);
        add(container);
    }

    // Positions the components on the screen
    private void positionComponents() {
        int w = getWidth() > 0 ? getWidth() : GamePanel.GAME_WIDTH;
        int h = getHeight() > 0 ? getHeight() : GamePanel.GAME_HEIGHT;

        TITLE_LABEL.setBounds(50, 50, w - 100, 50);
        TEXT_LABEL.setBounds((w - 900) / 2, 130, 900, 480);

        int btnW = 140, btnH = 50, gap = 20;
        int startX = (w - (btnW * 3 + gap * 2)) / 2;
        int y = h - btnH - 40;
        PREV_BTN.setBounds(startX - 30, y, btnW + 30, btnH);
        NEXT_BTN.setBounds(startX + btnW + gap, y, btnW, btnH);
        BACK_BTN.setBounds(startX + 2 * (btnW + gap), y, btnW, btnH);
    }

    // Paints the background image with transparency
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bgImage != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            g2.drawImage(bgImage, 0, 0, getWidth(), getHeight(), null);
            g2.dispose();
        }
    }
}
