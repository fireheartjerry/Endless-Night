/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class operates the light lance skill with visuals and damage
*/

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class LightLance extends Skill {
    static final int BASE_DAMAGE = 5;
    static final int DAMAGE_PER_LEVEL = 2;
    static final float BEAM_LENGTH = 1500f;
    static final float BEAM_WIDTH = 30f;
    static final float HIT_WIDTH = 30f;
    static final long COOLDOWN_MS = 1500L;

    static final float CHARGE_TIME = 0.01f;
    static final float BEAM_DURATION = 0.3f;

    static final Color CORE_COLOR = new Color(255, 255, 255, 220);
    static final Color MAIN_COLOR = new Color(120, 180, 255, 180);
    static final Color GLOW_COLOR = new Color(100, 150, 255, 100);
    static final Color CHARGE_COLOR = new Color(200, 220, 255);

    final CopyOnWriteArrayList<BeamParticle> particles = new CopyOnWriteArrayList<>();
    final CopyOnWriteArrayList<ImpactEffect> impacts = new CopyOnWriteArrayList<>();

    boolean firing;
    boolean charging;
    float charge_progress;
    float beam_progress;
    Point mouse_position = new Point();
    float aim_angle;

    final Player owner;
    final Random rng = new Random();

    final Line2D.Float beam_line = new Line2D.Float();
    final List<Enemy> hit_enemies = new ArrayList<>();

    public LightLance(Player owner) {
        super("Light Lance", 1, (int) COOLDOWN_MS, false);
        this.owner = owner;
    }    public void update(float dt, List<Enemy> enemies) {
        if (isReady() && !charging && !firing) {
            updateAimAngle(); // Update aim angle before starting to charge
            charging = true;
            activate();
            spawnChargeParticles();
        }

        if (charging) {
            charge_progress += dt / CHARGE_TIME;
            if (charge_progress >= 1f) {
                // We don't update aim angle here anymore - use the angle captured at beginning of charge
                fireBeam(enemies);
                charging = false;
                charge_progress = 0f;
                beam_progress = 0f;
            }
        }

        if (firing) {
            beam_progress += dt / BEAM_DURATION;
            if (beam_progress >= 1f)
                firing = false;
        }

        updateParticles(dt);
        updateImpacts(dt);
    }    public void attemptActivate(Point mousePos) {
        mouse_position = mousePos;
        if (isReady() && !charging && !firing) {
            updateAimAngle();
            charging = true;
            activate();
            spawnChargeParticles();
        }
    }
    
    public void setMousePosition(Point mousePos) {
        mouse_position = mousePos;
    }
    
    void updateAimAngle() {
        double dx = mouse_position.x - owner.getCenterX();
        double dy = mouse_position.y - owner.getCenterY();
        aim_angle = (float) Math.atan2(dy, dx);
    }

    void fireBeam(List<Enemy> enemies) {
        firing = true;
        double sx = owner.getCenterX();
        double sy = owner.getCenterY();
        double ex = sx + BEAM_LENGTH * Math.cos(aim_angle);
        double ey = sy + BEAM_LENGTH * Math.sin(aim_angle);
        beam_line.setLine(sx, sy, ex, ey);
        // Variables removed since they were unused
        hit_enemies.clear();
        for (Enemy enemy : enemies) {
            Rectangle2D bounds = new Rectangle2D.Double(enemy.getX(), enemy.getY(), enemy.getWidth(),
                    enemy.getHeight());
            boolean hit = beam_line.intersects(bounds);
            if (!hit) {
                double cx = enemy.getCenterX() - sx;
                double cy = enemy.getCenterY() - sy;
                double proj = cx * Math.cos(aim_angle) + cy * Math.sin(aim_angle);
                if (proj > 0 && proj < BEAM_LENGTH) {
                    double proj_x = sx + proj * Math.cos(aim_angle);
                    double proj_y = sy + proj * Math.sin(aim_angle);
                    double dist = Math.hypot(enemy.getCenterX() - proj_x, enemy.getCenterY() - proj_y);
                    if (dist < HIT_WIDTH / 2)
                        hit = true;
                }
            }
            if (hit) {
                int dmg = BASE_DAMAGE + (getLevel() - 1) * DAMAGE_PER_LEVEL;
                enemy.takeDamage(dmg);
                hit_enemies.add(enemy);
                Point2D impact = getIntersectionPoint(beam_line, bounds);
                if (impact == null)
                    impact = new Point2D.Double(enemy.getCenterX(), enemy.getCenterY());
                impacts.add(new ImpactEffect(impact.getX(), impact.getY()));
                for (int i = 0; i < 2 + getLevel(); i++) {
                    impacts.add(new ImpactEffect(impact.getX() + rng.nextDouble() * 10 - 5,
                            impact.getY() + rng.nextDouble() * 10 - 5));
                }
            }
        }
        spawnBeamParticles();
    }

    Point2D getIntersectionPoint(Line2D line, Rectangle2D rect) {
        double x1 = rect.getX();
        double y1 = rect.getY();
        double x2 = x1 + rect.getWidth();
        double y2 = y1 + rect.getHeight();
        Line2D[] edges = {
                new Line2D.Double(x1, y1, x2, y1),
                new Line2D.Double(x2, y1, x2, y2),
                new Line2D.Double(x2, y2, x1, y2),
                new Line2D.Double(x1, y2, x1, y1)
        };
        for (Line2D edge : edges) {
            Point2D p = getIntersectionPoint(line, edge);
            if (p != null)
                return p;
        }
        return new Point2D.Double(rect.getCenterX(), rect.getCenterY());
    }

    Point2D getIntersectionPoint(Line2D a, Line2D b) {
        double x1 = a.getX1(), y1 = a.getY1();
        double x2 = a.getX2(), y2 = a.getY2();
        double x3 = b.getX1(), y3 = b.getY1();
        double x4 = b.getX2(), y4 = b.getY2();
        double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
        if (d == 0)
            return null;
        double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
        double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
        if (xi < Math.min(x1, x2) || xi > Math.max(x1, x2) || yi < Math.min(y1, y2) || yi > Math.max(y1, y2))
            return null;
        if (xi < Math.min(x3, x4) || xi > Math.max(x3, x4) || yi < Math.min(y3, y4) || yi > Math.max(y3, y4))
            return null;
        return new Point2D.Double(xi, yi);
    }

    void updateParticles(float dt) {
        for (BeamParticle p : new ArrayList<>(particles)) {
            p.update(dt);
            if (p.dead())
                particles.remove(p);
        }
    }

    void updateImpacts(float dt) {
        for (ImpactEffect imp : new ArrayList<>(impacts)) {
            imp.update(dt);
            if (imp.dead())
                impacts.remove(imp);
        }
    }

    void spawnChargeParticles() {
        int count = 15 + rng.nextInt(10);
        for (int i = 0; i < count; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double dist = 30 + rng.nextDouble() * 50;
            double px = owner.getCenterX() + Math.cos(angle) * dist;
            double py = owner.getCenterY() + Math.sin(angle) * dist;
            double vx = -Math.cos(angle) * (50 + rng.nextDouble() * 30);
            double vy = -Math.sin(angle) * (50 + rng.nextDouble() * 30);
            particles.add(new BeamParticle(px, py, vx, vy, BeamParticle.Type.CHARGE, 0.5f + rng.nextFloat() * 0.3f));
        }
    }

    void spawnBeamParticles() {
        double sx = owner.getCenterX();
        double sy = owner.getCenterY();
        double ex = sx + BEAM_LENGTH * Math.cos(aim_angle);
        double ey = sy + BEAM_LENGTH * Math.sin(aim_angle);
        int count = 40 + rng.nextInt(15);
        for (int i = 0; i < count; i++) {
            double t = rng.nextDouble();
            double px = sx + t * (ex - sx);
            double py = sy + t * (ey - sy);
            double offset = (rng.nextDouble() * 2 - 1) * BEAM_WIDTH * 0.7;
            double perp_x = Math.cos(aim_angle + Math.PI / 2);
            double perp_y = Math.sin(aim_angle + Math.PI / 2);
            px += perp_x * offset;
            py += perp_y * offset;
            double speed = 150 + rng.nextDouble() * 200;
            double vx = Math.cos(aim_angle) * speed;
            double vy = Math.sin(aim_angle) * speed;
            particles.add(new BeamParticle(px, py, vx, vy, BeamParticle.Type.BEAM, 0.3f + rng.nextFloat() * 0.3f));
        }
        int sparkle = 15 + rng.nextInt(10);
        for (int i = 0; i < sparkle; i++) {
            double t = rng.nextDouble();
            double px = sx + t * (ex - sx);
            double py = sy + t * (ey - sy);
            double perp_angle = aim_angle + Math.PI / 2 + (rng.nextDouble() - 0.5) * Math.PI / 3;
            double speed = 50 + rng.nextDouble() * 100;
            double vx = Math.cos(perp_angle) * speed;
            double vy = Math.sin(perp_angle) * speed;
            particles.add(new BeamParticle(px, py, vx, vy, BeamParticle.Type.BEAM, 0.2f + rng.nextFloat() * 0.2f));
        }
        for (int i = 0; i < 8; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            double speed = 20 + rng.nextDouble() * 40;
            particles.add(new BeamParticle(sx, sy, Math.cos(angle) * speed, Math.sin(angle) * speed,
                    BeamParticle.Type.BEAM, 0.4f + rng.nextFloat() * 0.3f));
        }
    }

    public void draw(Graphics2D g) {
        if (firing)
            drawBeam(g);
        for (BeamParticle p : particles)
            if (p.type == BeamParticle.Type.BEAM)
                p.draw(g);
        for (ImpactEffect imp : impacts)
            imp.draw(g);
    }

    public void drawBeam(Graphics2D g) {        
        double cx = owner.getCenterX();
        double cy = owner.getCenterY();
        Graphics2D b = (Graphics2D) g.create();
        float fade = Math.max(0f, 1f - beam_progress);
        double ex = cx + BEAM_LENGTH * Math.cos(aim_angle);
        double ey = cy + BEAM_LENGTH * Math.sin(aim_angle);
        float shine = (float) Math.sin(System.nanoTime() / 1.0e8) * 0.1f + 0.9f;
        float pulse = (float) Math.sin(System.nanoTime() / 3.0e7) * 0.15f + 0.85f; // Even faster pulse effect

        // Draw more echo trails for enhanced sci-fi effect
        for (int i = 1; i <= 5; i++) { // Increased from 3 to 5 trails
            float trailFade = fade * (1.0f - (i * 0.15f));
            float trailAlpha = 0.1f * trailFade * (1.0f - (i * 0.15f));
            float trailWidth = BEAM_WIDTH * (4.0f - i * 0.4f) * fade;

            double offset = Math.sin(System.nanoTime() / (1.5e8 / i)) * (i * 3.5); // Faster frequency
            double perpX = Math.cos(aim_angle + Math.PI / 2) * offset;
            double perpY = Math.sin(aim_angle + Math.PI / 2) * offset;

            b.setStroke(new BasicStroke(trailWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, trailAlpha));
            b.setColor(new Color(100, 180, 255, 100));
            b.drawLine((int) (cx + perpX), (int) (cy + perpY), (int) (ex + perpX), (int) (ey + perpY));
        }

        // Main beam with enhanced effects
        float outer_w = BEAM_WIDTH * 3.8f * fade * pulse;
        b.setStroke(new BasicStroke(outer_w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f * fade));
        b.setColor(GLOW_COLOR);
        b.drawLine((int) cx, (int) cy, (int) ex, (int) ey);

        float mid_w = BEAM_WIDTH * 2.5f * fade;
        b.setStroke(new BasicStroke(mid_w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * fade * pulse));
        b.setColor(new Color(120, 190, 255, 180));
        b.drawLine((int) cx, (int) cy, (int) ex, (int) ey);

        // More animated pulse waves along beam with faster frequency
        float phase = (System.nanoTime() % 1000000000) / 800000000.0f; // Faster phase movement
        for (int i = 0; i < 6; i++) { // Increased from 3 to 6 pulses
            float pos = (phase + i * 0.16f) % 1.0f; // More evenly distributed
            float pulseSize = BEAM_WIDTH * (2.0f + shine * 0.5f) * fade * (1.0f - pos * 0.8f);
            if (pos < 0.95f) {
                int px = (int) (cx + (ex - cx) * pos);
                int py = (int) (cy + (ey - cy) * pos);
                b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f * fade * (1.0f - pos)));
                b.setColor(new Color(255, 255, 255, 180));
                b.fillOval(px - (int) (pulseSize / 2), py - (int) (pulseSize / 2), (int) pulseSize, (int) pulseSize);
            }
        }

        float main_w = BEAM_WIDTH * 1.7f * fade * shine;
        b.setStroke(new BasicStroke(main_w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f * fade * shine));
        b.setColor(MAIN_COLOR);
        b.drawLine((int) cx, (int) cy, (int) ex, (int) ey);

        float core_w = BEAM_WIDTH * 0.9f * fade;
        b.setStroke(new BasicStroke(core_w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f * fade * shine));
        b.setColor(CORE_COLOR);
        b.drawLine((int) cx, (int) cy, (int) ex, (int) ey);

        // Bright central line with fluctuating width
        float center_w = core_w * 0.4f * (0.9f + 0.1f * (float) Math.sin(System.nanoTime() / 1.5e8));
        b.setStroke(new BasicStroke(center_w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f * fade));
        b.setColor(new Color(255, 255, 255, 240));
        b.drawLine((int) cx, (int) cy, (int) ex, (int) ey);

        // Add magical sparkles along the beam
        Random sparkRng = new Random(System.nanoTime() / 10000000);
        for (int i = 0; i < 8; i++) {
            float t = sparkRng.nextFloat();
            int sx = (int) (cx + t * (ex - cx));
            int sy = (int) (cy + t * (ey - cy));
            float sparkSize = 3f + sparkRng.nextFloat() * 5f;
            b.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f * fade));
            b.setColor(new Color(255, 255, 255, 220));
            b.fillOval(sx - (int) (sparkSize / 2), sy - (int) (sparkSize / 2), (int) sparkSize, (int) sparkSize);
        }

        b.dispose();
    }

    class BeamParticle {
        enum Type {
            CHARGE, BEAM
        }

        double x, y, vx, vy;
        float life, max_life;
        final Type type;
        final float size;

        BeamParticle(double x, double y, double vx, double vy, Type type, float life_time) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.type = type;
            this.life = this.max_life = life_time;
            this.size = type == Type.BEAM ? 1f + rng.nextFloat() * 1.5f : 1.5f + rng.nextFloat() * 2f;
        }

        void update(float dt) {
            x += vx * dt;
            y += vy * dt;
            life -= dt;
            vx *= 0.95;
            vy *= 0.95;
        }

        boolean dead() {
            return life <= 0;
        }

        void draw(Graphics2D g) {
            float alpha = Math.max(0.0f, Math.min(1f, life / max_life));
            if (type == Type.CHARGE)
                drawCharge(g, alpha);
            else
                drawBeam(g, alpha);
        }

        void drawCharge(Graphics2D g, float alpha) {
            Graphics2D p = (Graphics2D) g.create();
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(alpha * 0.8f, 1f)));
            RadialGradientPaint paint = new RadialGradientPaint((float) x, (float) y, size * 2.5f,
                    new float[] { 0f, 1f },
                    new Color[] { new Color(255, 255, 255, Math.min(255, Math.max(0, (int) (200 * alpha)))),
                            new Color(200, 220, 255, 0) });
            p.setPaint(paint);
            float d = size * 2f;
            p.fill(new Ellipse2D.Float((float) (x - d / 2), (float) (y - d / 2), d, d));
            p.setColor(CHARGE_COLOR);
            p.fill(new Ellipse2D.Float((float) (x - size / 2), (float) (y - size / 2), size, size));
            p.dispose();
        }

        void drawBeam(Graphics2D g, float alpha) {
            Graphics2D p = (Graphics2D) g.create();
            float safe_alpha = Math.max(0.0f, Math.min(alpha * 0.9f, 1f));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safe_alpha));
            RadialGradientPaint outer = new RadialGradientPaint((float) x, (float) y, size * 4f,
                    new float[] { 0f, 0.7f, 1f },
                    new Color[] { new Color(180, 210, 255, Math.min(255, Math.max(0, (int) (60 * alpha)))),
                            new Color(140, 180, 255, Math.min(255, Math.max(0, (int) (30 * alpha)))),
                            new Color(120, 160, 255, 0) });
            p.setPaint(outer);
            float outer_d = size * 3f;
            p.fill(new Ellipse2D.Float((float) (x - outer_d / 2), (float) (y - outer_d / 2), outer_d, outer_d));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safe_alpha));
            RadialGradientPaint main = new RadialGradientPaint((float) x, (float) y, size * 3f,
                    new float[] { 0f, 0.5f, 1f },
                    new Color[] { new Color(255, 255, 255, Math.min(255, Math.max(0, (int) (220 * alpha)))),
                            new Color(160, 200, 255, Math.min(255, Math.max(0, (int) (150 * alpha)))),
                            new Color(120, 180, 255, 0) });
            p.setPaint(main);
            float d = size * 2f;
            p.fill(new Ellipse2D.Float((float) (x - d / 2), (float) (y - d / 2), d, d));
            p.setColor(new Color(255, 255, 255, Math.min(255, Math.max(0, (int) (240 * alpha)))));
            p.fill(new Ellipse2D.Float((float) (x - size / 4), (float) (y - size / 4), size / 2, size / 2));
            p.dispose();
        }
    }

    class ImpactEffect {
        final double x, y;
        float life = 0.5f;
        final float max_life = 0.5f;
        final float size;
        final float rot_speed;

        ImpactEffect(double x, double y) {
            this.x = x;
            this.y = y;
            this.size = 5f + rng.nextFloat() * 5f;
            this.rot_speed = 5f + rng.nextFloat() * 5f;
        }

        void update(float dt) {
            life -= dt;
        }

        boolean dead() {
            return life <= 0;
        }

        void draw(Graphics2D g) {
            Graphics2D p = (Graphics2D) g.create();
            float alpha = Math.min(1f, life / max_life);
            float scale = 0.5f + (1f - life / max_life) * 1.8f;
            p.translate(x, y);
            p.rotate(life * rot_speed);
            p.scale(scale, scale);
            float safe_alpha = Math.max(0.0f, Math.min(alpha * 0.9f, 1f));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safe_alpha * 0.7f));
            p.setColor(new Color(170, 210, 255, Math.min(255, Math.max(0, (int) (120 * alpha)))));
            p.fillOval((int) (-size * 2.5), (int) (-size * 2.5), (int) (size * 5), (int) (size * 5));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safe_alpha * 0.8f));
            p.setColor(new Color(200, 220, 255, Math.min(255, Math.max(0, (int) (150 * alpha)))));
            p.fillOval((int) (-size * 1.7), (int) (-size * 1.7), (int) (size * 3.4), (int) (size * 3.4));
            p.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, safe_alpha));
            p.setColor(new Color(255, 255, 255, Math.min(255, Math.max(0, (int) (220 * alpha)))));
            int spikes = 8;
            for (int i = 0; i < spikes; i++) {
                double ang = i * Math.PI * 2 / spikes;
                float inner_r = size * 0.5f;
                float outer_r = size * (1f + 0.3f * (float) Math.sin(i * 3 + life * 10));
                p.setStroke(new BasicStroke(2f));
                p.drawLine((int) (inner_r * Math.cos(ang)), (int) (inner_r * Math.sin(ang)),
                        (int) (outer_r * Math.cos(ang)), (int) (outer_r * Math.sin(ang)));
            }
            p.setColor(new Color(230, 240, 255, (int) (180 * alpha)));
            p.fillOval((int) (-size * 0.8), (int) (-size * 0.8), (int) (size * 1.6), (int) (size * 1.6));
            p.setColor(new Color(255, 255, 255, (int) (240 * alpha)));
            p.fillOval((int) (-size * 0.3), (int) (-size * 0.3), (int) (size * 0.6), (int) (size * 0.6));
            p.dispose();
        }
    }
}
