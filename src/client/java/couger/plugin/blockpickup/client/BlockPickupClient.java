package couger.plugin.blockpickup.client;

import couger.plugin.blockpickup.BlockPickup;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class BlockPickupClient implements ClientModInitializer {
    public static KeyBinding toggleKey;
    private static long lastActionTime = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blockpickup.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                KeyBinding.Category.create(Identifier.of("block_pickup", "main"))
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (toggleKey.wasPressed()) {
                if (!BlockPickup.isEnabled) {
                    client.setScreen(new PickupConfirmScreen());
                } else {
                    BlockPickup.isEnabled = false;
                    BlockPickup.taskQueue.clear();
                    client.player.sendMessage(Text.literal("§cPickup Blocking: OFF"), true);
                }
            }

            if (BlockPickup.isEnabled && client.currentScreen == null && !BlockPickup.taskQueue.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastActionTime >= 150) {
                    var task = BlockPickup.taskQueue.poll();
                    if (task != null) {
                        executeDropLogic(client, task);
                        lastActionTime = currentTime;
                    }
                }
            }
        });
    }

    private void executeDropLogic(MinecraftClient client, BlockPickup.CursorTask task) {
        if (client.player == null || client.interactionManager == null) return;
        int syncId = client.player.currentScreenHandler.syncId;

        client.interactionManager.clickSlot(syncId, task.syncSlot(), 0, SlotActionType.PICKUP, client.player);
        for (int i = 0; i < task.countToKeep(); i++) {
            client.interactionManager.clickSlot(syncId, task.syncSlot(), 1, SlotActionType.PICKUP, client.player);
        }
        client.interactionManager.clickSlot(syncId, -999, 0, SlotActionType.PICKUP, client.player);
    }
}