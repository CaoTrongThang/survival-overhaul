package com.trongthang.survivaloverhaul.mixin;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mechanics.thirst.ThirstManager;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyDamageManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements IThirstData, IBodyDamageData {

    @Unique
    private final ThirstManager survivalOverhaul$thirstManager = new ThirstManager();

    @Unique
    private final BodyDamageManager survivalOverhaul$bodyDamageManager = new BodyDamageManager(
            (PlayerEntity) (Object) this);

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public ThirstManager survivalOverhaul$getThirstManager() {
        return this.survivalOverhaul$thirstManager;
    }

    @Override
    public BodyDamageManager survivalOverhaul$getBodyDamageManager() {
        return this.survivalOverhaul$bodyDamageManager;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void survivalOverhaul$tickThirst(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            this.survivalOverhaul$thirstManager.update((PlayerEntity) (Object) this);
            this.survivalOverhaul$bodyDamageManager.update();
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void survivalOverhaul$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.survivalOverhaul$thirstManager.readNbt(nbt);
        this.survivalOverhaul$bodyDamageManager.readNbt(nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void survivalOverhaul$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        this.survivalOverhaul$thirstManager.writeNbt(nbt);
        this.survivalOverhaul$bodyDamageManager.writeNbt(nbt);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    protected void survivalOverhaul$applyBodyDamage(net.minecraft.entity.damage.DamageSource source, float amount,
            CallbackInfo ci) {
        if (!this.getWorld().isClient() && !source.isOf(net.minecraft.entity.damage.DamageTypes.OUT_OF_WORLD)) {
            this.survivalOverhaul$bodyDamageManager.applyDamage(source, amount);
        }
    }

    @Inject(method = "addExhaustion", at = @At("TAIL"))
    private void survivalOverhaul$addExhaustion(float exhaustion, CallbackInfo ci) {
        if (!this.getWorld().isClient && ModConfig.enableThirst) {
            float addedExhaustion = exhaustion * 1.0F * ModConfig.thirstDepletionMultiplier;
            // Give 100% of hunger exhaustion to thirst by default, multiplied by config
            this.survivalOverhaul$getThirstManager()
                    .addExhaustion(addedExhaustion);
        }
    }
}
