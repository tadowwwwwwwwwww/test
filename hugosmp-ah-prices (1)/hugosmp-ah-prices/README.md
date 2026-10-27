# HugoSMP AH Prices (Fabric 1.21.11, client-only)

Liest beim Oeffnen von /ah die Preise aus der Item-Lore mit, speichert pro Item den
guenstigsten Preis und zeigt ihn als Tooltip in JEDEM GUI (Inventar, Kisten, Ender Chest ...).

## Bauen
1. JDK 21 + aktuelles Gradle installieren
2. Im Ordner: `gradle wrapper` (einmalig), dann `./gradlew build`
3. `build/libs/hugosmp-ah-prices-1.0.0.jar` nach `.minecraft/mods/` (+ Fabric API)

## Einstellen
`.minecraft/config/hugosmp-ah-prices.json`
Falls keine Preise erkannt werden: `"debug": true` setzen, /ah oeffnen -> im Chat siehst du
Titel + Lore der ersten Items. Dann `ahTitleRegex` / `priceRegex` anpassen.
