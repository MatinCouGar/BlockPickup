package couger.plugin.blockpickup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockPickup implements ModInitializer {
    public static boolean isEnabled = false;
    public static HashMap<net.minecraft.item.Item, Integer> inventorySnapshot = new HashMap<>();
    public static KeyBinding toggleKey;

    // ✅ ساخت کتگوری اختصاصی به روش جدید (حل ارور Category)
    public static final KeyBinding.Category BLOCKPICKUP_CATEGORY =
            KeyBinding.Category.create(Identifier.of("block_pickup", "main"));

    // صف برای مدیریت تسک‌های دراپ کردن
    private static final ConcurrentLinkedQueue<CursorTask> taskQueue = new ConcurrentLinkedQueue<>();
    private static long lastActionTime = 0;

    public record CursorTask(int syncSlot, int countToKeep) {}

    @Override
    public void onInitialize() {
        // ثبت دکمه با استفاده از آبجکت کتگوری واقعی
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.blockpickup.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P,
                BLOCKPICKUP_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.interactionManager == null) return;

            // منطق روشن/خاموش کردن
            while (toggleKey.wasPressed()) {
                isEnabled = !isEnabled;
                if (isEnabled) {
                    takeInventorySnapshot(client.player.getInventory());
                    client.player.sendMessage(Text.literal("§aGuard: ON"), true);
                } else {
                    taskQueue.clear();
                    client.player.sendMessage(Text.literal("§cGuard: OFF"), true);
                }
            }

            // اجرای عملیات دراپ کردن از صف
            if (isEnabled && !taskQueue.isEmpty()) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastActionTime >= 150) {
                    CursorTask task = taskQueue.poll();
                    if (task != null) {
                        executeDropLogic(client, task);
                        lastActionTime = currentTime;
                    }
                }
            }
        });
    }

    private void executeDropLogic(MinecraftClient client, CursorTask task) {
        int syncId = client.player.currentScreenHandler.syncId;
        // ۱. برداشتن استک
        client.interactionManager.clickSlot(syncId, task.syncSlot, 0, SlotActionType.PICKUP, client.player);
        // ۲. برگرداندن تعداد مجاز
        for (int i = 0; i < task.countToKeep; i++) {
            client.interactionManager.clickSlot(syncId, task.syncSlot, 1, SlotActionType.PICKUP, client.player);
        }
        // ۳. دراپ بقیه محتویات (خارج از صفحه)
        client.interactionManager.clickSlot(syncId, -999, 0, SlotActionType.PICKUP, client.player);
    }

    public static void takeInventorySnapshot(PlayerInventory inv) {
        inventorySnapshot.clear();
        for (int i = 0; i < inv.size(); i++) {
            if (!inv.getStack(i).isEmpty()) {
                inventorySnapshot.put(inv.getStack(i).getItem(), inv.getStack(i).getCount());
            }
        }
    }

    // ✅ متدی که InventoryMixin به آن نیاز داشت (حل ارور cannot find symbol)
    public static void addTask(int syncSlot, int countToKeep) {
        taskQueue.add(new CursorTask(syncSlot, countToKeep));
    }
}