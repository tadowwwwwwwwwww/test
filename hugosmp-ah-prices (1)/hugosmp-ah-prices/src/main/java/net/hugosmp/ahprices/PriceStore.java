package net.hugosmp.ahprices;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class PriceStore {
    public static class Entry {
        public double price; // pro Stueck
        public long time;
        public Entry(double price, long time) { this.price = price; this.time = time; }
    }

    private static final Gson GSON = new Gson();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hugosmp-ah-prices-data.json");
    private static Map<String, Entry> map = new HashMap<>();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Map<String, Entry> m = GSON.fromJson(Files.readString(FILE), new TypeToken<Map<String, Entry>>() {}.getType());
                if (m != null) map = m;
            }
        } catch (Exception e) {
            HugoAhPrices.LOG.error("Preisdaten konnten nicht geladen werden", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(map));
        } catch (Exception e) {
            HugoAhPrices.LOG.error("Preisdaten konnten nicht gespeichert werden", e);
        }
    }

    public static Entry get(String key) { return map.get(key); }

    public static void update(Map<String, Double> seen) {
        long now = System.currentTimeMillis();
        long ttl = Config.INSTANCE.ttlMinutes * 60_000L;
        for (Map.Entry<String, Double> e : seen.entrySet()) {
            Entry old = map.get(e.getKey());
            double v = e.getValue();
            if (old == null || v < old.price || now - old.time > ttl) {
                map.put(e.getKey(), new Entry(v, now));
            } else if (Math.abs(v - old.price) < 1e-9) {
                old.time = now;
            }
        }
    }

    /** Item-ID + Verzauberungen (damit z.B. Mending-Buch != Schutz-Buch). */
    public static String keyOf(ItemStack stack) {
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        List<String> ench = new ArrayList<>();
        collect(ench, stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY));
        collect(ench, stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY));
        if (ench.isEmpty()) return id;
        Collections.sort(ench);
        return id + "|" + String.join(",", ench);
    }

    private static void collect(List<String> out, ItemEnchantments e) {
        for (var entry : e.entrySet()) {
            out.add(entry.getKey().getRegisteredName() + ":" + entry.getIntValue());
        }
    }
}
