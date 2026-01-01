package couger.plugin.blockpickup.mixin;

import couger.plugin.blockpickup.BlockPickup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class InventoryMixin {
    @Shadow @Final public PlayerEntity player;

    @Inject(method = "setStack", at = @At("RETURN"))
    private void onSetStack(int slot, ItemStack stack, CallbackInfo ci) {
        if (BlockPickup.isProcessing || !BlockPickup.isEnabled || stack.isEmpty()) return;

        if (this.player != null && this.player.getEntityWorld().isClient()) {
            if (slot < 0 || slot >= 46) return;

            try {
                BlockPickup.isProcessing = true;
                PlayerInventory inv = (PlayerInventory)(Object)this;

                int allowedLimit = BlockPickup.inventorySnapshot.getOrDefault(stack.getItem(), 0);

                int currentTotal = 0;
                for (int i = 0; i < 46; i++) {
                    if (inv.getStack(i).getItem().equals(stack.getItem())) {
                        currentTotal += inv.getStack(i).getCount();
                    }
                }

                if (currentTotal > allowedLimit) {
                    int extra = currentTotal - allowedLimit;
                    int countToKeep = Math.max(0, stack.getCount() - extra);
                    int syncSlot = (slot < 9) ? slot + 36 : slot;
                    BlockPickup.addTask(syncSlot, countToKeep);
                }
            } finally {
                BlockPickup.isProcessing = false;
            }
        }
    }
}