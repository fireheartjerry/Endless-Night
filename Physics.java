import java.awt.Rectangle;
import java.util.List;

public class Physics {
    private static final float PLAYER_PUSH_FORCE = 14f;
    private static final float ENEMY_SEPARATION_GAP = 2.5f;
    private static final float COLLISION_RESTITUTION = 0.65f;
    private static final float COLLISION_FRICTION = 0.2f;
    private static final float FLOATING_POINT_EPSILON = 1e-4f;
    private static final float COLLISION_SLOP = 0.01f;
    private static final float POSITION_BIAS_FACTOR = 0.2f;
    private static final float CONTINUOUS_COLLISION_THRESHOLD = 8f;
    private static final float PLAYER_ENTITY_MASS = 2.0f;
    private static final float ENEMY_ENTITY_MASS = 1.0f;

    private Physics() {
    }

    public static void resolveCollisions(GameState state, Player player, List<Enemy> enemies, float dt) {
        if (state != GameState.PLAYING)
            return;

        float dt_scale = Math.min(dt * 60f, 2.0f);

        resolvePlayerEnemyCollisions(player, enemies, dt_scale);
        resolveEnemyEnemyCollisions(enemies, dt_scale);
    }

    private static void resolvePlayerEnemyCollisions(Player player, List<Enemy> enemies, float dt_scale) {
        Rectangle player_broadphase = player.getBounds();
        expandRectByVelocity(player_broadphase, player.getXVelocity(), player.getYVelocity());

        for (Enemy enemy : enemies) {
            Rectangle enemy_broadphase = enemy.getBounds();
            expandRectByVelocity(enemy_broadphase, enemy.getXVelocity(), enemy.getYVelocity());

            if (!player_broadphase.intersects(enemy_broadphase))
                continue;

            if (circleIntersection(player, enemy)) {
                player.takeDamage(enemy.getDamage());

                Vector normal = new Vector(
                        (float) (player.getCenterX() - enemy.getCenterX()),
                        (float) (player.getCenterY() - enemy.getCenterY()));
                float distance = normal.length();

                if (distance < FLOATING_POINT_EPSILON) {
                    normal.set((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
                } else {
                    normal.scale(1f / distance);
                }

                float penetration = enemy.getRadius() + player.getRadius() - distance;

                if (penetration > 0f) {
                    float total_mass = PLAYER_ENTITY_MASS + ENEMY_ENTITY_MASS;
                    float enemy_ratio = PLAYER_ENTITY_MASS / total_mass;

                    float push_factor = PLAYER_PUSH_FORCE * dt_scale;
                    penetration += push_factor * enemy_ratio;

                    enemy.setPos(
                            enemy.getCenterX() - normal.x * penetration * enemy_ratio,
                            enemy.getCenterY() - normal.y * penetration * enemy_ratio);
                }

                Vector relative_vel = new Vector(
                        enemy.getXVelocity() - player.getXVelocity(),
                        enemy.getYVelocity() - player.getYVelocity());

                float base_push = 10.0f * dt_scale;
                float angle_bonus = 1.0f + Math.max(0, -relative_vel.dot(normal) / 10.0f);
                float final_force = base_push * angle_bonus;

                enemy.applyKnockback(
                        -normal.x * final_force,
                        -normal.y * final_force);
            }
        }
    }

    private static void resolveEnemyEnemyCollisions(List<Enemy> enemies, float dt_scale) {
        int size = enemies.size();

        for (int i = 0; i < size; i++) {
            Enemy a = enemies.get(i);

            float a_speed = (float) Math.hypot(a.getXVelocity(), a.getYVelocity());
            boolean a_moving_fast = a_speed > 0.5f;

            for (int j = i + 1; j < size; j++) {
                Enemy b = enemies.get(j);

                float b_speed = (float) Math.hypot(b.getXVelocity(), b.getYVelocity());
                if (!a_moving_fast && !(b_speed > 0.5f))
                    continue;

                if (!circleIntersection(a, b))
                    continue;

                Vector normal = new Vector(
                        (float) (a.getCenterX() - b.getCenterX()),
                        (float) (a.getCenterY() - b.getCenterY()));
                float distance = normal.length();

                if (distance < FLOATING_POINT_EPSILON) {
                    normal.set((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
                    distance = FLOATING_POINT_EPSILON;
                } else {
                    normal.scale(1f / distance);
                }

                float target_distance = a.getRadius() + b.getRadius() + ENEMY_SEPARATION_GAP;
                float penetration = target_distance - distance;

                if (penetration > COLLISION_SLOP) {
                    float correction = (penetration - COLLISION_SLOP) * POSITION_BIAS_FACTOR * dt_scale;

                    a.setPos(
                            a.getCenterX() + normal.x * correction * 0.5f,
                            a.getCenterY() + normal.y * correction * 0.5f);
                    b.setPos(
                            b.getCenterX() - normal.x * correction * 0.5f,
                            b.getCenterY() - normal.y * correction * 0.5f);
                }

                if (a.isInKnockbackState() || b.isInKnockbackState())
                    continue;

                Vector rel_vel = new Vector(
                        b.getXVelocity() - a.getXVelocity(),
                        b.getYVelocity() - a.getYVelocity());

                float vel_along_normal = rel_vel.dot(normal);

                if (vel_along_normal > 0)
                    continue;

                float restitution = COLLISION_RESTITUTION;

                float impulse_scalar = -(1.0f + restitution) * vel_along_normal;
                impulse_scalar /= 2.0f;
                impulse_scalar *= dt_scale;

                Vector impulse = normal.copy().scale(impulse_scalar);

                Vector tangent = new Vector(-normal.y, normal.x);

                float vel_along_tangent = rel_vel.dot(tangent);

                float friction_impulse = -vel_along_tangent * COLLISION_FRICTION * impulse_scalar;
                Vector friction_vec = tangent.copy().scale(friction_impulse);

                a.setVelocity(
                        a.getXVelocity() - impulse.x - friction_vec.x,
                        a.getYVelocity() - impulse.y - friction_vec.y);
                b.setVelocity(
                        b.getXVelocity() + impulse.x + friction_vec.x,
                        b.getYVelocity() + impulse.y + friction_vec.y);
            }
        }
    }

    private static boolean circleIntersection(Entity a, Entity b) {
        float dx = (float) (b.getCenterX() - a.getCenterX());
        float dy = (float) (b.getCenterY() - a.getCenterY());
        float radius_sum = a.getRadius() + b.getRadius();
        return dx * dx + dy * dy <= radius_sum * radius_sum;
    }

    private static void expandRectByVelocity(Rectangle rect, float vx, float vy) {
        float speed = (float) Math.hypot(vx, vy);
        if (speed > CONTINUOUS_COLLISION_THRESHOLD) {
            int expansion = (int) (speed * 0.5f);
            rect.grow(expansion, expansion);
        }
    }
}
