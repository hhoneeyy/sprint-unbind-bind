package com.spunb;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class SprintUnbindClient implements ClientModInitializer {

    private static KeyMapping toggleKey;

    // Default sprint key is Left Shift
    private static final InputConstants.Key SPRINT_KEY = InputConstants.Type.KEYBOARD.getOrCreate(InputConstants.KEY_LSHIFT);
    private static boolean isUnbound = false;
    private static boolean initializedStartup = false;

    @Override
    public void onInitializeClient() {
        // Register toggle keybind under MOVEMENT (default: Right Control)
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.sprint_unbind.toggle",
                InputConstants.KEY_RCONTROL,
                KeyMapping.Category.MOVEMENT
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.options == null) return;

            // Startup check: If game was closed while unbound, restore Left Shift automatically on launch
            if (!initializedStartup) {
                initializedStartup = true;
                if (client.options.keySprint.isUnbound()) {
                    restoreSprintKey(client, false);
                }
            }

            // Handle toggle key press
            while (toggleKey.consumeClick()) {
                if (!isUnbound) {
                    // Unbind sprint
                    client.options.keySprint.setKey(InputConstants.UNKNOWN);
                    KeyMapping.resetMapping();
                    isUnbound = true;

                    if (client.gui != null && client.gui.hud != null) {
                        client.gui.hud.setOverlayMessage(Component.literal("§c[Sprint Unbound]"), false);
                    }
                } else {
                    // Rebind sprint to Left Shift
                    restoreSprintKey(client, true);
                }
            }
        });
    }

    private static void restoreSprintKey(Minecraft client, boolean showMessage) {
        client.options.keySprint.setKey(SPRINT_KEY);
        KeyMapping.resetMapping();
        isUnbound = false;

        if (showMessage && client.gui != null && client.gui.hud != null) {
            client.gui.hud.setOverlayMessage(Component.literal("§a[Sprint Bound: Left Shift]"), false);
        }
    }
}