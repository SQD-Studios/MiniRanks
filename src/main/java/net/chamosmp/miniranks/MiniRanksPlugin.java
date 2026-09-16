package net.chamosmp.miniranks;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.chamosmp.miniranks.commands.BaseCommandBrigadier;
import net.chamosmp.miniranks.commands.suggestions.GroupSuggestionImpl;
import net.chamosmp.miniranks.util.LuckPermsUtil;
import net.chamosmp.miniranks.util.NoteMakerUtil;
import net.chamosmp.sqdlib.paper.util.LoggerUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniRanksPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new LoggerUtil("<blue>MiniRanks</blue>| ");
        LuckPermsUtil util = new LuckPermsUtil(this);
        NoteMakerUtil noteMakerUtil = new NoteMakerUtil(this, util);

        Bukkit.getPluginManager().registerEvents(noteMakerUtil, this);

        GroupSuggestionImpl.init(this);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            BaseCommandBrigadier.register(event.registrar(), noteMakerUtil, this);
        });
    }
}
