package gg.kasai.emojiplugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final EmojiPlugin plugin;

    public ChatListener(EmojiPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChat(AsyncPlayerChatEvent event) {
        String permission = plugin.getConfig().getString("use-permission", "emojiplugin.use");

        if (permission != null && !permission.isEmpty()) {
            if (!event.getPlayer().hasPermission(permission)) {
                return;
            }
        }

        String message = event.getMessage();

        // Only process if message contains a colon (potential emoji)
        if (message.contains(":")) {
            String replaced = plugin.replaceEmojis(message);
            if (!replaced.equals(message)) {
                event.setMessage(replaced);
            }
        }
    }
}
