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
        if (BlockPickup.isEnabled && !stack.isEmpty() && this.player != null && this.player.getEntityWorld().isClient()) {

            var allowedLimit = BlockPickup.inventorySnapshot.getOrDefault(stack.getItem(), 0);
            PlayerInventory inv = (PlayerInventory)(Object)this;

            int currentInOtherSlots = 0;
            for (int i = 0; i < inv.size(); i++) {
                if (i != slot && inv.getStack(i).getItem().equals(stack.getItem())) {
                    currentInOtherSlots += inv.getStack(i).getCount();
                }
            }

            // چک کردن اینکه آیا این آیتم جدید (تیر یا کمان) اضافی است؟
            if (currentInOtherSlots + stack.getCount() > allowedLimit) {
                int countToKeep = Math.max(0, allowedLimit - currentInOtherSlots);
                int syncSlot = (slot < 9) ? slot + 36 : slot;

                // اضافه کردن به صف کرسر
                BlockPickup.addTask(syncSlot, countToKeep);
            }
        }
    }
}