package net.chamosmp.miniranks.commands;

import net.chamosmp.miniranks.commands.suggestions.GroupSuggestion;
import net.chamosmp.miniranks.util.NoteMakerUtil;
import net.chamosmp.sqdlib.paper.util.ColorUtil;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

@Command("miniranks")
public class BaseCommand {
    private final NoteMakerUtil noteMakerUtil;
    private final Plugin plugin;

    public BaseCommand(NoteMakerUtil noteMakerUtil, Plugin plugin) {
        this.noteMakerUtil = noteMakerUtil;
        this.plugin = plugin;
    }

    @Subcommand("give")
    public class give {
        @Executes("normal")
        public void giveNormal(CommandSender sender, Player target, @GroupSuggestion String targetGroup) {
            target.getInventory().addItem(noteMakerUtil.createGrantNote(targetGroup));
            sender.sendMessage(ColorUtil.parse("Added a note to the target's inventory."));
        }

        @Executes("upgrade")
        public void upgrade(CommandSender sender, Player target, @GroupSuggestion String targetGroup) {
            String groupNeeded = "";
            //plugin.getConfig()
            target.getInventory().addItem(noteMakerUtil.createUpgradeNote(targetGroup, groupNeeded));
        }
    }

    @Executes("reload")
    public void reload(CommandSender sender, Player target) {
        plugin.reloadConfig();
        sender.sendMessage(ColorUtil.parse("<green>Reloaded configuration."));
    }
}