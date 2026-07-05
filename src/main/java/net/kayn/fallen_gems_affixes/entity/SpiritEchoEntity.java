package net.kayn.fallen_gems_affixes.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class SpiritEchoEntity extends PathfinderMob implements ItemSupplier {
    private int lifetime = 100;
    private int age = 0;
    private UUID ownerUUID;

    public SpiritEchoEntity(EntityType<? extends SpiritEchoEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setInvulnerable(true);
        this.setSilent(true);
        this.setGlowingTag(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    public void setLifetime(int ticks) { this.lifetime = ticks; }

    public void setOwnerUUID(UUID uuid) { this.ownerUUID = uuid; }

    public UUID getOwnerUUID() { return this.ownerUUID; }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.ECHO_SHARD);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.random.nextInt(4) == 0) {
                this.level().addParticle(ParticleTypes.SOUL,
                        this.getX() + (this.random.nextDouble() - 0.5),
                        this.getY() + this.random.nextDouble(),
                        this.getZ() + (this.random.nextDouble() - 0.5), 0, 0.02, 0);
            }
            return;
        }
        this.age++;
        if (this.age >= this.lifetime) this.discard();
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    protected void pushEntities() {}

    @Override
    public boolean hurt(DamageSource source, float amount) { return false; }
}