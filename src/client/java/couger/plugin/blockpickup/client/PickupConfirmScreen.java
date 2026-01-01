package couger.plugin.blockpickup.client;

import couger.plugin.blockpickup.BlockPickup;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import java.util.ArrayList;
import java.util.List;

public class PickupConfirmScreen extends Screen {
    private final List<ItemStack> items = new ArrayList<>();

    public PickupConfirmScreen() {
        super(Text.of("Inventory Guard"));
        if (MinecraftClient.getInstance().player != null) {
            var inv = MinecraftClient.getInstance().player.getInventory();
            for (int i = 0; i < 46; i++) {
                if (!inv.getStack(i).isEmpty()) {
                    items.add(inv.getStack(i).copy());
                }
            }
        }
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(Text.of("§a✅ Save & Start"), (button) -> {
            if (client != null && client.player != null) {
                BlockPickup.takeInventorySnapshot(client.player.getInventory());
                BlockPickup.isEnabled = true;
                client.player.sendMessage(Text.literal("§aPickup Blocking: ON"), true);
                this.close();
            }
        }).dimensions(this.width / 2 - 125, this.height - 40, 120, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("§c❌ Cancel"), (button) -> this.close())
                .dimensions(this.width / 2 + 5, this.height - 40, 120, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "§6§lProtect current items?", this.width / 2, 20, 0xFFFFFF);

        int startX = this.width / 2 - 90;
        int startY = 50;
        int col = 0;
        int row = 0;

        for (ItemStack stack : items) {
            int dx = startX + (col * 20);
            int dy = startY + (row * 20);
            context.drawItem(stack, dx, dy);
            context.drawStackOverlay(this.textRenderer, stack, dx, dy);
            col++;
            if (col >= 9) { col = 0; row++; }
        }
    }
}