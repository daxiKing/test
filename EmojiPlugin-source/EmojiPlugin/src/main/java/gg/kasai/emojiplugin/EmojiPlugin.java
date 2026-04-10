package gg.kasai.emojiplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EmojiPlugin extends JavaPlugin {

    private static EmojiPlugin instance;
    private final Map<String, String> emojiMap = new HashMap<>();
    private Logger log;

    @Override
    public void onEnable() {
        instance = this;
        log = getLogger();

        saveDefaultConfig();

        // Create emojis folder
        File emojiFolder = new File(getDataFolder(), "emojis");
        if (!emojiFolder.exists()) {
            emojiFolder.mkdirs();
            log.info("Created emojis/ folder — drop your PNG images here!");
            log.info("Each image filename (without .png) becomes the emoji name.");
            log.info("Example: star.png -> use :star: in chat");
        }

        loadEmojis();

        // Register command
        EmojiCommand emojiCommand = new EmojiCommand(this);
        getCommand("emojis").setExecutor(emojiCommand);
        getCommand("emojis").setTabCompleter(emojiCommand);

        // Register chat listener
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);

        log.info("EmojiPlugin enabled! Loaded " + emojiMap.size() + " emojis.");
        log.info("Players use :emoji_name: in chat. Try :star: or :heart:");
    }

    @Override
    public void onDisable() {
        log.info("EmojiPlugin disabled.");
    }

    public void loadEmojis() {
        emojiMap.clear();
        reloadConfig();

        // Load from config.yml emojis section
        if (getConfig().getConfigurationSection("emojis") != null) {
            for (String key : getConfig().getConfigurationSection("emojis").getKeys(false)) {
                String value = getConfig().getString("emojis." + key);
                if (value != null && !value.isEmpty()) {
                    emojiMap.put(key.toLowerCase(), value);
                }
            }
        }

        // Load custom emojis from emojis/ folder
        // Each PNG file = one emoji slot with auto-assigned unicode
        File emojiFolder = new File(getDataFolder(), "emojis");
        if (emojiFolder.exists()) {
            File[] files = emojiFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".gif"));
            if (files != null) {
                // Start at Unicode Private Use Area E000
                int unicodeStart = 0xE000;
                for (File file : files) {
                    String name = file.getName()
                            .replaceAll("(?i)\\.png$", "")
                            .replaceAll("(?i)\\.gif$", "")
                            .toLowerCase()
                            .replace(" ", "_");
                    // Only add if not already defined in config
                    if (!emojiMap.containsKey(name)) {
                        // Use the private use area character as placeholder
                        // (requires resource pack to actually display as image)
                        String unicodeChar = String.valueOf((char) unicodeStart);
                        emojiMap.put(name, unicodeChar);
                        unicodeStart++;
                    }
                }
            }
        }

        log.info("Loaded " + emojiMap.size() + " emojis.");
    }

    public Map<String, String> getEmojiMap() {
        return emojiMap;
    }

    public static EmojiPlugin getInstance() {
        return instance;
    }

    /**
     * Replace all :emoji_name: occurrences in a string with their emoji characters.
     */
    public String replaceEmojis(String message) {
        StringBuilder result = new StringBuilder(message);
        int startIndex = 0;

        while (true) {
            int colonStart = result.indexOf(":", startIndex);
            if (colonStart == -1) break;

            int colonEnd = result.indexOf(":", colonStart + 1);
            if (colonEnd == -1) break;

            String emojiName = result.substring(colonStart + 1, colonEnd).toLowerCase();

            if (!emojiName.isEmpty() && emojiMap.containsKey(emojiName)) {
                String replacement = emojiMap.get(emojiName);
                result.replace(colonStart, colonEnd + 1, replacement);
                startIndex = colonStart + replacement.length();
            } else {
                startIndex = colonEnd;
            }
        }

        return result.toString();
    }
}
