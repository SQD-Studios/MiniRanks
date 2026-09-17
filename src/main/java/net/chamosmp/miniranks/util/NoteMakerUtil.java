package net.chamosmp.miniranks.util;

import net.chamosmp.sqdlib.paper.util.ColorUtil;
import net.chamosmp.sqdlib.paper.util.LanguageUtil;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.util.Tristate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@NullMarked
public class NoteMakerUtil implements Listener {
    private final Plugin plugin;
    private final LuckPermsUtil util;
    private final LanguageUtil languageUtil;

    private final NamespacedKey IS_NOTE;
    private final NamespacedKey LUCKPERMS_GROUP;
    private final NamespacedKey IS_UPGRADE;
    private final NamespacedKey UPGRADE_NEEDS_GROUP;
    private final NamespacedKey ENDS_IN;

    public NoteMakerUtil(Plugin plugin, LuckPermsUtil util, LanguageUtil languageUtil) {
        this.plugin = plugin;
        this.util = util;
        this.languageUtil = languageUtil;

        IS_NOTE = new NamespacedKey(plugin, "is_miniranks_note");
        LUCKPERMS_GROUP = new NamespacedKey(plugin, "miniranks_group");
        IS_UPGRADE = new NamespacedKey(plugin, "is_upgrade");
        UPGRADE_NEEDS_GROUP = new NamespacedKey(plugin, "upgrade_needs_group");
        ENDS_IN = new NamespacedKey(plugin, "ends_in");
    }


    public ItemStack createGrantNote(String group, int endsIn, String groupDisplayName) {
        ItemStack item = createGeneralNote(group, false, Map.of("group", groupDisplayName, "time_left", endsIn), endsIn < 0);
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(IS_UPGRADE, PersistentDataType.BOOLEAN, false);
            persistentDataContainer.set(ENDS_IN, PersistentDataType.INTEGER, endsIn);
        });
        return item;
    }

    public ItemStack createUpgradeNote(String group, String requiredGroup, String groupDisplayName, String requiredGroupDisplayName) {
        ItemStack item = createGeneralNote(group, true, Map.of("group", requiredGroupDisplayName, "target_rank", groupDisplayName), true);
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(IS_UPGRADE, PersistentDataType.BOOLEAN, true);
            persistentDataContainer.set(UPGRADE_NEEDS_GROUP, PersistentDataType.STRING, requiredGroup);
        });
        return item;
    }

    private ItemStack createGeneralNote(String group, boolean isUpgrade, Map<?, ?> placeholders, boolean isPermanent) {
        ItemStack item = new ItemStack(Material.BARRIER);
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("notes.groups." + group);
        if (section == null) {
            section = plugin.getConfig().getConfigurationSection("notes.groups.default");
            if (section == null) return item;
        }

        String materialString;
        Component customName;
        List<Component> lore = new ArrayList<>();

        if (!isUpgrade) {
            materialString = section.getString("material", "PAPER");

            if (isPermanent) {
                customName = ColorUtil.parse(
                        null,
                        section.getString("name", "<aqua>%group% <i><dark_gray>[Rank]"),
                        placeholders
                );
            } else {
                customName = ColorUtil.parse(
                        null,
                        section.getString("name-with-time-left", "<aqua>%group% <i><dark_gray>[Rank]"),
                        placeholders
                );
            }

            for (String i : section.getStringList("lore")) {
                lore.add(ColorUtil.parse(i));
            }
        } else {
            materialString = section.getString("upgrade.material", "PAPER");
            customName = ColorUtil.parse(
                    null,
                    section.getString("upgrade.name", "<aqua>%group% <i><dark_gray>[Rank]"),
                    placeholders
            );
            for (String i : section.getStringList("upgrade.lore")) {
                lore.add(ColorUtil.parse(null, i, placeholders));
            }
        }

        Material material = Material.getMaterial(materialString);
        if (material != null) {
            item = item.withType(material);
        }

        ItemMeta meta = item.getItemMeta();
        meta.customName(customName);
        item.setItemMeta(meta);

        item.editPersistentDataContainer(container -> {
            container.set(IS_NOTE, PersistentDataType.BOOLEAN, true);
            container.set(LUCKPERMS_GROUP, PersistentDataType.STRING, group);
        });

        if (lore.isEmpty()) lore = null;
        item.lore(lore);

        return item;
    }

    @EventHandler
    public void onPlayerClickedItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null) return;
        if (!isNote(item)) return;

        boolean isUpgrade = Boolean.TRUE.equals(item.getPersistentDataContainer().get(IS_UPGRADE, PersistentDataType.BOOLEAN));
        String groupString = item.getPersistentDataContainer().get(LUCKPERMS_GROUP, PersistentDataType.STRING);

        if (groupString == null) return;

        if (isUpgrade) {
            String needsGroup = item.getPersistentDataContainer().get(UPGRADE_NEEDS_GROUP, PersistentDataType.STRING);
            if (needsGroup == null) return;

            if (hasGroup(player, needsGroup)) {
                grantGroup(player, groupString, item, -1);
            } else {
                player.sendMessage(ColorUtil.parse(languageUtil.getMessage("not-have-previous-rank-for-upgrade")));
            }
        } else {
            @SuppressWarnings("all")
            int endsIn = item.getPersistentDataContainer().get(ENDS_IN, PersistentDataType.INTEGER);
            grantGroup(player, groupString, item, endsIn);
        }
    }

    public boolean isNote(ItemStack item) {
        return Boolean.TRUE.equals(item.getPersistentDataContainer().get(IS_NOTE, PersistentDataType.BOOLEAN));
    }

    private void grantGroup(Player player, String groupString, ItemStack item, int endsIn) {
        if (hasGroup(player, groupString)) {
            player.sendMessage(ColorUtil.parse(languageUtil.getMessage("already-has-group")));
            return;
        }

        Group group = util.getLuckPerms().getGroupManager().getGroup(groupString);
        if (group == null) return;
        Node node;
        if (endsIn > 0) {
            node = Node.builder("group." + groupString)
                    .value(true)
                    .expiry(endsIn, TimeUnit.DAYS)
                    .build();
        } else {
            node = Node.builder("group." + groupString)
                    .value(true)
                    .build();
        }
        util.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> user.data().add(node));
        player.sendMessage(ColorUtil.parse(languageUtil.getMessage("granted-rank")));
        item.setAmount(item.getAmount() - 1);
    }

    private boolean hasGroup(Player player, String groupString) {
        Node node = Node.builder("group." + groupString)
                .value(true)
                .build();
        return util.getLuckPerms().getPlayerAdapter(Player.class).getUser(player).data().contains(node, NodeEqualityPredicate.ONLY_KEY) == Tristate.TRUE;
    }
}
