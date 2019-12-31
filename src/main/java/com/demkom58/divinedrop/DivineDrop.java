package com.demkom58.divinedrop;

import com.demkom58.divinedrop.config.Config;
import com.demkom58.divinedrop.config.StaticData;
import com.demkom58.divinedrop.drop.ItemHandler;
import com.demkom58.divinedrop.lang.LangManager;
import com.demkom58.divinedrop.util.Metrics;
import com.demkom58.divinedrop.util.WebSpigot;
import com.demkom58.divinedrop.version.Version;
import com.demkom58.divinedrop.version.VersionManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Logger;

@Getter
public final class DivineDrop extends JavaPlugin {

    private final Metrics metrics = new Metrics(this);
    private final WebSpigot webSpigot = new WebSpigot(this, getDescription().getVersion(), StaticData.RESOURCE_ID);

    private final VersionManager versionManager = new VersionManager(this);
    private final Config configuration = new Config("config", this, versionManager, 1);
    private final LangManager langManager = new LangManager(this, configuration.getConfigData());
    private final ItemHandler itemHandler = new ItemHandler(this, versionManager, configuration.getConfigData());

    @Override
    public void onEnable() {
        try {
            metrics.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            versionManager.setup(configuration.getConfigData(), itemHandler);
        } catch (UnsupportedOperationException e) {
            Bukkit.getConsoleSender().sendMessage("[" + getDescription().getName() + "] " + ChatColor.RED + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        final Version version = versionManager.getVersion();

        reloadPlugin(version);

        final PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(version.createListener(), this);
        pluginManager.registerEvents(new CommonListener(this), this);

        Optional.ofNullable(getCommand("divinedrop"))
                .ifPresent(cmd -> cmd.setExecutor(
                        new DivineCommandHandler(this, versionManager, configuration.getConfigData())
                ));

        if (configuration.getConfigData().isCheckUpdates())
            webSpigot.ifOutdated((latestVersion) -> {
                final Logger logger = Bukkit.getLogger();
                logger.info("New version '" + latestVersion + "' detected.");
                logger.info("Please update it on: " + webSpigot.getResourceLink());
            }, false);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        itemHandler.disable();
    }

    public void reloadPlugin(@NotNull final Version version) {
        loadConfig(version);
        itemHandler.reload();
    }

    public void loadConfig(@NotNull final Version version) {
        configuration.load();
        langManager.manageLang(configuration.getConfigData().getLang(), version);
    }

}
