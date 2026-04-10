package gg.kasai.emojiplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class EmojiCommand implements CommandExecutor, TabCompleter {

    private final EmojiPlugin plugin;

    public EmojiCommand(EmojiPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "list":
                sendList(sender, args);
                break;
            case "reload":
                if (!sender.hasPermission("emojiplugin.admin")) {
                    sender.sendMessage(color("&cYou don't have permission to reload!"));
                    return true;
                }
                plugin.loadEmojis();
                String msg = plugin.getConfig().getString("messages.reload-success",
                        "&aEmoji config reloaded! &7(%s emojis loaded)");
                sender.sendMessage(color(String.format(msg, plugin.getEmojiMap().size())));
                break;
            default:
                String unknown = plugin.getConfig().getString("messages.unknown-command",
                        "&cUnknown command! Use /emojis help");
                sender.sendMessage(color(unknown));
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(color("&8&m------------------------------------"));
        sender.sendMessage(color("  &6✦ &eEmojiPlugin &6✦  &7v1.0.0"));
        sender.sendMessage(color("&8&m------------------------------------"));
        sender.sendMessage(color(" &e/emojis help &7- Show this help menu"));
        sender.sendMessage(color(" &e/emojis list &7- Browse all emojis"));
        sender.sendMessage(color(" &e/emojis list <page> &7- Go to page"));
        sender.sendMessage(color(" &e/emojis reload &7- Reload config &c(Admin)"));
        sender.sendMessage(color("&8&m------------------------------------"));
        sender.sendMessage(color(" &7In chat, type &e:emoji_name: &7to use!"));
        sender.sendMessage(color(" &7Example: &fI love this server &e:heart:&f!"));
        sender.sendMessage(color("&8&m------------------------------------"));
    }

    private void sendList(CommandSender sender, String[] args) {
        Map<String, String> emojis = plugin.getEmojiMap();
        int perPage = plugin.getConfig().getInt("list-per-page", 10);

        int page = 1;
        if (args.length >= 2) {
            try {
                page = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(color("&cInvalid page number!"));
                return;
            }
        }

        List<Map.Entry<String, String>> emojiList = new ArrayList<>(emojis.entrySet());
        int totalPages = (int) Math.ceil((double) emojiList.size() / perPage);

        if (page < 1 || page > totalPages) {
            sender.sendMessage(color("&cPage " + page + " doesn't exist! (1-" + totalPages + ")"));
            return;
        }

        int start = (page - 1) * perPage;
        int end = Math.min(start + perPage, emojiList.size());

        sender.sendMessage(color("&8&m------------------------------------"));
        sender.sendMessage(color("  &6✦ &eEmoji List &7(Page " + page + "/" + totalPages + ") &6✦"));
        sender.sendMessage(color("&8&m------------------------------------"));

        for (int i = start; i < end; i++) {
            Map.Entry<String, String> entry = emojiList.get(i);
            sender.sendMessage(color("  &e:" + entry.getKey() + ": &7→ " + entry.getValue() + " &8| &7Type &f:&e" + entry.getKey() + "&f:"));
        }

        sender.sendMessage(color("&8&m------------------------------------"));
        if (page < totalPages) {
            sender.sendMessage(color("  &7Next page: &e/emojis list " + (page + 1)));
        }
        sender.sendMessage(color("  &7Total emojis: &e" + emojiList.size()));
        sender.sendMessage(color("&8&m------------------------------------"));
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> subs = Arrays.asList("help", "list", "reload");
            List<String> completions = new ArrayList<>();
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            return completions;
        }
        return new ArrayList<>();
    }
}
