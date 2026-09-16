package net.chamosmp.miniranks.util;

import net.chamosmp.sqdlib.exceptions.PluginNotFoundException;
import net.chamosmp.sqdlib.paper.util.LoggerUtil;
import net.chamosmp.sqdlib.util.LogType;
import net.luckperms.api.LuckPerms;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;

public class LuckPermsUtil {
    private LuckPerms luckPerms;

    private final Plugin plugin;

    public LuckPermsUtil(Plugin plugin) {
        this.plugin = plugin;

        initialize();
    }

    public void initialize() {
        final RegisteredServiceProvider<LuckPerms> luckPermsProvider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
        luckPerms = (luckPermsProvider != null) ? luckPermsProvider.getProvider() : null;
        if (luckPerms == null) {
            LoggerUtil.log(LogType.SEVERE, "LuckPerms is not available! MiniRanks will not function properly.");
            throw new PluginNotFoundException("MiniRanks needs LuckPerms to function! This may (And is) an api mistake from LuckPerms");
        }
        LoggerUtil.log(LogType.INFO, "LuckPerms found!");
    }

    public @NotNull LuckPerms getLuckPerms() {
        return luckPerms;
    }
}
