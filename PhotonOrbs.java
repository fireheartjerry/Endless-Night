import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhotonOrbs extends Skill {
    // Core mechanics
    private static final int BASE_DAMAGE = 5;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final float ORB_RADIUS = 8f;
    private static final float ORB_SPEED = 7f;
    private static final float MAX_ORB_SPEED = 12f;
    private static final float ORB_ACCELERATION = 0.3f;
    private static final float HOMING_STRENGTH = 0.5f; // How aggressively the orbs track enemies
    private static final long ORB_SPAWN_COOLDOWN_MS = 500; // 2 orbs per second
    
    // Visual properties
    private static final Color ORB_CORE_COLOR = new Color(30, 180, 255, 255); // Bright blue core
    private static final Color ORB_GLOW_COLOR = new Color(100, 200, 255, 160); // Blue glow
    private static final Color ORB_TRAIL_COLOR = new Color(80, 170, 255, 120); // Trail color
    private static final Color IMPACT_FLASH_COLOR = new Color(220, 240, 255, 200); // Bright flash on impact
    private static final Color DAMAGE_TEXT_COLOR = new Color(50, 200, 255); // Bright blue damage text
    
    private static final Random RNG = new Random();
    
    // Collection of active orbs and effects
    private final CopyOnWriteArrayList<PhotonOrb> orbs = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<OrbTrailParticle> trailParticles = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<ImpactEffect> impactEffects = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<DamageNumber> damageNumbers = new CopyOnWriteArrayList<>();
    
    private final Player owner;
    private long lastOrbSpawnTime = 0;
    
    public PhotonOrbs(Player owner) {
        super("Photon Orbs", 1, 0, true); // Passive skill that's always active
        this.owner = owner;
    }
    
    public void update(float dt, List<Enemy> enemies) {
        spawnOrbs(enemies);
        updateOrbs(dt, enemies);
        updateEffects(dt);
        cleanupDeadEffects();
    }
    
    private void spawnOrbs(List<Enemy> enemies) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastOrbSpawnTime >= ORB_SPAWN_COOLDOWN_MS) {
            Enemy targetEnemy = findClosestEnemy(enemies);
            if (targetEnemy != null) {
                // Spawn the orb with fancy spiral starting pattern
                double angle = Math.random() * Math.PI * 2;
                float offsetX = (float)(Math.cos(angle) * 30);
                float offsetY = (float)(Math.sin(angle) * 30);
                
                // Create the orb with a starting position slightly offset from player
                orbs.add(new PhotonOrb(
                    owner.getCenterX() + offsetX, 
                    owner.getCenterY() + offsetY,
                    targetEnemy
                ));
                
                // Add initial spawn effect
                for (int i = 0; i < 8; i++) {
                    double particleAngle = Math.random() * Math.PI * 2;
                    float speed = 1f + (float)(Math.random() * 2f);
                    trailParticles.add(new OrbTrailParticle(
                        (float)(owner.getCenterX() + offsetX),
                        (float)(owner.getCenterY() + offsetY),
                        (float)(Math.cos(particleAngle) * speed),
                        (float)(Math.sin(particleAngle) * speed),
                        (int)(10 + Math.random() * 10),
                        OrbTrailParticle.Type.SPAWN
                    ));
                }
                
                lastOrbSpawnTime = currentTime;
            }
        }
    }
    
    private void updateOrbs(float dt, List<Enemy> enemies) {
        for (PhotonOrb orb : orbs) {
            orb.update(dt, enemies);
            
            // Generate trail particles as orb moves
            if (Math.random() < 0.3) {
                trailParticles.add(new OrbTrailParticle(
                    orb.x, orb.y,
                    (float)(Math.random() * 1.0 - 0.5), 
                    (float)(Math.random() * 1.0 - 0.5),
                    (int)(5 + Math.random() * 10),
                    OrbTrailParticle.Type.TRAIL
                ));
            }
        }
    }
    
    private void updateEffects(float dt) {
        trailParticles.forEach(particle -> particle.update(dt));
        impactEffects.forEach(effect -> effect.update(dt));
        damageNumbers.forEach(number -> number.update(dt));
    }
    
    private void cleanupDeadEffects() {
        orbs.removeIf(PhotonOrb::isDead);
        trailParticles.removeIf(OrbTrailParticle::isDead);
        impactEffects.removeIf(ImpactEffect::isDead);
        damageNumbers.removeIf(DamageNumber::isDead);
    }
    
    private Enemy findClosestEnemy(List<Enemy> enemies) {
        Enemy closest = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;
            
            double dx = enemy.getCenterX() - owner.getCenterX();
            double dy = enemy.getCenterY() - owner.getCenterY();
            double distanceSq = dx * dx + dy * dy;
            
            if (distanceSq < minDistance) {
                minDistance = distanceSq;
                closest = enemy;
            }
        }
        
        return closest;
    }    public void draw(Graphics2D g2d) {
        // Save original graphics settings
        Composite originalComposite = g2d.getComposite();
        RenderingHints originalHints = g2d.getRenderingHints();
        
        // Enable anti-aliasing for smooth circles
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        // Draw in proper z-order: trails, orbs, impact effects, damage numbers
        drawTrailParticles(g2d);
        drawOrbs(g2d);
        drawImpactEffects(g2d);
        drawDamageNumbers(g2d);
        
        // Restore original graphics settings
        g2d.setComposite(originalComposite);
        g2d.setRenderingHints(originalHints);
    }
    
    private void drawTrailParticles(Graphics2D g2d) {
        for (OrbTrailParticle particle : trailParticles) {
            particle.draw(g2d);
        }
    }
    
    private void drawOrbs(Graphics2D g2d) {
        for (PhotonOrb orb : orbs) {
            orb.draw(g2d);
        }
    }
    
    private void drawImpactEffects(Graphics2D g2d) {
        for (ImpactEffect effect : impactEffects) {
            effect.draw(g2d);
        }
    }
    
    private void drawDamageNumbers(Graphics2D g2d) {
        for (DamageNumber number : damageNumbers) {
            number.draw(g2d);
        }
    }
    
    /**
     * The main PhotonOrb projectile class
     */
    private class PhotonOrb {
        private float x, y;
        private float vx, vy;
        private float speed;
        private float rotation = 0;
        private float pulsePhase = 0;
        private Enemy target;
        private boolean isDead = false;
        private float orbSize;
        private float glowSize;
          public PhotonOrb(double x, double y, Enemy target) {
            this.x = (float) x;
            this.y = (float) y;
            this.target = target;
            this.speed = ORB_SPEED;
            this.orbSize = ORB_RADIUS * (0.8f + (RNG.nextFloat() * 0.4f));
            this.glowSize = orbSize * 2.5f;
            
            // Initialize path phases with random values for more varied motion
            this.pathPhase = RNG.nextFloat() * (float)Math.PI * 2;
            this.orbitAmplitude = 0.7f + RNG.nextFloat() * 0.6f; // Randomize orbit intensity
            this.orbitSpeed = 1.5f + RNG.nextFloat() * 1.0f; // Randomize orbit speed
            this.sineFrequency = 3.0f + RNG.nextFloat() * 2.0f; // Randomize sine frequency
            
            // Initial velocity toward target
            updateVelocityTowardTarget();
        }
          // Enhanced pathfinding variables for more advanced curves
        private float pathPhase = 0;
        private float orbitRadius = 60f; // Maximum orbit radius
        private float orbitAmplitude = 1.0f; // How pronounced the orbit is
        private float orbitSpeed = 2.0f; // How fast the orbit cycles
        private float sineAmplitude = 30f; // Amplitude of sine wave path
        private float sineFrequency = 4.0f; // Frequency of sine wave
        
        public void update(float dt, List<Enemy> enemies) {
            // Update animation phases
            pulsePhase = (pulsePhase + dt * 5f) % (float)(Math.PI * 2);
            pathPhase = (pathPhase + dt * orbitSpeed) % (float)(Math.PI * 2);
            rotation += dt * 140f; // Rotate orb
            
            // If target is dead, find a new one
            if (target == null || target.isDead()) {
                target = findClosestEnemy(enemies);
                if (target == null) {
                    // No targets, just continue on current trajectory with more elaborate curve
                    float curveAngle = dt * 1.5f;
                    float oldVx = vx;
                    float oldVy = vy;
                    
                    // Create a more interesting spiral pattern when no target
                    vx = (float)(oldVx * Math.cos(curveAngle) - oldVy * Math.sin(curveAngle));
                    vy = (float)(oldVx * Math.sin(curveAngle) + oldVy * Math.cos(curveAngle));
                    
                    // Add slight pulsing to the speed
                    float pulseMultiplier = 1f + 0.2f * (float)Math.sin(pathPhase * 2);
                    float currentSpeed = (float)Math.sqrt(vx * vx + vy * vy);
                    if (currentSpeed > 0.1f) {
                        vx = vx / currentSpeed * speed * pulseMultiplier;
                        vy = vy / currentSpeed * speed * pulseMultiplier;
                    }
                }
            }
            
            // Advanced path-finding with target
            if (target != null) {
                // Calculate direct path to target
                float targetX = (float) target.getCenterX();
                float targetY = (float) target.getCenterY();
                float dx = targetX - x;
                float dy = targetY - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance < 0.1f) {
                    // Prevent division by zero
                    dx = 0.1f;
                    distance = 0.1f;
                }
                
                // Calculate normalized direct vector to target
                float dirX = dx / distance;
                float dirY = dy / distance;
                
                // Calculate perpendicular vector for orbital motion
                float perpX = -dirY;
                float perpY = dirX;
                
                // Calculate target approach factor - gets more direct as orb gets closer
                float approachFactor = Math.min(1.0f, distance / 400f); 
                
                // Adjust orbit amplitude based on distance (smaller orbit as we get closer)
                float currentOrbitRadius = orbitRadius * approachFactor;
                
                // Calculate orbital component
                float orbitX = perpX * currentOrbitRadius * (float)Math.sin(pathPhase) * orbitAmplitude;
                float orbitY = perpY * currentOrbitRadius * (float)Math.sin(pathPhase) * orbitAmplitude;
                
                // Add sine wave component for extra curvature
                // Create a vector perpendicular to the orbit motion
                float sineX = perpX * (float)Math.cos(pathPhase * sineFrequency) * sineAmplitude * approachFactor;
                float sineY = perpY * (float)Math.cos(pathPhase * sineFrequency) * sineAmplitude * approachFactor;
                
                // Combine direct vector with orbital and sine components
                // Use more direct path as we get closer to target
                float directFactor = 1.0f - approachFactor * 0.8f; // More direct as approachFactor decreases
                float orbitalFactor = approachFactor;
                
                // Apply steering forces
                float steeringX = dirX * directFactor * HOMING_STRENGTH + 
                                (orbitX + sineX) * orbitalFactor;
                float steeringY = dirY * directFactor * HOMING_STRENGTH + 
                                (orbitY + sineY) * orbitalFactor;
                
                // Apply steering to velocity
                vx += steeringX * dt * 60;
                vy += steeringY * dt * 60;
                
                // Normalize and apply speed
                float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
                if (currentSpeed > 0.1f) {
                    vx = vx / currentSpeed * speed;
                    vy = vy / currentSpeed * speed;
                }
                
                // Increase speed over time, faster as we get closer to target
                float accelerationFactor = 1.0f + (1.0f - approachFactor) * 0.5f;
                speed = Math.min(MAX_ORB_SPEED, speed + ORB_ACCELERATION * dt * accelerationFactor);
                
                // Check for collision with target
                if (distance < orbSize + target.getRadius()) {
                    // Hit detected!
                    int damage = BASE_DAMAGE + (getLevel() - 1) * DAMAGE_PER_LEVEL;
                    target.takeDamage(damage);
                    
                    // Create impact effect
                    impactEffects.add(new ImpactEffect(x, y));
                    
                    // Create damage number
                    damageNumbers.add(new DamageNumber(
                        (int) target.getCenterX(), 
                        (int) target.getCenterY() - 20,
                        damage
                    ));
                    
                    // Create particle burst
                    createImpactParticles(x, y);
                    
                    isDead = true;
                    return;
                }
            } else {
                // No target, but still try to refresh its trajectory periodically
                updateVelocityTowardTarget();
            }
            
            // Move orb
            x += vx * dt * 60;
            y += vy * dt * 60;
            
            // Check if orb is far off-screen
            if (x < -100 || x > GamePanel.GAME_WIDTH + 100 || 
                y < -100 || y > GamePanel.GAME_HEIGHT + 100) {
                isDead = true;
            }
        }
        
        private void updateVelocityTowardTarget() {
            if (target != null) {
                float dx = (float) target.getCenterX() - x;
                float dy = (float) target.getCenterY() - y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance > 0.1f) {
                    // Add more pronounced sweeping motion
                    float orbitalFactor = Math.min(1.0f, distance / 300f);
                    float offsetX = (float) Math.sin(pathPhase * 1.5f) * orbitalFactor;
                    float offsetY = (float) Math.cos(pathPhase * 2.0f) * orbitalFactor;
                    
                    // Normalize direction
                    float dirX = dx / distance;
                    float dirY = dy / distance;
                    
                    // Combine direct path with orbital motion
                    vx = (dirX + offsetX * 1.8f) * speed;
                    vy = (dirY + offsetY * 1.8f) * speed;
                    
                    // Normalize again to ensure consistent speed
                    float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);
                    if (currentSpeed > 0.1f) {
                        vx = vx / currentSpeed * speed;
                        vy = vy / currentSpeed * speed;
                    }
                }
            }
        }
        
        public boolean isDead() {
            return isDead;
        }
        
        public void draw(Graphics2D g2d) {
            // Glow effect with pulse
            float pulseScale = 1f + 0.2f * (float) Math.sin(pulsePhase);
            float currentGlowSize = glowSize * pulseScale;
            
            // Draw outer glow
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.6f));
            g2d.setPaint(new RadialGradientPaint(
                new Point2D.Float(x, y),
                currentGlowSize,
                new float[] { 0f, 0.5f, 1.0f },
                new Color[] {
                    new Color(ORB_GLOW_COLOR.getRed(), 
                             ORB_GLOW_COLOR.getGreen(), 
                             ORB_GLOW_COLOR.getBlue(), 180),
                    new Color(ORB_GLOW_COLOR.getRed(), 
                             ORB_GLOW_COLOR.getGreen(), 
                             ORB_GLOW_COLOR.getBlue(), 100),
                    new Color(ORB_GLOW_COLOR.getRed(), 
                             ORB_GLOW_COLOR.getGreen(), 
                             ORB_GLOW_COLOR.getBlue(), 0)
                }
            ));
            g2d.fill(new Ellipse2D.Float(x - currentGlowSize, y - currentGlowSize, 
                                       currentGlowSize * 2, currentGlowSize * 2));
            
            // Draw inner core with rotation effect
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.9f));
            
            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            
            // Inner bright core
            g2d.setColor(ORB_CORE_COLOR);
            g2d.fill(new Ellipse2D.Float(-orbSize, -orbSize, orbSize * 2, orbSize * 2));
            
            // Energy swirl pattern
            g2d.setColor(Color.WHITE);
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
            g2d.setStroke(new BasicStroke(1.5f));
            
            // Draw swirling energy patterns
            for (int i = 0; i < 2; i++) {
                double spiralPhase = pulsePhase + Math.PI * i;
                float spiralX = (float) Math.cos(spiralPhase) * orbSize * 0.7f;
                float spiralY = (float) Math.sin(spiralPhase) * orbSize * 0.7f;
                g2d.drawOval((int)(-orbSize/2 + spiralX/2), (int)(-orbSize/2 + spiralY/2), 
                            (int)orbSize, (int)orbSize);
            }
            
            // Restore transform
            g2d.setTransform(oldTransform);
        }
    }
    
    /**
     * Trail particle effects for PhotonOrbs
     */
    private class OrbTrailParticle {
        enum Type {
            TRAIL, SPAWN, IMPACT
        }
        
        private float x, y;
        private float vx, vy;
        private int life, maxLife;
        private Type type;
        private float size;
        private Color color;
        
        public OrbTrailParticle(float x, float y, float vx, float vy, int life, Type type) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = this.maxLife = life;
            this.type = type;
            
            // Size varies by type
            switch (type) {
                case SPAWN:
                    this.size = 2f + RNG.nextFloat() * 2f;
                    this.color = new Color(200, 230, 255);
                    break;
                case IMPACT:
                    this.size = 2.5f + RNG.nextFloat() * 3f;
                    this.color = IMPACT_FLASH_COLOR;
                    break;
                case TRAIL:
                default:
                    this.size = 1f + RNG.nextFloat() * 2f;
                    this.color = ORB_TRAIL_COLOR;
                    break;
            }
        }
        
        public void update(float dt) {
            x += vx * dt * 60;
            y += vy * dt * 60;
            
            // Apply drag based on type
            switch (type) {
                case SPAWN:
                    vx *= 0.95f;
                    vy *= 0.95f;
                    break;
                case IMPACT:
                    vx *= 0.92f;
                    vy *= 0.92f;
                    break;
                case TRAIL:
                default:
                    vx *= 0.9f;
                    vy *= 0.9f;
                    break;
            }
            
            life--;
        }
        
        public boolean isDead() {
            return life <= 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = (float) life / maxLife;
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.7f));
            g2d.setColor(color);
            
            // Draw particle
            float drawSize = size * (type == Type.IMPACT ? 
                                    (1f + 0.5f * (1f - alpha)) : 
                                    (0.5f + 0.5f * alpha));
            g2d.fill(new Ellipse2D.Float(x - drawSize, y - drawSize, drawSize * 2, drawSize * 2));
        }
    }
    
    /**
     * Impact effect when a photon orb hits an enemy
     */
    private class ImpactEffect {
        private float x, y;
        private int life = 20;
        private float size = 1f;
        private float maxSize = 25f;
        private float rotation = 0;
        
        public ImpactEffect(float x, float y) {
            this.x = x;
            this.y = y;
            this.rotation = RNG.nextFloat() * 360f;
        }
        
        public void update(float dt) {
            life--;
            rotation += dt * 360f; // Rotate quickly
            
            // Expand and then contract
            float progress = 1f - (life / 20f);
            if (progress < 0.5f) {
                // Expand phase
                size = maxSize * (progress / 0.5f);
            } else {
                // Contract phase
                size = maxSize * (1f - ((progress - 0.5f) / 0.5f));
            }
        }
        
        public boolean isDead() {
            return life <= 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = life / 20f;
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.8f));
            
            // Save current transform
            AffineTransform oldTransform = g2d.getTransform();
            
            // Apply transform for rotation
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            
            // Draw expanding/contracting burst
            g2d.setPaint(new RadialGradientPaint(
                new Point2D.Float(0, 0),
                size,
                new float[] { 0f, 0.5f, 1.0f },
                new Color[] {
                    IMPACT_FLASH_COLOR,
                    new Color(IMPACT_FLASH_COLOR.getRed(), 
                             IMPACT_FLASH_COLOR.getGreen(), 
                             IMPACT_FLASH_COLOR.getBlue(), 160),
                    new Color(IMPACT_FLASH_COLOR.getRed(), 
                             IMPACT_FLASH_COLOR.getGreen(), 
                             IMPACT_FLASH_COLOR.getBlue(), 0)
                }
            ));
            g2d.fill(new Ellipse2D.Float(-size, -size, size * 2, size * 2));
            
            // Draw cross-shaped rays
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2f));
            float rayLength = size * 0.8f;
            
            for (int i = 0; i < 4; i++) {
                double rayAngle = Math.PI / 2 * i;
                float rayX = (float) Math.cos(rayAngle) * rayLength;
                float rayY = (float) Math.sin(rayAngle) * rayLength;
                g2d.drawLine(0, 0, (int)rayX, (int)rayY);
            }
            
            // Restore transform
            g2d.setTransform(oldTransform);
        }
    }
    
    /**
     * Creates a burst of particles when an orb impacts an enemy
     */
    private void createImpactParticles(float x, float y) {
        int numParticles = 12 + RNG.nextInt(8);
        
        for (int i = 0; i < numParticles; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            float speed = 2f + RNG.nextFloat() * 3f;
            
            trailParticles.add(new OrbTrailParticle(
                x, y,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed,
                15 + RNG.nextInt(10),
                OrbTrailParticle.Type.IMPACT
            ));
        }
    }
    
    /**
     * Displays damage numbers when an enemy is hit
     */
    private class DamageNumber {
        private float x, y;
        private int damage;
        private int life = 40;
        private float initialY;
        
        public DamageNumber(int x, int y, int damage) {
            this.x = x;
            this.y = this.initialY = y;
            this.damage = damage;
        }
        
        public void update(float dt) {
            float progress = 1f - (life / 40f);
            y = initialY - 30f * progress; // Float upward
            life--;
        }
        
        public boolean isDead() {
            return life <= 0;
        }
        
        public void draw(Graphics2D g2d) {
            float alpha = life > 30 ? 1f : life / 30f;
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha));
            
            String text = String.valueOf(damage);
            Font originalFont = g2d.getFont();
            Font damageFont = originalFont.deriveFont(Font.BOLD, 14f);
            g2d.setFont(damageFont);
            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            
            // Draw shadow
            g2d.setColor(new Color(0, 50, 100, 160));
            g2d.drawString(text, x - textWidth/2 + 1, y + 1);
            
            // Draw text
            g2d.setColor(DAMAGE_TEXT_COLOR);
            g2d.drawString(text, x - textWidth/2, y);
            
            // Restore original font
            g2d.setFont(originalFont);
        }
    }
}
