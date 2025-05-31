/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class is used to show the numbers representing damage taken when enemies are hit
*/

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class DamageNumber {
    // Static constants for visual appearance
    private static final Color TEXT_COLOR = new Color(255, 80, 120); // Brighter, more saturated red
    private static final Color GLOW_COLOR = new Color(255, 200, 200, 80); // Additional glow
    private static final Color SHADOW_COLOR = new Color(40, 10, 30, 200); // Darker shadow for contrast
    private static final int LIFE_SPAN = 60; // Increased lifespan for better visibility
    private static final Random RNG = new Random();

    // Instance variables
    private double x, y;
    private final int damage;
    private int life = LIFE_SPAN;
    private final double initialY;
    private final float hueShift;

    /**
     * Constructs a new damage number at the specified position
     * 
     * @param x      The x coordinate
     * @param y      The y coordinate
     * @param damage The damage amount to display
     */
    public DamageNumber(int x, int y, int damage) {
        this.x = x;
        this.y = this.initialY = y;
        this.damage = damage;
        this.hueShift = RNG.nextFloat() * 0.2f - 0.1f; // Slight color variation
    }

    /**
     * Updates the damage number's position and lifetime
     * 
     * @param dt Delta time for frame-rate independence
     */
    public void update(float dt) {
        float progress = 1f - (life / (float) LIFE_SPAN);
        y = initialY - (80 * progress + 20 * Math.sin(Math.PI * progress));
        life--;
    }

    /**
     * Checks if the damage number should be removed
     * 
     * @return true if the damage number has exceeded its lifetime
     */
    public boolean isDead() {
        return life <= 0;
    }

    /**
     * Draws the damage number with visual effects
     * 
     * @param g The graphics context
     */    
    public void draw(Graphics2D g) {
        float lifeRatio = life / (float) LIFE_SPAN;
        float alpha = lifeRatio > 0.3f ? 1f : (lifeRatio / 0.3f);

        // Dynamic scaling with bounce effect - increased scale for better visibility
        float scale = 1.2f + 0.8f * (float) Math.sin(Math.PI * (1 - lifeRatio));

        String text = String.valueOf(damage);
        Font originalFont = g.getFont();
        Font font = originalFont.deriveFont(Font.BOLD, 18f * scale); // Larger font size
        g.setFont(font);

        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int textX = (int) x - textWidth / 2;
        int textY = (int) y;

        // Save the original transform and composite
        Composite originalComposite = g.getComposite();
        AffineTransform originalTransform = g.getTransform();

        // Add outer glow for better visibility
        g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.6f));
        g.setColor(GLOW_COLOR);
        for (int i = 1; i <= 3; i++) {
            g.drawString(text, textX - i, textY - i);
            g.drawString(text, textX + i, textY - i);
            g.drawString(text, textX - i, textY + i);
            g.drawString(text, textX + i, textY + i);
        }

        // Enhanced shadow with blur effect
        g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.8f));
        g.setColor(SHADOW_COLOR);
        for (int i = 1; i <= 2; i++) {
            g.drawString(text, textX + i, textY + i);
        }

        // Main text with slight color variation
        g.setComposite(AlphaComposite.SrcOver.derive(alpha));
        g.setColor(TEXT_COLOR);
        g.drawString(text, textX, textY);

        // Restore original settings
        g.setFont(originalFont);
        g.setComposite(originalComposite);
        g.setTransform(originalTransform);
    }
}
