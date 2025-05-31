/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class handles collisions with physics calculations!
*/

import java.awt.Rectangle;
import java.util.List;

public class Physics {
    // Constants for various physics parameters
    private static final float PLAYER_PUSH_FORCE = 14f; // Force applied to push the player during collisions with
                                                        // enemies
    private static final float ENEMY_SEPARATION_GAP = 2.5f; // Minimum distance maintained between enemy entities to
                                                            // prevent overlap
    private static final float COLLISION_RESTITUTION = 0.65f; // Coefficient of restitution, determining the bounciness
                                                              // of collisions
    private static final float COLLISION_FRICTION = 0.2f; // Coefficient of friction, affecting the tangential velocity
                                                          // during collisions
    private static final float FLOATING_POINT_EPSILON = 1e-4f; // Small threshold to handle precision errors in
                                                               // floating-point calculations
    private static final float COLLISION_SLOP = 0.01f; // Allowable overlap between entities before applying collision
                                                       // resolution
    private static final float POSITION_BIAS_FACTOR = 0.2f; // Factor controlling the rate of position correction during
                                                            // collision resolution
    private static final float CONTINUOUS_COLLISION_THRESHOLD = 8f; // Velocity threshold for enabling continuous
                                                                    // collision detection
    private static final float PLAYER_ENTITY_MASS = 2.0f; // Mass of the player entity, used in collision response
                                                          // calculations
    private static final float ENEMY_ENTITY_MASS = 1.0f; // Mass of an enemy entity, used in collision response
                                                         // calculations

    // Private constructor to prevent instantiation of this utility class
    private Physics() {
    }

    // Resolves collisions between the player, enemies, and other entities in the
    // game
    public static void resolveCollisions(GameState state, Player player, List<Enemy> enemies, float dt) {
        // Only resolve collisions if the game state is "PLAYING"
        if (state != GameState.PLAYING)
            return;

        // Scale the delta time to ensure consistent behavior
        float dt_scale = Math.min(dt * 60f, 2.0f);

        // Resolve collisions between the player and enemies
        resolvePlayerEnemyCollisions(player, enemies, dt_scale);

        // Resolve collisions between enemies
        resolveEnemyEnemyCollisions(enemies, dt_scale);
    }

    // Resolves collisions between the player and enemies
    private static void resolvePlayerEnemyCollisions(Player player, List<Enemy> enemies, float dt_scale) {
        // Get the player's bounding box and expand it based on velocity
        Rectangle player_broadphase = player.getBounds();
        expandRectByVelocity(player_broadphase, player.getXVelocity(), player.getYVelocity());

        // Iterate through all enemies
        for (Enemy enemy : enemies) {
            // Get the enemy's bounding box and expand it based on velocity
            Rectangle enemy_broadphase = enemy.getBounds();
            expandRectByVelocity(enemy_broadphase, enemy.getXVelocity(), enemy.getYVelocity());

            // Skip if the player's and enemy's bounding boxes do not intersect
            if (!player_broadphase.intersects(enemy_broadphase))
                continue;

            // Check if the player and enemy circles intersect
            if (circleIntersection(player, enemy)) {
                // Apply damage to the player
                player.takeDamage(enemy.getDamage());

                // Calculate the collision normal vector
                Vector normal = new Vector(
                        (float) (player.getCenterX() - enemy.getCenterX()),
                        (float) (player.getCenterY() - enemy.getCenterY()));
                float distance = normal.length();

                // Handle cases where the distance is too small to avoid division by zero
                if (distance < FLOATING_POINT_EPSILON) {
                    normal.set((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
                } else {
                    normal.scale(1f / distance);
                }

                // Calculate the penetration depth
                float penetration = enemy.getRadius() + player.getRadius() - distance;

                // Resolve penetration by adjusting the enemy's position
                if (penetration > 0f) {
                    float total_mass = PLAYER_ENTITY_MASS + ENEMY_ENTITY_MASS;
                    float enemy_ratio = PLAYER_ENTITY_MASS / total_mass;

                    float push_factor = PLAYER_PUSH_FORCE * dt_scale;
                    penetration += push_factor * enemy_ratio;

                    enemy.setPos(
                            enemy.getCenterX() - normal.x * penetration * enemy_ratio,
                            enemy.getCenterY() - normal.y * penetration * enemy_ratio);
                }

                // Calculate the relative velocity between the player and enemy
                Vector relative_vel = new Vector(
                        enemy.getXVelocity() - player.getXVelocity(),
                        enemy.getYVelocity() - player.getYVelocity());

                // Apply knockback to the enemy based on the collision
                float base_push = 10.0f * dt_scale;
                float angle_bonus = 1.0f + Math.max(0, -relative_vel.dot(normal) / 10.0f);
                float final_force = base_push * angle_bonus;

                enemy.applyKnockback(
                        -normal.x * final_force,
                        -normal.y * final_force);
            }
        }
    }

    // Resolves collisions between enemies
    private static void resolveEnemyEnemyCollisions(List<Enemy> enemies, float dt_scale) {
        int size = enemies.size();

        // Iterate through all pairs of enemies
        for (int i = 0; i < size; i++) {
            Enemy a = enemies.get(i);

            // Check if enemy 'a' is moving fast enough to consider for collision
            float a_speed = (float) Math.hypot(a.getXVelocity(), a.getYVelocity());
            boolean a_moving_fast = a_speed > 0.5f;

            for (int j = i + 1; j < size; j++) {
                Enemy b = enemies.get(j);

                // Check if enemy 'b' is moving fast enough to consider for collision
                float b_speed = (float) Math.hypot(b.getXVelocity(), b.getYVelocity());
                if (!a_moving_fast && !(b_speed > 0.5f))
                    continue;

                // Skip if the enemies' circles do not intersect
                if (!circleIntersection(a, b))
                    continue;

                // Calculate the collision normal vector
                Vector normal = new Vector(
                        (float) (a.getCenterX() - b.getCenterX()),
                        (float) (a.getCenterY() - b.getCenterY()));
                float distance = normal.length();

                // Handle cases where the distance is too small to avoid division by zero
                if (distance < FLOATING_POINT_EPSILON) {
                    normal.set((float) Math.random() * 2 - 1, (float) Math.random() * 2 - 1).normalize();
                    distance = FLOATING_POINT_EPSILON;
                } else {
                    normal.scale(1f / distance);
                }

                // Calculate the target distance and penetration depth
                float target_distance = a.getRadius() + b.getRadius() + ENEMY_SEPARATION_GAP;
                float penetration = target_distance - distance;

                // Resolve penetration by adjusting the positions of both enemies
                if (penetration > COLLISION_SLOP) {
                    float correction = (penetration - COLLISION_SLOP) * POSITION_BIAS_FACTOR * dt_scale;

                    a.setPos(
                            a.getCenterX() + normal.x * correction * 0.5f,
                            a.getCenterY() + normal.y * correction * 0.5f);
                    b.setPos(
                            b.getCenterX() - normal.x * correction * 0.5f,
                            b.getCenterY() - normal.y * correction * 0.5f);
                }

                // Skip if either enemy is in a knockback state
                if (a.isInKnockbackState() || b.isInKnockbackState())
                    continue;

                // Calculate the relative velocity between the two enemies
                Vector rel_vel = new Vector(
                        b.getXVelocity() - a.getXVelocity(),
                        b.getYVelocity() - a.getYVelocity());

                // Calculate the velocity along the collision normal
                float vel_along_normal = rel_vel.dot(normal);

                // Skip if the enemies are moving away from each other
                if (vel_along_normal > 0)
                    continue;

                // Calculate the impulse scalar based on restitution
                float restitution = COLLISION_RESTITUTION;

                float impulse_scalar = -(1.0f + restitution) * vel_along_normal;
                impulse_scalar /= 2.0f;
                impulse_scalar *= dt_scale;

                // Apply the impulse to both enemies
                Vector impulse = normal.copy().scale(impulse_scalar);

                // Calculate the friction impulse
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

    // Checks if two circular entities intersect
    private static boolean circleIntersection(Entity a, Entity b) {
        float dx = (float) (b.getCenterX() - a.getCenterX());
        float dy = (float) (b.getCenterY() - a.getCenterY());
        float radius_sum = a.getRadius() + b.getRadius();
        return dx * dx + dy * dy <= radius_sum * radius_sum;
    }

    // Expands a rectangle based on the velocity of an entity
    private static void expandRectByVelocity(Rectangle rect, float vx, float vy) {
        float speed = (float) Math.hypot(vx, vy);
        if (speed > CONTINUOUS_COLLISION_THRESHOLD) {
            int expansion = (int) (speed * 0.5f);
            rect.grow(expansion, expansion);
        }
    }
}
