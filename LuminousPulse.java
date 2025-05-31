import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class LuminousPulse extends Skill { // Core mechanics
    private static final int BASE_DMG = 1;
    private static final int DMG_PER_LVL = 2;
    private static final float AURA_RADIUS = 90f;
    private static final float PULSE_RADIUS = 160f;
    private static final long PULSE_COOLDOWN_MS = 450;
    // Note: Enemy velocity reduction is handled directly in Enemy.takeDamage()
    // method (80% reduction)

    // Enhanced color palette with better gradients
    private static final Color COL_CORE_INNER = new Color(80, 20, 160, 200);
    private static final Color COL_CORE_OUTER = new Color(120, 60, 200, 100);
    private static final Color COL_PULSE_BRIGHT = new Color(160, 80, 255, 180);
    private static final Color COL_PULSE_DIM = new Color(100, 40, 180, 80);
    private static final Color COL_PARTICLE = new Color(220, 160, 255, 220);
    private static final Color COL_DAMAGE_TEXT = new Color(255, 120, 150);
    private static final Color COL_DAMAGE_SHADOW = new Color(60, 20, 40, 180);

    private static final Random RNG = new Random();

    // Visual effect collections
    private final CopyOnWriteArrayList<PulseRing> rings = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<LightParticle> particles = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<DamageNumber> damageNumbers = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<AuraSparkle> sparkles = new CopyOnWriteArrayList<>();

    // Animation state
    private float idlePhase;
    private float intensityPhase;
    private long lastPulse;
    private int pulseCount = 0;

    private final Player owner;

    public LuminousPulse(Player owner) {
        super("Luminous Pulse", 1, 0, true);
        this.owner = owner;
    }

    public void update(float dt, List<Enemy> enemies) {
        // Update animation phases
        idlePhase = (idlePhase + dt * 2.5f) % (float) (Math.PI * 2);
        intensityPhase = (intensityPhase + dt * 4.2f) % (float) (Math.PI * 2);

        long now = System.currentTimeMillis();
        if (now - lastPulse >= PULSE_COOLDOWN_MS) {
            createPulseEffect();
            applyDamageAndSlow(enemies);
            lastPulse = now;
            pulseCount++;
        }

        // Update all visual effects
        updateEffects(dt);
        cleanupDeadEffects();

        // Spawn ambient effects
        spawnAmbientEffects(dt);
    }

    private void createPulseEffect() {
        // Create main pulse ring
        rings.add(new PulseRing(PulseRing.Type.MAIN));

        // Add secondary rings for enhanced visual impact
        if (getLevel() >= 3) {
            rings.add(new PulseRing(PulseRing.Type.SECONDARY, 0.15f));
        }

        // Create burst of particles
        spawnPulseBurst();
    }

    private void applyDamageAndSlow(List<Enemy> enemies) {
        for (Enemy enemy : enemies) {
            if (enemy.isDead())
                continue;

            double dx = enemy.getCenterX() - owner.getCenterX();
            double dy = enemy.getCenterY() - owner.getCenterY();
            double distanceSq = dx * dx + dy * dy;
            if (distanceSq <= PULSE_RADIUS * PULSE_RADIUS) {
                int damage = BASE_DMG + (getLevel() - 1) * DMG_PER_LVL;
                enemy.takeDamage(damage);

                // Create enhanced damage number
                damageNumbers.add(new DamageNumber(
                        (int) enemy.getCenterX(),
                        (int) enemy.getCenterY() - 20,
                        damage));

                // We don't need additional velocity reduction since the takeDamage() method now
                // handles this

                // Create impact particles
                spawnImpactEffect(enemy.getCenterX(), enemy.getCenterY());
            }
        }
    }

    public void draw(Graphics2D g) {
        Composite originalComposite = g.getComposite();
        Stroke originalStroke = g.getStroke();
        RenderingHints originalHints = g.getRenderingHints();

        // Enable anti-aliasing for smoother visuals
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        drawCoreAura(g);
        drawPulseField(g);
        drawVisualEffects(g);

        // Restore original graphics state
        g.setComposite(originalComposite);
        g.setStroke(originalStroke);
        g.setRenderingHints(originalHints);
    }

    private void drawCoreAura(Graphics2D g) {
        float centerX = (float) owner.getCenterX();
        float centerY = (float) owner.getCenterY();

        // Pulsing core aura with dynamic intensity
        float pulseIntensity = 0.3f * (float) Math.sin(idlePhase) + 0.7f;
        float breathe = 0.15f * (float) Math.sin(intensityPhase * 0.7f) + 0.85f;
        float finalRadius = AURA_RADIUS * pulseIntensity * breathe;

        // Multi-layered core effect
        g.setComposite(AlphaComposite.SrcOver.derive(0.4f));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(centerX, centerY),
                finalRadius,
                new float[] { 0f, 0.6f, 1f },
                new Color[] { COL_CORE_INNER, COL_CORE_OUTER, new Color(120, 60, 200, 0) }));

        int radius = (int) finalRadius;
        g.fillOval((int) (centerX - radius), (int) (centerY - radius),
                radius * 2, radius * 2);
    }

    private void drawPulseField(Graphics2D g) {
        float centerX = (float) owner.getCenterX();
        float centerY = (float) owner.getCenterY();

        // Outer pulse field with subtle animation
        float fieldPulse = 0.1f * (float) Math.sin(idlePhase * 0.5f) + 0.9f;

        g.setComposite(AlphaComposite.SrcOver.derive(0.15f));
        g.setPaint(new RadialGradientPaint(
                new Point2D.Float(centerX, centerY),
                PULSE_RADIUS * fieldPulse,
                new float[] { 0f, 0.4f, 0.8f, 1f },
                new Color[] {
                        new Color(140, 80, 200, 60),
                        new Color(100, 60, 160, 40),
                        new Color(80, 40, 120, 20),
                        new Color(80, 40, 120, 0)
                }));

        int fieldRadius = (int) (PULSE_RADIUS * fieldPulse);
        g.fillOval((int) (centerX - fieldRadius), (int) (centerY - fieldRadius),
                fieldRadius * 2, fieldRadius * 2);
    }

    private void drawVisualEffects(Graphics2D g) {
        // Draw effects in proper z-order
        sparkles.forEach(sparkle -> sparkle.draw(g));
        rings.forEach(ring -> ring.draw(g));
        particles.forEach(particle -> particle.draw(g));
        damageNumbers.forEach(number -> number.draw(g));
    }

    private void updateEffects(float dt) {
        rings.forEach(ring -> ring.update(dt));
        particles.forEach(particle -> particle.update(dt));
        damageNumbers.forEach(number -> number.update(dt));
        sparkles.forEach(sparkle -> sparkle.update(dt));
    }

    private void cleanupDeadEffects() {
        rings.removeIf(PulseRing::isDead);
        particles.removeIf(LightParticle::isDead);
        damageNumbers.removeIf(DamageNumber::isDead);
        sparkles.removeIf(AuraSparkle::isDead);
    }

    private void spawnAmbientEffects(float dt) {
        // Ambient particle spawning
        if (RNG.nextFloat() < dt * 25f) {
            spawnAmbientParticles();
        }

        // Aura sparkles
        if (RNG.nextFloat() < dt * 15f) {
            spawnAuraSparkles();
        }
    }

    private void spawnPulseBurst() {
        int particleCount = 6 + RNG.nextInt(4);
        for (int i = 0; i < particleCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double distance = RNG.nextDouble() * PULSE_RADIUS * 0.8;
            double x = owner.getCenterX() + Math.cos(angle) * distance;
            double y = owner.getCenterY() + Math.sin(angle) * distance;

            double speed = 2.5 + RNG.nextDouble() * 2;
            double vx = Math.cos(angle) * speed * (0.5 + RNG.nextDouble() * 0.5);
            double vy = Math.sin(angle) * speed * (0.5 + RNG.nextDouble() * 0.5);

            particles.add(new LightParticle(x, y, vx, vy,
                    40 + RNG.nextInt(20), LightParticle.Type.PULSE));
        }
    }

    private void spawnAmbientParticles() {
        int count = 2 + RNG.nextInt(2);
        for (int i = 0; i < count; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double distance = RNG.nextDouble() * AURA_RADIUS * 0.9;
            double x = owner.getCenterX() + Math.cos(angle) * distance;
            double y = owner.getCenterY() + Math.sin(angle) * distance;

            double vx = (RNG.nextDouble() - 0.5) * 1.2;
            double vy = (RNG.nextDouble() - 0.5) * 1.2;

            particles.add(new LightParticle(x, y, vx, vy,
                    30 + RNG.nextInt(15), LightParticle.Type.AMBIENT));
        }
    }

    private void spawnAuraSparkles() {
        double angle = RNG.nextDouble() * Math.PI * 2;
        double distance = RNG.nextDouble() * AURA_RADIUS;
        double x = owner.getCenterX() + Math.cos(angle) * distance;
        double y = owner.getCenterY() + Math.sin(angle) * distance;

        sparkles.add(new AuraSparkle(x, y, 20 + RNG.nextInt(10)));
    }

    private void spawnImpactEffect(double x, double y) {
        int count = 3 + RNG.nextInt(3);
        for (int i = 0; i < count; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double speed = 1.5 + RNG.nextDouble();
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;

            particles.add(new LightParticle(x, y, vx, vy,
                    15 + RNG.nextInt(10), LightParticle.Type.IMPACT));
        }
    }

    // Enhanced PulseRing class with multiple types
    private class PulseRing {
        enum Type {
            MAIN, SECONDARY
        }

        private double radius = 10;
        private int life = 60; // Extended life for smoother transitions
        private float speed;
        private final Type type;
        private final float delay;
        private float currentDelay;

        private final Stroke dashStroke = new BasicStroke(
                4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                12f, new float[] { 12f, 8f }, 0f); // Modernized dash pattern for a premium look

        public PulseRing(Type type) {
            this(type, 0f);
        }

        public PulseRing(Type type, float delay) {
            this.type = type;
            this.delay = delay;
            this.currentDelay = delay;
            this.speed = type == Type.MAIN ? 600f : 650f; // Faster initial speed for dynamic visuals
        }

        public void update(float dt) {
            if (currentDelay > 0) {
                currentDelay -= dt;
                return;
            }

            radius += speed * dt;
            speed *= 0.92f; // Gradual deceleration for cinematic expansion
            life--;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void draw(Graphics2D g) {
            if (life <= 0 || currentDelay > 0)
                return;

            float lifeRatio = life / 60f;
            float alpha = lifeRatio > 0.8f ? 1f : (lifeRatio / 0.8f); // Smooth fade-out
            alpha *= (type == Type.MAIN ? 1.0f : 0.8f); // Brighter for main rings

            double centerX = owner.getCenterX();
            double centerY = owner.getCenterY();

            // Dynamic fill effect with advanced gradient glow
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.5f));
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(centerX, centerY),
                    (float) radius,
                    new float[] { 0.5f, 0.8f, 1f },
                    new Color[] { COL_PULSE_DIM, new Color(140, 70, 220, 100), new Color(100, 50, 200, 0) }));
            g.fill(new Ellipse2D.Double(centerX - radius, centerY - radius,
                    radius * 2, radius * 2));

            // Outer ring with glowing edge
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.setStroke(dashStroke);
            g.setColor(type == Type.MAIN ? COL_PULSE_BRIGHT : COL_PULSE_DIM);
            g.draw(new Ellipse2D.Double(centerX - radius, centerY - radius,
                    radius * 2, radius * 2));

            // Inner ripple effect for added depth
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.3f));
            g.setPaint(new RadialGradientPaint(
                    new Point2D.Double(centerX, centerY),
                    (float) radius * 0.6f,
                    new float[] { 0f, 1f },
                    new Color[] { COL_PULSE_BRIGHT, new Color(180, 90, 255, 0) }));
            g.fill(new Ellipse2D.Double(centerX - radius * 0.6, centerY - radius * 0.6,
                    radius * 1.2, radius * 1.2));

            // Subtle particle-like shimmer for modern aesthetics
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.2f));
            g.setColor(new Color(255, 255, 255, 100));
            for (int i = 0; i < 5; i++) {
                double angle = RNG.nextDouble() * Math.PI * 2;
                double shimmerRadius = radius * (0.7 + RNG.nextDouble() * 0.3);
                double shimmerX = centerX + Math.cos(angle) * shimmerRadius;
                double shimmerY = centerY + Math.sin(angle) * shimmerRadius;
                g.fill(new Ellipse2D.Double(shimmerX - 2, shimmerY - 2, 4, 4));
            }
        }
    }

    // Enhanced LightParticle class with different behaviors
    private class LightParticle {
        enum Type {
            PULSE, AMBIENT, IMPACT
        }

        private double x, y, vx, vy;
        private int life, maxLife;
        private final Type type;
        private boolean homing = false;
        private float size;

        public LightParticle(double x, double y, double vx, double vy, int life, Type type) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = this.maxLife = life;
            this.type = type;
            this.size = type == Type.IMPACT ? 1.5f : (2f + RNG.nextFloat());
        }

        public void update(float dt) {
            switch (type) {
                case PULSE:
                    updatePulseParticle(dt);
                    break;
                case AMBIENT:
                    updateAmbientParticle(dt);
                    break;
                case IMPACT:
                    updateImpactParticle(dt);
                    break;
            }

            x += vx * dt * 60;
            y += vy * dt * 60;
            life--;
        }

        private void updatePulseParticle(float dt) {
            if (!homing && life < maxLife * 0.4) {
                homing = true;
            }

            if (homing) {
                double dx = owner.getCenterX() - x;
                double dy = owner.getCenterY() - y;
                double distance = Math.hypot(dx, dy) + 1e-3;
                double homingForce = 35;

                vx += (dx / distance) * homingForce * dt;
                vy += (dy / distance) * homingForce * dt;
            }

            vx *= 0.94;
            vy *= 0.94;
        }

        private void updateAmbientParticle(float dt) {
            // Gentle floating motion
            vx += (RNG.nextFloat() - 0.5) * 0.5 * dt;
            vy += (RNG.nextFloat() - 0.5) * 0.5 * dt;
            vx *= 0.98;
            vy *= 0.98;
        }

        private void updateImpactParticle(float dt) {
            vx *= 0.92;
            vy *= 0.92;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            if (type == Type.IMPACT) {
                alpha = Math.min(alpha * 2f, 1f); // Brighter initially
            }

            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g.setColor(COL_PARTICLE);

            int px = (int) x;
            int py = (int) y;
            int s = (int) size;

            // Cross pattern for better visibility
            g.fillRect(px - s, py - 1, s * 2 + 1, 3);
            g.fillRect(px - 1, py - s, 3, s * 2 + 1);
        }
    }

    // New AuraSparkle class for ambient effects
    private class AuraSparkle {
        private final double x, y;
        private int life, maxLife;
        private final float rotation;

        public AuraSparkle(double x, double y, int life) {
            this.x = x;
            this.y = y;
            this.life = this.maxLife = life;
            this.rotation = RNG.nextFloat() * 360f;
        }

        public void update(float dt) {
            life--;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void draw(Graphics2D g) {
            float alpha = (float) life / maxLife;
            float scale = 0.5f + 0.5f * (float) Math.sin(Math.PI * (1 - alpha));

            g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.7f));
            g.setColor(new Color(255, 200, 255, 180));

            AffineTransform oldTransform = g.getTransform();
            g.translate(x, y);
            g.rotate(Math.toRadians(rotation));
            g.scale(scale, scale);

            // Draw sparkle as small star
            int[] xPoints = { 0, -2, -6, -2, 0, 2, 6, 2 };
            int[] yPoints = { -6, -2, 0, 2, 6, 2, 0, -2 };
            g.fillPolygon(xPoints, yPoints, 8);

            g.setTransform(oldTransform);
        }
    }

    // Enhanced DamageNumber class with better animations
    private class DamageNumber {
        private double x, y;
        private final int damage;
        private int life = 40;
        private final double initialY;
        private final float hueShift;

        public DamageNumber(int x, int y, int damage) {
            this.x = x;
            this.y = this.initialY = y;
            this.damage = damage;
            this.hueShift = RNG.nextFloat() * 0.2f - 0.1f; // Slight color variation
        }

        public void update(float dt) {
            float progress = 1f - (life / 40f);
            y = initialY - (80 * progress + 20 * Math.sin(Math.PI * progress));
            life--;
        }

        public boolean isDead() {
            return life <= 0;
        }

        public void draw(Graphics2D g) {
            float lifeRatio = life / 40f;
            float alpha = lifeRatio > 0.3f ? 1f : (lifeRatio / 0.3f);

            // Dynamic scaling with bounce effect
            float scale = 1f + 0.6f * (float) Math.sin(Math.PI * (1 - lifeRatio));

            String text = String.valueOf(damage);
            Font font = g.getFont().deriveFont(Font.BOLD, 16f * scale);
            g.setFont(font);

            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            int textX = (int) x - textWidth / 2;
            int textY = (int) y;

            // Enhanced shadow with blur effect
            g.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.8f));
            g.setColor(COL_DAMAGE_SHADOW);
            for (int i = 1; i <= 2; i++) {
                g.drawString(text, textX + i, textY + i);
            }

            // Main text with slight color variation
            g.setComposite(AlphaComposite.SrcOver.derive(alpha));
            Color textColor = new Color(
                    Math.max(0, Math.min(255, (int) (COL_DAMAGE_TEXT.getRed() * (1 + hueShift)))),
                    Math.max(0, Math.min(255, (int) (COL_DAMAGE_TEXT.getGreen() * (1 + hueShift * 0.5f)))),
                    Math.max(0, Math.min(255, (int) (COL_DAMAGE_TEXT.getBlue() * (1 - hueShift * 0.3f)))));
            g.setColor(textColor);
            g.drawString(text, textX, textY);
        }
    }
}