package couger.plugin.blockpickup;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BlockPickup implements ModInitializer {
    public static boolean isEnabled = false;
    public static final HashMap<Item, Integer> inventorySnapshot = new HashMap<>();
    public static final ConcurrentLinkedQueue<CursorTask> taskQueue = new ConcurrentLinkedQueue<>();

    public record CursorTask(int syncSlot, int countToKeep) {}

    @Override
    public void onInitialize() {
    }

    public static void takeInventorySnapshot(PlayerInventory inv) {
        inventorySnapshot.clear();
        for (int i = 0; i < inv.size(); i++) {
            var stack = inv.getStack(i);
            if (!stack.isEmpty()) {
                inventorySnapshot.put(stack.getItem(), stack.getCount());
            }
        }
    }

    public static void addTask(int syncSlot, int countToKeep) {
        taskQueue.add(new CursorTask(syncSlot, countToKeep));
    }
}