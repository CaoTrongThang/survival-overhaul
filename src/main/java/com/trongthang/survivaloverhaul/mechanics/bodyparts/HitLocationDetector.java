package com.trongthang.survivaloverhaul.mechanics.bodyparts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;

/**
 * Detects which body part was hit based on the spatial relationship
 * between the damage source and the victim.
 *
 * <p>
 * <b>Two detection strategies:</b>
 * <ul>
 * <li><b>Melee:</b> Compares the attacker's center Y to the victim's hitbox
 * to determine the vertical zone (head/torso/arm/leg/foot). Left vs right
 * is resolved via a horizontal angle calculation relative to victim
 * facing.</li>
 * <li><b>Projectile:</b> Uses the projectile entity's Y for the vertical zone.
 * Left/right is random — arrow angle at the moment of impact is
 * unreliable.</li>
 * <li><b>Fallback:</b> Purely random — used for AoE, fire, magic, and any
 * environmental damage that carries no positional info.</li>
 * </ul>
 *
 * <p>
 * <b>Mod compatibility:</b> Works with any mod's entities and projectiles
 * because all entities share {@link Entity#getY()} and
 * {@link Entity#getHeight()}.
 * No special-casing for any specific mod is needed.
 */
public class HitLocationDetector {

    // -------------------------------------------------------------------------
    // Zone thresholds — fraction of victim height (0.0 = feet, 1.0 = top of head)
    // -------------------------------------------------------------------------

    /** Impact at or above 75% of victim height → HEAD */
    private static final double ZONE_HEAD = 0.75;

    /** Impact at or above 40% of victim height → TORSO or ARM */
    private static final double ZONE_TORSO = 0.40;

    /** Impact at or above 15% of victim height → LEGS */
    private static final double ZONE_LEGS = 0.15;

    // Below ZONE_LEGS → FEET

    /**
     * Minimum horizontal angle (degrees) off-center for a melee attack to count
     * as hitting an arm instead of the torso. An attacker directly in front scores
     * 0° — they hit the torso. An attacker squarely to the side scores 90° —
     * they hit the arm.
     */
    private static final double ARM_SIDE_MIN = 55.0;

    /**
     * Maximum angle for an arm hit. Beyond this (~125°) the attacker is coming
     * from almost behind — we treat that as a torso hit (backstab).
     */
    private static final double ARM_SIDE_MAX = 125.0;

    /**
     * Random ± spread added to the melee vertical ratio before zone lookup.
     * Simulates natural swing variance — a zombie centered at your torso
     * will mostly hit there, but can occasionally reach your legs or head.
     *
     * <p>
     * 0.30 means the impact point can drift ±30% of your total height from
     * the attacker's actual center, giving a realistic distribution:
     * roughly torso 55%, legs 25%, head 15%, feet 5% for same-height mobs.
     */
    private static final double MELEE_SWING_SPREAD = 0.30;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Returns the body part most likely hit, given the damage source and victim.
     *
     * <p>
     * Call this from {@link BodyDamageManager#applyDamage} for all generic
     * (non-typed) hits.
     */
    public static BodyPart detect(LivingEntity victim, DamageSource source) {
        // getSource() returns the actual projectile entity (arrow, trident, etc.)
        // It is distinct from getAttacker() which is the mob/player who fired it.
        Entity projectile = source.getSource();
        if (projectile != null && projectile != source.getAttacker()) {
            return detectFromProjectile(victim, projectile);
        }

        // getAttacker() returns the living entity responsible for a melee hit.
        Entity attacker = source.getAttacker();
        if (attacker != null) {
            return detectFromMelee(victim, attacker);
        }

        // No position data available (explosion, fire, magic, drowning, etc.).
        return randomPart(victim);
    }

    // -------------------------------------------------------------------------
    // Detection strategies
    // -------------------------------------------------------------------------

    /**
     * Projectile hit: vertical zone from projectile Y, left/right is random.
     *
     * <p>
     * Arrows travel fast and can arc — using their angle at impact to determine
     * left vs right would be unreliable, so we randomize it.
     */
    private static BodyPart detectFromProjectile(LivingEntity victim, Entity projectile) {
        double impactY = projectile.getY() + projectile.getHeight() * 0.5;
        double ratio = verticalRatio(victim, impactY);

        // Even projectiles shouldn't be 100% precise in their impact zone,
        // though they are generally more accurate than a wild melee swing.
        double jitter = (victim.getRandom().nextFloat() * 2f - 1f) * (MELEE_SWING_SPREAD * 0.5);
        ratio = Math.max(0.0, Math.min(1.0, ratio + jitter));

        double sideAngle = horizontalAngleFromFacing(victim, projectile);
        boolean isLeft = sideAngle < 0; // negative = projectile on victim's left

        if (ratio >= ZONE_HEAD)
            return BodyPart.HEAD;
        if (ratio >= ZONE_TORSO) {
            // Horizontal detection for projectile arm hits
            double absAngle = Math.abs(sideAngle);
            boolean isSideAttack = absAngle >= ARM_SIDE_MIN && absAngle <= ARM_SIDE_MAX;
            return isSideAttack
                    ? (isLeft ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM)
                    : BodyPart.TORSO;
        }
        if (ratio >= ZONE_LEGS)
            return isLeft ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        return isLeft ? BodyPart.LEFT_FOOT : BodyPart.RIGHT_FOOT;
    }

    /**
     * Melee hit: vertical zone from attacker's center Y, left/right from angle.
     *
     * <p>
     * If the attacker is coming clearly from a side angle ({@link #ARM_SIDE_MIN}°
     * to {@link #ARM_SIDE_MAX}° off-center), the hit lands on an arm instead of
     * the torso. All other zones also use the horizontal angle for left/right.
     */
    private static BodyPart detectFromMelee(LivingEntity victim, Entity attacker) {
        double attackerCenterY = attacker.getY() + attacker.getHeight() * 0.5;

        // Cap the impact point so huge mobs (Bosses/Giants) hit the victim's upper
        // body,
        // not the empty air above them. This ensures they hit Head/Torso naturally.
        double maxTargetY = victim.getY() + victim.getHeight() * 0.85;
        double impactY = Math.min(attackerCenterY, maxTargetY);

        double ratio = verticalRatio(victim, impactY);

        // Apply random swing spread so same-height attackers don't always
        // hit the exact same zone. Uniform distribution across ±MELEE_SWING_SPREAD.
        double jitter = (victim.getRandom().nextFloat() * 2f - 1f) * MELEE_SWING_SPREAD;
        ratio = Math.max(0.0, Math.min(1.0, ratio + jitter));

        double sideAngle = horizontalAngleFromFacing(victim, attacker);
        boolean isLeft = sideAngle < 0; // negative = attacker on victim's left

        if (ratio >= ZONE_HEAD) {
            return BodyPart.HEAD;
        }

        if (ratio >= ZONE_TORSO) {
            // Arm hit only when attacker is clearly coming from the side.
            double absAngle = Math.abs(sideAngle);
            boolean isSideAttack = absAngle >= ARM_SIDE_MIN && absAngle <= ARM_SIDE_MAX;
            return isSideAttack
                    ? (isLeft ? BodyPart.LEFT_ARM : BodyPart.RIGHT_ARM)
                    : BodyPart.TORSO;
        }

        if (ratio >= ZONE_LEGS) {
            return isLeft ? BodyPart.LEFT_LEG : BodyPart.RIGHT_LEG;
        }

        return isLeft ? BodyPart.LEFT_FOOT : BodyPart.RIGHT_FOOT;
    }

    // -------------------------------------------------------------------------
    // Math helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a world Y coordinate to a 0.0–1.0 fraction within the victim's
     * bounding box (0 = feet, 1 = top of head). Clamped so out-of-range values
     * (e.g. very tall attackers) stay within valid zone bounds.
     */
    private static double verticalRatio(LivingEntity victim, double worldY) {
        double height = victim.getHeight();
        if (height <= 0)
            return 0.5; // safety guard for zero-height edge cases
        double ratio = (worldY - victim.getY()) / height;
        return Math.max(0.0, Math.min(1.0, ratio));
    }

    /**
     * Returns the signed horizontal angle (degrees) from the victim's facing
     * direction to the direction of {@code other}.
     *
     * <ul>
     * <li>Positive → {@code other} is to the victim's <b>right</b></li>
     * <li>Negative → {@code other} is to the victim's <b>left</b></li>
     * </ul>
     *
     * <p>
     * Uses a 2D forward-vector cross/dot product (X–Z plane).
     * Minecraft yaw convention: 0° = south (+Z), 90° = west (−X).
     */
    private static double horizontalAngleFromFacing(LivingEntity victim, Entity other) {
        double dx = other.getX() - victim.getX();
        double dz = other.getZ() - victim.getZ();

        double yawRad = Math.toRadians(victim.getYaw());
        // Forward vector in world space (MC: south at yaw=0)
        double fwdX = -Math.sin(yawRad);
        double fwdZ = Math.cos(yawRad);

        // 2D cross product (fwd × toOther): positive → 'other' is on victim's right.
        double cross = fwdX * dz - fwdZ * dx;
        // Dot product: positive → 'other' is in front of victim.
        double dot = fwdX * dx + fwdZ * dz;

        return Math.toDegrees(Math.atan2(cross, dot));
    }

    /** Picks a random body part (fallback for damage with no positional info). */
    private static BodyPart randomPart(LivingEntity victim) {
        BodyPart[] parts = BodyPart.values();
        return parts[victim.getRandom().nextInt(parts.length)];
    }
}
