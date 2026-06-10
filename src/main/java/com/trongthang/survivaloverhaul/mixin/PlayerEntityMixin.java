package com.trongthang.survivaloverhaul.mixin;

import com.trongthang.survivaloverhaul.config.ModConfig;
import com.trongthang.survivaloverhaul.mechanics.thirst.IThirstData;
import com.trongthang.survivaloverhaul.mechanics.thirst.ThirstManager;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.IBodyDamageData;
import com.trongthang.survivaloverhaul.mechanics.bodyparts.BodyDamageManager;
import com.trongthang.survivaloverhaul.mechanics.poop.IPoopData;
import com.trongthang.survivaloverhaul.mechanics.poop.PoopManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.trongthang.survivaloverhaul.mechanics.temperature.ITemperatureData;
import com.trongthang.survivaloverhaul.mechanics.temperature.TemperatureManager;
import com.trongthang.survivaloverhaul.mechanics.temperature.TemperatureState;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity
        implements IThirstData, IBodyDamageData, ITemperatureData, IPoopData {

    @Unique
    private final ThirstManager survivalOverhaul$thirstManager = new ThirstManager();

    @Unique
    private final BodyDamageManager survivalOverhaul$bodyDamageManager = new BodyDamageManager(
            (PlayerEntity) (Object) this);

    @Unique
    private final TemperatureManager survivalOverhaul$temperatureManager = new TemperatureManager(
            (LivingEntity) (Object) this);

    @Unique
    private final PoopManager survivalOverhaul$poopManager = new PoopManager();

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

    @Override
    public TemperatureManager survivalOverhaul$getTemperatureManager() {
        return this.survivalOverhaul$temperatureManager;
    }

    @Override
    public PoopManager survivalOverhaul$getPoopManager() {
        return this.survivalOverhaul$poopManager;
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    protected void survivalOverhaul$initDataTracker(CallbackInfo ci) {
        this.dataTracker.startTracking(BodyDamageManager.HEAD_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.TORSO_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.LEFT_ARM_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.RIGHT_ARM_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.LEFT_LEG_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.RIGHT_LEG_HEALTH, 20.0f);
        this.dataTracker.startTracking(BodyDamageManager.LEFT_FOOT_HEALTH, 10.0f);
        this.dataTracker.startTracking(BodyDamageManager.RIGHT_FOOT_HEALTH, 10.0f);
        this.dataTracker.startTracking(TemperatureManager.BODY_TEMPERATURE, 20.0f);
        this.dataTracker.startTracking(TemperatureManager.AMBIENT_TEMPERATURE, 20.0f);
        this.dataTracker.startTracking(TemperatureManager.TEMPERATURE_STATE, TemperatureState.NORMAL.getId());
        this.dataTracker.startTracking(TemperatureManager.BIOME_CONTRIBUTION, 20.0f);
        this.dataTracker.startTracking(TemperatureManager.ENVIRONMENT_MODIFIER, 0.0f);
        this.dataTracker.startTracking(TemperatureManager.THERMAL_MODIFIER, 0.0f);
        this.dataTracker.startTracking(TemperatureManager.EQUIPMENT_MODIFIER, 0.0f);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void survivalOverhaul$tick(CallbackInfo ci) {
        if (!this.getWorld().isClient) {
            this.survivalOverhaul$thirstManager.update((PlayerEntity) (Object) this);
            if (ModConfig.enableBodyDamage) {
                this.survivalOverhaul$bodyDamageManager.update();
            }
            if (ModConfig.enableTemperature) {
                this.survivalOverhaul$temperatureManager.update();
            }
        }
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void survivalOverhaul$readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
        this.survivalOverhaul$thirstManager.readNbt(nbt);
        this.survivalOverhaul$bodyDamageManager.readNbt(nbt);
        this.survivalOverhaul$temperatureManager.readNbt(nbt);
        this.survivalOverhaul$poopManager.readNbt(nbt);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void survivalOverhaul$writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
        this.survivalOverhaul$thirstManager.writeNbt(nbt);
        this.survivalOverhaul$bodyDamageManager.writeNbt(nbt);
        this.survivalOverhaul$temperatureManager.writeNbt(nbt);
        this.survivalOverhaul$poopManager.writeNbt(nbt);
    }

    @Inject(method = "eatFood", at = @At("RETURN"))
    private void survivalOverhaul$onEatFood(World world, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (!world.isClient && ModConfig.enablePoop && stack.isFood()) {
            FoodComponent food = stack.getItem().getFoodComponent();
            if (food != null) {
                this.survivalOverhaul$poopManager.onFoodEaten((PlayerEntity) (Object) this, food.getHunger());
            }
        }
    }

    @Inject(method = "applyDamage", at = @At("HEAD"))
    protected void survivalOverhaul$applyBodyDamage(DamageSource source, float amount, CallbackInfo ci) {
        if (!this.getWorld().isClient() && ModConfig.enableBodyDamage && !source.isOf(DamageTypes.OUT_OF_WORLD)) {
            this.survivalOverhaul$bodyDamageManager.applyDamage(source, amount);
        }
    }

}
