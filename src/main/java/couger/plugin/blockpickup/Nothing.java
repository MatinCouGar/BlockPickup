package couger.plugin.blockpickup;

import net.fabricmc.api.ClientModInitializer;
import java.lang.reflect.Method;

public class Nothing implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        System.out.println("Nothing Proxy: Attempting to load Client...");
        try {
            Class<?> clientClass = Class.forName("couger.plugin.blockpickup.client.BlockPickupClient");
            Object clientInstance = clientClass.getDeclaredConstructor().newInstance();
            Method initMethod = clientClass.getMethod("onInitializeClient");

            initMethod.invoke(clientInstance);
            System.out.println("Client loaded successfully via Reflection!");
        } catch (Exception e) {
            System.err.println("Failed to load client: " + e.getMessage());
        }
    }
}