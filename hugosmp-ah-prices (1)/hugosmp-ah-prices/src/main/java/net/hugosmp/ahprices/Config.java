package net.hugosmp.ahprices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/** Config: .minecraft/config/hugosmp-ah-prices.json */
public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hugosmp-ah-prices.json");

    /** Mod nur auf Servern aktiv, deren Adresse das hier enthaelt. Leer = ueberall. */
    public String serverHost = "hugosmp.net";
    /** Regex fuer den Titel des AH-Menues (Gross/Klein egal). Leer = jedes Container-GUI. */
    public String ahTitleRegex = "(?i)(auction|auktion|\\bah\\b|markt|market)";
    /** Regex fuer die Preis-Zeile im Lore. Gruppe 1 = Zahl, Gruppe 2 = optionales k/m/b. */
    public String priceRegex = "(?i)(?:(?:preis|price|kosten|cost)\\s*[:\\-]?\\s*\\$?|\\$)\\s*([0-9][0-9.,]*)\\s*([kmb])?";
    /** true = Listenpreis ist Gesamtpreis des Stacks und wird durch die Anzahl geteilt. */
    public boolean perUnit = true;
    /** Nach so vielen Minuten darf ein alter (guenstigerer) Preis ersetzt werden. */
    public int ttlMinutes = 30;
    /** Zusaetzlich Preis fuer den ganzen Stack anzeigen. */
    public boolean showTotal = true;
    /** Gibt beim Oeffnen des AH die Lore der ersten Items im Chat aus, um die Regex einzustellen. */
    public boolean debug = false;

    public transient Pattern titlePattern;
    public transient Pattern pricePattern;

    public static Config INSTANCE = new Config();

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Config c = GSON.fromJson(Files.readString(FILE), Config.class);
                if (c != null) INSTANCE = c;
            }
            Files.writeString(FILE, GSON.toJson(INSTANCE));
        } catch (Exception e) {
            HugoAhPrices.LOG.error("Config konnte nicht geladen werden", e);
        }
        compile();
    }

    private static void compile() {
        Config c = INSTANCE;
        try {
            c.titlePattern = (c.ahTitleRegex == null || c.ahTitleRegex.isBlank()) ? null : Pattern.compile(c.ahTitleRegex);
        } catch (Exception e) {
            HugoAhPrices.LOG.error("ahTitleRegex ungueltig", e);
        }
        try {
            c.pricePattern = Pattern.compile(c.priceRegex);
        } catch (Exception e) {
            HugoAhPrices.LOG.error("priceRegex ungueltig, nutze Default", e);
            c.pricePattern = Pattern.compile(new Config().priceRegex);
        }
    }
}
