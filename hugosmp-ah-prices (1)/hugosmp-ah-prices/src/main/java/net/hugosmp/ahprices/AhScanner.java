package net.hugosmp.ahprices;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

/** Liest Preise aus dem geoeffneten AH-Menue (Lore der Items). */
public class AhScanner {
    private static boolean wasAh = false;
    private static int cooldown = 0;
    private static boolean debugPrinted = false;

    public static boolean serverOk(Minecraft mc) {
        String host = Config.INSTANCE.serverHost;
        if (host == null || host.isBlank()) return true;
        ServerData sd = mc.getCurrentServer();
        return sd != null && sd.ip != null && sd.ip.toLowerCase().contains(host.toLowerCase());
    }

    public static boolean isAhScreen(Minecraft mc) {
        if (!(mc.screen instanceof AbstractContainerScreen<?> s)) return false;
        if (!serverOk(mc)) return false;
        var p = Config.INSTANCE.titlePattern;
        return p == null || p.matcher(s.getTitle().getString()).find();
    }

    public static void tick(Minecraft mc) {
        boolean ah = isAhScreen(mc);
        if (ah) {
            if (!wasAh) debugPrinted = false;
            if (--cooldown <= 0) {
                cooldown = 10;
                scan(mc);
            }
        } else if (wasAh) {
            PriceStore.save();
        }
        wasAh = ah;
    }

    /** Preis (Gesamt) aus der Lore, oder null. */
    public static Double extractTotalPrice(ItemStack stack) {
        ItemLore lore = stack.get(DataComponents.LORE);
        if (lore == null) return null;
        for (Component line : lore.lines()) {
            Matcher m = Config.INSTANCE.pricePattern.matcher(line.getString());
            if (m.find()) {
                try {
                    double v = parseNumber(m.group(1));
                    if (m.groupCount() >= 2 && m.group(2) != null) {
                        switch (Character.toLowerCase(m.group(2).charAt(0))) {
                            case 'k' -> v *= 1_000;
                            case 'm' -> v *= 1_000_000;
                            case 'b' -> v *= 1_000_000_000;
                        }
                    }
                    return v;
                } catch (Exception ignored) {}
            }
        }
        return null;
    }

    private static void scan(Minecraft mc) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) mc.screen;
        Map<String, Double> seen = new HashMap<>();
        int debugCount = 0;

        for (Slot slot : screen.getMenu().slots) {
            if (slot.container instanceof Inventory) continue; // nur AH-Slots, nicht dein Inventar
            ItemStack st = slot.getItem();
            if (st.isEmpty()) continue;

            if (Config.INSTANCE.debug && !debugPrinted && debugCount < 3 && mc.player != null) {
                ItemLore lore = st.get(DataComponents.LORE);
                if (lore != null) {
                    debugCount++;
                    mc.player.displayClientMessage(Component.literal("[AHPrices] Titel: '" + screen.getTitle().getString()
                            + "' Item: " + st.getHoverName().getString()), false);
                    for (Component l : lore.lines())
                        mc.player.displayClientMessage(Component.literal("  | " + l.getString()), false);
                }
            }

            Double total = extractTotalPrice(st);
            if (total == null) continue;
            double unit = Config.INSTANCE.perUnit ? total / Math.max(1, st.getCount()) : total;
            seen.merge(PriceStore.keyOf(st), unit, Math::min);
        }
        if (debugCount > 0) debugPrinted = true;
        if (!seen.isEmpty()) PriceStore.update(seen);
    }

    static double parseNumber(String s) {
        s = s.replaceAll("[.,]+$", "");
        int lastDot = s.lastIndexOf('.'), lastComma = s.lastIndexOf(',');
        if (lastDot >= 0 && lastComma >= 0) {
            char dec = lastDot > lastComma ? '.' : ',';
            char th = dec == '.' ? ',' : '.';
            s = s.replace(String.valueOf(th), "").replace(dec, '.');
        } else if (lastDot >= 0 || lastComma >= 0) {
            char sep = lastDot >= 0 ? '.' : ',';
            int count = 0;
            for (char c : s.toCharArray()) if (c == sep) count++;
            int digitsAfter = s.length() - s.lastIndexOf(sep) - 1;
            if (count > 1 || digitsAfter == 3) s = s.replace(String.valueOf(sep), "");
            else s = s.replace(sep, '.');
        }
        return Double.parseDouble(s);
    }
}
