import java.awt.*;
import java.awt.geom.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhotonOrbs extends Skill {    // Core mechanics
    private static final int BASE_DAMAGE = 5;
    private static final int DAMAGE_PER_LEVEL = 2;
    private static final float ORB_RADIUS = 8f;
    private static final float ORB_SPEED = 7f;
    private static final float MAX_ORB_SPEED = 12f;
    private static final float ORB_ACCELERATION = 0.3f;
    private static final float HOMING_STRENGTH = 0.8f; // Increased homing strength for more accurate targeting
    private static final long ORB_SPAWN_COOLDOWN_MS = 500; // 2 orbs per second
    // Each orb can hit exactly one enemy before vanishing
    
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
        private boolean isDead = false;        private final float orbSize;
        private final float glowSize;public PhotonOrb(double x, double y, Enemy target) {
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
            
            // Initialize advanced motion variables with randomized values for unique movement
            this.spiralFactor = 0.2f + RNG.nextFloat() * 0.3f;
            this.spiralFrequency = 0.4f + RNG.nextFloat() * 0.4f;
            this.wobblePhase = RNG.nextFloat() * (float)Math.PI * 2;
            this.wobbleSpeed = 2.5f + RNG.nextFloat() * 1.5f;
            this.wobbleAmplitude = 10f + RNG.nextFloat() * 10f;
            
            // Initial velocity toward target
            updateVelocityTowardTarget();
        }        // Enhanced pathfinding variables for more advanced curves
        private float pathPhase = 0;
        private final float orbitRadius = 60f; // Maximum orbit radius
        private float orbitAmplitude = 1.0f; // How pronounced the orbit is
        private float orbitSpeed = 2.0f; // How fast the orbit cycles
        private final float sineAmplitude = 30f; // Amplitude of sine wave path
        private float sineFrequency = 4.0f; // Frequency of sine wave
          // Advanced path variables for more intricate movement
        private float spiralFactor = 0.3f; // How much the orb spirals
        private float spiralFrequency = 0.5f; // Speed of spiral motion
        private float wobblePhase = 0f; // Phase for wobbling motion
        private float wobbleSpeed = 3.0f; // Speed of wobble
        private float wobbleAmplitude = 15f; // Intensity of wobble
        private float convergenceFactor = 0f; // Increases as orb gets closer to target
        
        public void update(float dt, List<Enemy> enemies) {            // Update animation phases
            pulsePhase = (pulsePhase + dt * 5f) % (float)(Math.PI * 2);
            pathPhase = (pathPhase + dt * orbitSpeed) % (float)(Math.PI * 2);
            wobblePhase = (wobblePhase + dt * wobbleSpeed) % (float)(Math.PI * 2);
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
                convergenceFactor = 1.0f - approachFactor; // Increases as we get closer to target
                
                // Adjust orbit amplitude based on distance (smaller orbit as we get closer)
                float currentOrbitRadius = orbitRadius * approachFactor;
                
                // Calculate orbital component
                float orbitX = perpX * currentOrbitRadius * (float)Math.sin(pathPhase) * orbitAmplitude;
                float orbitY = perpY * currentOrbitRadius * (float)Math.sin(pathPhase) * orbitAmplitude;
                
                // Add sine wave component for extra curvature
                float sineX = perpX * (float)Math.cos(pathPhase * sineFrequency) * sineAmplitude * approachFactor;
                float sineY = perpY * (float)Math.cos(pathPhase * sineFrequency) * sineAmplitude * approachFactor;
                  // Add spiral component - spiral tightens as we get closer to enemy
                float adjustedSpiralFactor = this.spiralFactor * (1.0f - convergenceFactor * 0.7f);
                float spiralX = perpX * (float)Math.sin(pathPhase * spiralFrequency) * distance * adjustedSpiralFactor;
                float spiralY = perpY * (float)Math.cos(pathPhase * spiralFrequency) * distance * adjustedSpiralFactor;
                
                // Add wobble effect - more pronounced when further away
                float wobbleX = perpX * (float)Math.sin(wobblePhase) * wobbleAmplitude * approachFactor;
                float wobbleY = perpY * (float)Math.cos(wobblePhase) * wobbleAmplitude * approachFactor;                // Combine direct vector with orbital and sine components
                // Use more direct path as we get closer to target
                float directFactor = 1.0f - approachFactor * 0.7f; // More direct steering (0.7f instead of 0.8f)
                float orbitalFactor = approachFactor * 0.8f; // Reduced orbital factor for more accuracy
                  // When very close to the target, go almost straight for it
                if (distance < 60f) {
                    directFactor = 0.95f; // 95% direct path when very close
                    orbitalFactor *= 0.2f; // Greatly reduce orbital movement when close
                } else if (distance < 120f) {
                    directFactor = 0.9f; // 90% direct path when moderately close
                    orbitalFactor *= 0.3f; // Significantly reduce orbital movement
                }
                
                // Apply steering forces, combining all motion components
                float steeringX = dirX * directFactor * HOMING_STRENGTH + 
                                (orbitX + sineX + spiralX + wobbleX) * orbitalFactor;
                float steeringY = dirY * directFactor * HOMING_STRENGTH + 
                                (orbitY + sineY + spiralY + wobbleY) * orbitalFactor;
                
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
                speed = Math.min(MAX_ORB_SPEED, speed + ORB_ACCELERATION * dt * accelerationFactor);                // Check for collision with target - increased collision radius for more reliable hits
                float collisionRadius = orbSize + target.getRadius() + 2f; // Added a small buffer for more forgiving collision
                if (distance < collisionRadius) {
                    // Hit detected!
                    int damage = BASE_DAMAGE + (getLevel() - 1) * DAMAGE_PER_LEVEL;
                    target.takeDamage(damage);
                      // Create impact effect at the exact point of contact
                    float contactX = x + dirX * orbSize; // Move to edge of orb in target direction
                    float contactY = y + dirY * orbSize;
                    impactEffects.add(new ImpactEffect(contactX, contactY));
                    
                    // Create damage number
                    damageNumbers.add(new DamageNumber(
                        (int) target.getCenterX(), 
                        (int) target.getCenterY() - 20,
                        damage
                    ));                    // Create particle burst
                    createImpactParticles(contactX, contactY);
                    
                    // Always set isDead to true after hitting - orbs only hit once
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
                
                if (distance > 0.1f) {                    // Calculate initial prediction point where enemy might be
                    // For better prediction, consider enemy speed and distance
                    float targetVelX = target.getXVelocity();
                    float targetVelY = target.getYVelocity();
                    
                    // Calculate enemy speed for adaptive prediction
                    float enemySpeed = (float)Math.sqrt(targetVelX * targetVelX + targetVelY * targetVelY);
                    
                    // Adaptive prediction factor: higher for faster enemies
                    // Base time plus additional time scaling with enemy speed
                    float predictionFactor = Math.min(1.5f, 0.5f + (enemySpeed / (speed * 2.0f)));
                    float timeToReach = distance / speed * predictionFactor;
                    
                    // Predict where enemy will be
                    float predictedX = (float)target.getCenterX() + targetVelX * timeToReach;
                    float predictedY = (float)target.getCenterY() + targetVelY * timeToReach;
                    
                    // Recalculate direction based on prediction
                    float predDx = predictedX - x;
                    float predDy = predictedY - y;
                    float predDistance = (float) Math.sqrt(predDx * predDx + predDy * predDy);
                    
                    // Add smaller sweeping motion
                    float orbitalFactor = Math.min(0.7f, distance / 400f); // Reduced orbital factor
                    float offsetX = (float) Math.sin(pathPhase * 1.5f) * orbitalFactor;
                    float offsetY = (float) Math.cos(pathPhase * 2.0f) * orbitalFactor;
                    
                    // Normalize direction using predicted position
                    float dirX = predDistance > 0.1f ? predDx / predDistance : dx / distance;
                    float dirY = predDistance > 0.1f ? predDy / predDistance : dy / distance;
                      // Combine direct path with orbital motion, with stronger emphasis on direct path
                    // Near targets, almost eliminate the orbital component for a straighter path
                    float directness = distance < 100f ? 0.95f : 0.9f;
                    float orbitalInfluence = distance < 100f ? 0.5f : 0.9f;
                    
                    vx = (dirX * directness + offsetX * orbitalInfluence) * speed;
                    vy = (dirY * directness + offsetY * orbitalInfluence) * speed;
                    
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
            // Calculate various animation factors based on phases
            float pulseScale = 1f + 0.2f * (float) Math.sin(pulsePhase);
            float wobbleScale = 1f + 0.1f * (float) Math.sin(wobblePhase * 1.7f);
            float currentGlowSize = glowSize * pulseScale * wobbleScale;
            
            // Enhanced color based on convergence (gets brighter as it approaches target)
            Color glowColor = ORB_GLOW_COLOR;
            if (convergenceFactor > 0.5f) {
                float intensity = Math.min(1.0f, (convergenceFactor - 0.5f) * 2f);
                glowColor = new Color(
                    Math.min(255, (int)(glowColor.getRed() + (255 - glowColor.getRed()) * intensity * 0.7f)),
                    Math.min(255, (int)(glowColor.getGreen() + (255 - glowColor.getGreen()) * intensity * 0.3f)),
                    Math.min(255, (int)(glowColor.getBlue() + (255 - glowColor.getBlue()) * intensity * 0.1f)),
                    glowColor.getAlpha()
                );
            }
            
            // Draw outer glow with enhanced opacity based on speed
            float speedFactor = Math.min(1.0f, speed / MAX_ORB_SPEED);
            float glowOpacity = 0.6f + speedFactor * 0.3f;
            g2d.setComposite(AlphaComposite.SrcOver.derive(glowOpacity));
            
            g2d.setPaint(new RadialGradientPaint(
                new Point2D.Float(x, y),
                currentGlowSize,
                new float[] { 0f, 0.5f, 1.0f },
                new Color[] {
                    new Color(glowColor.getRed(), 
                             glowColor.getGreen(), 
                             glowColor.getBlue(), 180),
                    new Color(glowColor.getRed(), 
                             glowColor.getGreen(), 
                             glowColor.getBlue(), 100),
                    new Color(glowColor.getRed(), 
                             glowColor.getGreen(), 
                             glowColor.getBlue(), 0)
                }
            ));
            g2d.fill(new Ellipse2D.Float(x - currentGlowSize, y - currentGlowSize, 
                                       currentGlowSize * 2, currentGlowSize * 2));
            
            // Draw inner core with rotation effect
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.9f));
            
            AffineTransform oldTransform = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(rotation));
            
            // Inner bright core with enhanced glow based on convergence
            Color coreColor = ORB_CORE_COLOR;
            if (convergenceFactor > 0.7f) {
                float intensity = Math.min(1.0f, (convergenceFactor - 0.7f) * 3.3f);
                coreColor = new Color(
                    Math.min(255, (int)(coreColor.getRed() + (255 - coreColor.getRed()) * intensity)),
                    Math.min(255, (int)(coreColor.getGreen() + (255 - coreColor.getGreen()) * intensity)),
                    Math.min(255, (int)(coreColor.getBlue() + (255 - coreColor.getBlue()) * intensity * 0.5f)),
                    coreColor.getAlpha()
                );
            }
            g2d.setColor(coreColor);
            g2d.fill(new Ellipse2D.Float(-orbSize, -orbSize, orbSize * 2, orbSize * 2));
            
            // Dynamic inner bright spot
            float brightSpotSize = orbSize * 0.7f * pulseScale;
            g2d.setColor(new Color(255, 255, 255, 180));
            g2d.fill(new Ellipse2D.Float(-brightSpotSize/2, -brightSpotSize/2, brightSpotSize, brightSpotSize));
            
            // Energy swirl patterns - more complex with multiple layers
            g2d.setColor(Color.WHITE);
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.7f));
            g2d.setStroke(new BasicStroke(1.5f));
            
            // Draw primary swirling energy patterns
            for (int i = 0; i < 2; i++) {
                double spiralPhase = pulsePhase + Math.PI * i;
                float spiralX = (float) Math.cos(spiralPhase) * orbSize * 0.7f;
                float spiralY = (float) Math.sin(spiralPhase) * orbSize * 0.7f;
                g2d.drawOval((int)(-orbSize/2 + spiralX/2), (int)(-orbSize/2 + spiralY/2), 
                            (int)orbSize, (int)orbSize);
            }
            
            // Draw additional energy arcs for visual interest
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            for (int i = 0; i < 3; i++) {
                double arcPhase = pulsePhase * 1.5f + (Math.PI * 2/3) * i;
                float arcSize = orbSize * 1.3f;
                g2d.drawArc((int)(-arcSize/2), (int)(-arcSize/2), 
                           (int)arcSize, (int)arcSize,
                           (int)(Math.toDegrees(arcPhase)), 120);
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
        
        public void setSize(float size) {
            this.size = size;
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
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
        private final float x, y;
        private int life = 20;
        private float size = 1f;
        private final float maxSize = 25f;
        private float rotation = 0;
        private final float pulseSpeed;
        private final float rayLength;
        private final int numRays;
        private final Color impactColor;
        
        public ImpactEffect(float x, float y) {
            this.x = x;
            this.y = y;
            this.rotation = RNG.nextFloat() * 360f;
            this.pulseSpeed = 0.8f + RNG.nextFloat() * 0.4f;
            this.rayLength = 20f + RNG.nextFloat() * 15f;
            this.numRays = 4 + RNG.nextInt(4);  // 4-7 rays
            
            // Random tint for variety
            float blueShift = 0.7f + RNG.nextFloat() * 0.3f;
            this.impactColor = new Color(
                Math.min(255, (int)(IMPACT_FLASH_COLOR.getRed() * (1.0f - blueShift * 0.3f))),
                Math.min(255, (int)(IMPACT_FLASH_COLOR.getGreen() * (1.0f - blueShift * 0.1f))),
                Math.min(255, (int)(IMPACT_FLASH_COLOR.getBlue())),
                IMPACT_FLASH_COLOR.getAlpha()
            );
        }
          public void update(float dt) {
            life--;
            rotation += dt * 360f * pulseSpeed; // Rotate quickly, speed varies by instance
            
            // Expand and then contract with easing functions for smoother animation
            float progress = 1f - (life / 20f);
            float easedProgress;
            
            if (progress < 0.5f) {
                // Expand phase with ease-out (quadratic)
                easedProgress = progress * progress * 4; // Quadratic easing
                size = maxSize * (easedProgress);
            } else {
                // Contract phase with ease-in (quadratic)
                float contractProgress = (progress - 0.5f) / 0.5f;
                easedProgress = 1 - (1 - contractProgress) * (1 - contractProgress);
                size = maxSize * (1f - easedProgress);
            }
            
            // Apply slight oscillation to the size for more organic feel
            float oscillation = (float)Math.sin(progress * 20) * 0.05f;
            size *= (1 + oscillation);
        }
        
        public boolean isDead() {
            return life <= 0;
        }
          public void draw(Graphics2D g2d) {
            float alpha = life / 20f;
            float pulsePhase = (1f - alpha) * pulseSpeed * 10f;
            float pulseEffect = 1f + 0.2f * (float)Math.sin(pulsePhase * Math.PI);
            
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
                    impactColor,
                    new Color(impactColor.getRed(), 
                             impactColor.getGreen(), 
                             impactColor.getBlue(), 160),
                    new Color(impactColor.getRed(), 
                             impactColor.getGreen(), 
                             impactColor.getBlue(), 0)
                }
            ));
            g2d.fill(new Ellipse2D.Float(-size, -size, size * 2, size * 2));
            
            // Draw inner burst
            float innerSize = size * 0.6f * pulseEffect;
            g2d.setColor(new Color(255, 255, 255, (int)(200 * alpha)));
            g2d.fill(new Ellipse2D.Float(-innerSize, -innerSize, innerSize * 2, innerSize * 2));
              // Draw dynamic rays with varied lengths and enhanced visual design
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(1.5f));
            float currentRayLength = rayLength * size / maxSize * pulseEffect;
            
            // Main ray burst
            for (int i = 0; i < numRays; i++) {
                double rayAngle = (Math.PI * 2 / numRays) * i + pulsePhase;
                float rayX = (float) Math.cos(rayAngle) * currentRayLength;
                float rayY = (float) Math.sin(rayAngle) * currentRayLength;
                g2d.drawLine(0, 0, (int)rayX, (int)rayY);
                
                // Add secondary ray details for some rays
                if (i % 2 == 0) {
                    float secondaryLength = currentRayLength * 0.6f;
                    float offsetAngle = (float)(rayAngle + Math.PI/12);
                    float secRayX = (float) Math.cos(offsetAngle) * secondaryLength;
                    float secRayY = (float) Math.sin(offsetAngle) * secondaryLength;
                    g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.5f));
                    g2d.drawLine(0, 0, (int)secRayX, (int)secRayY);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.8f));
                }
                
                // Add tiny dot accents at the end of some rays
                if (i % 3 == 0) {
                    float dotSize = 1.5f + (float)Math.sin(pulsePhase * 2.5f);
                    float dotX = (float) Math.cos(rayAngle) * (currentRayLength * 0.85f);
                    float dotY = (float) Math.sin(rayAngle) * (currentRayLength * 0.85f);
                    g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.9f));
                    g2d.fill(new Ellipse2D.Float(dotX - dotSize/2, dotY - dotSize/2, dotSize, dotSize));
                }
            }
            
            // Add animated concentric rings
            g2d.setStroke(new BasicStroke(0.8f));
            g2d.setComposite(AlphaComposite.SrcOver.derive(alpha * 0.3f));
            float ringPhase = pulsePhase * 3.0f;
            float ringSize = size * 0.7f * (0.6f + 0.4f * (float)Math.sin(ringPhase));
            g2d.drawOval((int)(-ringSize), (int)(-ringSize), (int)(ringSize * 2), (int)(ringSize * 2));
            
            // Restore transform
            g2d.setTransform(oldTransform);
        }
    }
      /**
     * Creates a burst of particles when an orb impacts an enemy
     */    private void createImpactParticles(float x, float y) {
        int numParticles = 24 + RNG.nextInt(16); // Further increased particle count for more impressive effect
        
        // Create primary burst particles with improved spread and speed variation
        for (int i = 0; i < numParticles; i++) {
            // Create particles in a circular burst pattern
            double angle = RNG.nextDouble() * Math.PI * 2;
            // Vary speed based on particle position in the sequence for a more natural burst
            float speedVariation = (i % 3 == 0) ? 1.5f : 1.0f;
            float speed = (2f + RNG.nextFloat() * 4.5f) * speedVariation;
            
            // Add slight angle clusters for more interesting burst patterns
            float angleOffset = 0;
            if (i % 4 == 0) {
                angleOffset = (float)(Math.PI * 0.05f * (RNG.nextFloat() - 0.5f));
            }
            
            trailParticles.add(new OrbTrailParticle(
                x, y,
                (float) Math.cos(angle + angleOffset) * speed,
                (float) Math.sin(angle + angleOffset) * speed,
                15 + RNG.nextInt(12), // Slightly longer particle lifetime
                OrbTrailParticle.Type.IMPACT
            ));
        }
        
        // Create a few slower, larger particles for more dynamic effect
        for (int i = 0; i < 7; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            float speed = 0.8f + RNG.nextFloat() * 1.5f;
            
            OrbTrailParticle particle = new OrbTrailParticle(
                x, y,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed,
                25 + RNG.nextInt(15),
                OrbTrailParticle.Type.IMPACT
            );
            particle.setSize(3f + RNG.nextFloat() * 3f);
            trailParticles.add(particle);
        }
          // Create a variety of short-lived bright flash particles
        for (int i = 0; i < 8; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            float speed = 0.7f + RNG.nextFloat() * 1.4f;
            
            OrbTrailParticle particle = new OrbTrailParticle(
                x, y,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed,
                8 + RNG.nextInt(5),
                OrbTrailParticle.Type.IMPACT
            );
            particle.setSize(3.5f + RNG.nextFloat() * 2.5f);
            
            // Vary particle color for more interesting visual effect
            int brightness = 200 + RNG.nextInt(55); // Bright but with variation
            int alpha = 160 + RNG.nextInt(70);      // Semi-transparent with variation
            
            if (i % 3 == 0) {
                // Add some slight blue tint to some particles
                particle.setColor(new Color(brightness, brightness, 255, alpha));
            } else {
                particle.setColor(new Color(brightness, brightness, brightness, alpha));
            }
            
            trailParticles.add(particle);
        }
        
        // Create a central flash effect that fades quickly
        OrbTrailParticle centralFlash = new OrbTrailParticle(
            x, y,
            0f, 0f, // Stationary
            5 + RNG.nextInt(3),
            OrbTrailParticle.Type.IMPACT
        );
        centralFlash.setSize(6f + RNG.nextFloat() * 2f);
        centralFlash.setColor(new Color(255, 255, 255, 220));
        trailParticles.add(centralFlash);
    }
      /**
     * Displays damage numbers when an enemy is hit
     */
    private class DamageNumber {
        private final float x;
        private float y;
        private final int damage;
        private int life = 40;
        private final float initialY;
        
        public DamageNumber(int x, int y, int damage) {
            this.x = x;
            this.initialY = y;
            this.y = y;
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
