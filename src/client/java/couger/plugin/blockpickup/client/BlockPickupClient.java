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
                BlockPickup.isEnabled = !BlockPickup.isEnabled;
                if (BlockPickup.isEnabled) {
                    BlockPickup.takeInventorySnapshot(client.player.getInventory());
                    client.player.sendMessage(Text.literal("§aGuard: ON"), true);
                } else {
                    client.player.sendMessage(Text.literal("§cGuard: OFF"), true);
                }
            }

            if (BlockPickup.isEnabled && !BlockPickup.taskQueue.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastActionTime >= 150) {
                    var task = BlockPickup.taskQueue.poll();
                    if (task != null) {
                        executeDrop(client, task);
                        lastActionTime = currentTime;
                    }
                }
            }
        });
    }

    private void executeDrop(MinecraftClient client, BlockPickup.CursorTask task) {
        int syncId = client.player.currentScreenHandler.syncId;
        client.interactionManager.clickSlot(syncId, task.syncSlot(), 0, SlotActionType.PICKUP, client.player);
        for (int i = 0; i < task.countToKeep(); i++) {
            client.interactionManager.clickSlot(syncId, task.syncSlot(), 1, SlotActionType.PICKUP, client.player);
        }
        client.interactionManager.clickSlot(syncId, -999, 0, SlotActionType.PICKUP, client.player);
    }
}