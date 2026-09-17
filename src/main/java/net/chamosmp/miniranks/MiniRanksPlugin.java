package net.chamosmp.miniranks;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.chamosmp.miniranks.commands.BaseCommandBrigadier;
import net.chamosmp.miniranks.util.LuckPermsUtil;
import net.chamosmp.miniranks.util.NoteMakerUtil;
import net.chamosmp.sqdlib.exceptions.CommandRegisterException;
import net.chamosmp.sqdlib.paper.util.ConfigUtil;
import net.chamosmp.sqdlib.paper.util.LanguageUtil;
import net.chamosmp.sqdlib.paper.util.LoggerUtil;
import net.chamosmp.sqdlib.util.LogType;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MiniRanksPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        new LoggerUtil("<blue>MiniRanks</blue>| ");

        ConfigUtil.loadDataFile(this, "config.yml");


        LuckPermsUtil util = new LuckPermsUtil(this);
        LanguageUtil languageUtil = new LanguageUtil(this);
        NoteMakerUtil noteMakerUtil = new NoteMakerUtil(this, util, languageUtil);

        Bukkit.getPluginManager().registerEvents(noteMakerUtil, this);


        try {
            this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                BaseCommandBrigadier.register(event.registrar(), noteMakerUtil, this, util);
                LoggerUtil.log(LogType.INFO, "Successfully registered commands");
            });
        } catch (Exception e) {
            throw new CommandRegisterException("Failed to register commands!", e);
        }
    }
}
