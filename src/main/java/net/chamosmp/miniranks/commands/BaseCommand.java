package net.chamosmp.miniranks.commands;

import net.chamosmp.miniranks.commands.suggestions.GroupSuggestion;
import net.chamosmp.miniranks.util.LuckPermsUtil;
import net.chamosmp.miniranks.util.NoteMakerUtil;
import net.chamosmp.sqdlib.paper.util.ColorUtil;
import net.luckperms.api.model.group.Group;
import net.strokkur.commands.Command;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Optional;

@Command("miniranks")
public class BaseCommand {
    private final NoteMakerUtil noteMakerUtil;
    private final Plugin plugin;
    private final LuckPermsUtil util;

    public BaseCommand(NoteMakerUtil noteMakerUtil, Plugin plugin, LuckPermsUtil util) {
        this.noteMakerUtil = noteMakerUtil;
        this.plugin = plugin;
        this.util = util;
    }

    @Subcommand("give")
    public class GiveSubcommand {
        @Executes("normal")
        public void giveNormal(CommandSender sender, Player target, @GroupSuggestion String targetGroup, Optional<Integer> amount, Optional<Integer> endsInDays) {
            Group group = util.getLuckPerms().getGroupManager().getGroup(targetGroup);
            if (group == null) {
                sender.sendMessage(ColorUtil.parse("<red>Invalid group! Please ensure it's the same on the config and luckperms"));
                return;
            }

            int am = amount.orElse(1);
            int endsIn = endsInDays.orElse(-1);

            ItemStack item = noteMakerUtil.createGrantNote(group.getFriendlyName(), endsIn);
            item.setAmount(am);
            target.getInventory().addItem(item);
            sender.sendMessage(ColorUtil.parse("<green>Added a note to the target's inventory."));
        }

        @Executes("upgrade")
        public void upgrade(CommandSender sender, Player target, @GroupSuggestion String targetGroup, Optional<Integer> amount) {
            String groupNeeded = "";
            Group group = null;
            List<String> groups = plugin.getConfig().getStringList("group-progress");
            for (int i = 0; i < groups.size(); i++) {
                if (groups.get(i).equalsIgnoreCase(targetGroup)) {
                    if (i == 0) {
                        sender.sendMessage(ColorUtil.parse(
                                "<red>This group/rank is first on the groups needed list, so it can't be upgraded from something. Please use a different target group, or make a normal note"
                        ));
                        break;
                    }
                    group = util.getLuckPerms().getGroupManager().getGroup(groups.get(i - 1));
                    if (group == null) {
                        sender.sendMessage(ColorUtil.parse("<red>Invalid group! Please ensure it's the same on the config and luckperms"));
                        return;
                    }
                    groupNeeded = group.getFriendlyName();
                    break;
                }
            }

            if (group == null) return;

            group = util.getLuckPerms().getGroupManager().getGroup(targetGroup);
            if (group == null) {
                sender.sendMessage(ColorUtil.parse("<red>Invalid group! Please ensure it's the same on the config and luckperms"));
                return;
            } else {
                targetGroup = group.getFriendlyName();
            }

            int am = amount.orElse(1);
            ItemStack item = noteMakerUtil.createUpgradeNote(targetGroup, groupNeeded);
            item.setAmount(am);
            target.getInventory().addItem(item);
        }
    }

    @Executes("reload")
    public void reload(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(ColorUtil.parse("<green>Reloaded configuration."));
    }
}