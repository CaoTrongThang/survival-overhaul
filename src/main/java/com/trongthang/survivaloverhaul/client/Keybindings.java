package com.trongthang.survivaloverhaul.client;

import com.trongthang.survivaloverhaul.client.screen.BodyDamageScreen;
import com.trongthang.survivaloverhaul.config.ModConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class Keybindings {

    public static KeyBinding openBodyDamageScreenKey;

    public static void register() {
        openBodyDamageScreenKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.survivaloverhaul.open_body_damage_screen",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "category.survivaloverhaul.keys"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openBodyDamageScreenKey.wasPressed()) {
                if (ModConfig.enableBodyDamage && client.player != null && client.currentScreen == null) {
                    client.setScreen(new BodyDamageScreen(false));
                }
            }
        });
    }
}
