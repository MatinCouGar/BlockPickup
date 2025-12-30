package couger.plugin.blockpickup.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import org.lwjgl.glfw.GLFW;

public class BlockPickupClient implements ClientModInitializer {

    // ✅ Category اختصاصی واقعی (API جدید)
    public static final KeyBinding.Category BLOCKPICKUP_CATEGORY =
            KeyBinding.Category.create(Identifier.of("block_pickup", "main"));

    public static KeyBinding toggleKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        "key.blockpickup.toggle",
                        InputUtil.Type.KEYSYM,
                        GLFW.GLFW_KEY_P,
                        BLOCKPICKUP_CATEGORY
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                System.out.println("BlockPickup toggled");
            }
        });
    }
}
