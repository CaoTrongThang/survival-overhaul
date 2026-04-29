package com.trongthang.survivaloverhaul;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SoundsManager {
    public static final SoundEvent BANDAGE_USE_1 = registerSoundEvent("bandage_use_1");
    public static final SoundEvent BANDAGE_USE_2 = registerSoundEvent("bandage_use_2");
    public static final SoundEvent DRINKING_POITION = registerSoundEvent("drinking_poition");

    private static SoundEvent registerSoundEvent(String name) {
        Identifier id = new Identifier(SurvivalOverhaul.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void playRandomBandageSound(net.minecraft.world.World world,
            net.minecraft.entity.player.PlayerEntity player) {
        if (world == null || player == null)
            return;

        SoundEvent sound = world.random.nextBoolean() ? BANDAGE_USE_1 : BANDAGE_USE_2;
        world.playSound(null, player.getBlockPos(), sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

    public static void registerSounds() {
        SurvivalOverhaul.LOGGER.info("Registring sounds for " + SurvivalOverhaul.MOD_ID);
    }
}
