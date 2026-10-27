package net.hugosmp.ahprices;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HugoAhPrices implements ClientModInitializer {
    public static final Logger LOG = LoggerFactory.getLogger("hugosmp-ah-prices");

    @Override
    public void onInitializeClient() {
        Config.load();
        PriceStore.load();
        ClientTickEvents.END_CLIENT_TICK.register(AhScanner::tick);
        ItemTooltipCallback.EVENT.register(TooltipHandler::onTooltip);
        ClientLifecycleEvents.CLIENT_STOPPING.register(c -> PriceStore.save());
    }
}
