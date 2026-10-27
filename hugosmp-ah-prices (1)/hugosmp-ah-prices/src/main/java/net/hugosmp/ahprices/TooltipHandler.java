package net.hugosmp.ahprices;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;

public class TooltipHandler {
    public static void onTooltip(ItemStack stack, Item.TooltipContext ctx, TooltipFlag flag, List<Component> lines) {
        if (stack.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (!AhScanner.serverOk(mc)) return;
        // AH-Eintraege selbst haben schon eine Preiszeile -> nichts doppelt anzeigen
        if (AhScanner.extractTotalPrice(stack) != null) return;

        PriceStore.Entry e = PriceStore.get(PriceStore.keyOf(stack));
        if (e == null) return;

        long mins = Math.max(0, (System.currentTimeMillis() - e.time) / 60_000L);
        String age = mins < 60 ? mins + " Min" : (mins / 60) + " Std";

        lines.add(Component.literal("AH: ").withStyle(ChatFormatting.GOLD)
                .append(Component.literal("$" + fmt(e.price) + " / Stk").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal("  (vor " + age + ")").withStyle(ChatFormatting.DARK_GRAY)));

        if (Config.INSTANCE.showTotal && stack.getCount() > 1) {
            lines.add(Component.literal("AH x" + stack.getCount() + ": ").withStyle(ChatFormatting.GOLD)
                    .append(Component.literal("$" + fmt(e.price * stack.getCount())).withStyle(ChatFormatting.GREEN)));
        }
    }

    static String fmt(double v) {
        String suffix = "";
        if (v >= 1e9) { v /= 1e9; suffix = "B"; }
        else if (v >= 1e6) { v /= 1e6; suffix = "M"; }
        else if (v >= 1e3) { v /= 1e3; suffix = "K"; }
        String s = String.format(Locale.ROOT, "%.2f", v);
        if (s.contains(".")) s = s.replaceAll("0+$", "").replaceAll("\\.$", "");
        return s + suffix;
    }
}
