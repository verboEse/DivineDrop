package com.demkom58.divinedrop;

import com.demkom58.divinedrop.config.ConfigData;
import com.demkom58.divinedrop.versions.VersionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DivineCommandHandler implements CommandExecutor {
    private final DivineDrop plugin;
    private final VersionManager versionManager;
    private final ConfigData data;

    public DivineCommandHandler(@NotNull final DivineDrop plugin,
                                @NotNull final VersionManager versionManager,
                                @NotNull final ConfigData data) {
        this.plugin = plugin;
        this.versionManager = versionManager;
        this.data = data;
    }

    @Override
    public boolean onCommand(@NotNull final CommandSender sender,
                             @NotNull final Command command,
                             @NotNull final String label,
                             @NotNull final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(StaticData.INFO);
            return true;
        }

        final String subCommand = args[0];
        if (subCommand.equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("divinedrop.reload")) {
                sendMessage(sender, data.getNoPermissionMessage());
                return false;
            }

            plugin.reloadPlugin(versionManager.getVersion());
            sendMessage(sender, data.getReloadedMessage());
            return true;
        }

        if (subCommand.equalsIgnoreCase("getName")) {
            if (sender.hasPermission("divinedrop.getname")) {
                final Player player = (Player) sender;
                String itemName;

                try {
                    if (plugin.getServer().getVersion().contains("1.8"))
                        itemName = player.getItemInHand().getItemMeta().getDisplayName();
                    else
                        itemName = player.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
                } catch (NullPointerException ex) {
                    sendMessage(sender, data.getItemDisplayNameMessage().replace("%name%", "AIR"));
                    return false;
                }

                if (itemName == null)
                    itemName = "NONAME";

                sendMessage(sender, data.getItemDisplayNameMessage().replace("%name%", itemName.replace('§', '&')));
                return true;
            }
            return true;
        }

        if (subCommand.equalsIgnoreCase("size")) {
            if (sender.hasPermission("divinedrop.developer"))
                sendMessage(sender, "Items to remove: " + ItemsHandler.PROCESSING_ITEMS.size());
            return true;
        }

        sendMessage(sender, data.getUnknownCmdMessage());
        return true;
    }

    private void sendMessage(@NotNull final CommandSender player, @NotNull final String message) {
        player.sendMessage(StaticData.PREFIX + message);
    }

}
