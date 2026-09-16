package net.chamosmp.miniranks.util;

import net.chamosmp.sqdlib.paper.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.node.NodeEqualityPredicate;
import net.luckperms.api.node.types.PermissionNode;
import net.luckperms.api.util.Tristate;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class NoteMakerUtil implements Listener {
    private final Plugin plugin;
    private final LuckPermsUtil util;

    private final NamespacedKey IS_NOTE;
    private final NamespacedKey LUCKPERMS_GROUP;
    private final NamespacedKey IS_UPGRADE;
    private final NamespacedKey UPGRADE_NEEDS_GROUP;

    public NoteMakerUtil(Plugin plugin, LuckPermsUtil util) {
        this.plugin = plugin;
        this.util = util;

        IS_NOTE = new NamespacedKey(plugin, "is_miniranks_note");
        LUCKPERMS_GROUP = new NamespacedKey(plugin, "miniranks_group");
        IS_UPGRADE = new NamespacedKey(plugin, "is_upgrade");
        UPGRADE_NEEDS_GROUP = new NamespacedKey(plugin, "upgrade_needs_group");
    }


    public ItemStack createGrantNote(String group) {
        ItemStack item = createGeneralNote(group, false, Map.of("group", group));
        item.editPersistentDataContainer(persistentDataContainer -> persistentDataContainer.set(IS_UPGRADE, PersistentDataType.BOOLEAN, false));
        return item;
    }

    public ItemStack createUpgradeNote(String group, String requiredGroup) {
        ItemStack item = createGeneralNote(group, true, Map.of("group", group));
        item.editPersistentDataContainer(persistentDataContainer -> {
            persistentDataContainer.set(IS_UPGRADE, PersistentDataType.BOOLEAN, true);
            persistentDataContainer.set(UPGRADE_NEEDS_GROUP, PersistentDataType.STRING, requiredGroup);
        });
        return item;
    }

    private ItemStack createGeneralNote(String group, boolean isUpgrade, Map<?, ?> placeholders) {
        AtomicReference<ItemStack> item = new AtomicReference<>(new ItemStack(Material.PAPER));
        plugin.getConfig().getConfigurationSection("notes.groups").getKeys(false).forEach(key -> {
            if (key.equals(group)) {
                String materialString;
                Component customName;
                List<Component> lore = new ArrayList<>();

                if (!isUpgrade) {
                    materialString = plugin.getConfig().getString("notes.groups." + key + ".material", "PAPER");
                    customName = ColorUtil.parse(
                            null,
                            plugin.getConfig().getString("notes.groups." + key + ".name", "<aqua>%group% <i><dark_gray>[Rank]"),
                            Map.of("group", group)
                    );

                    for (String i : plugin.getConfig().getStringList("notes.groups." + key + ".lore")) {
                        lore.add(ColorUtil.parse(i));
                    }
                } else {
                    materialString = plugin.getConfig().getString("notes.groups." + key + ".upgrade.material", "PAPER");
                    customName = ColorUtil.parse(
                            null,
                            plugin.getConfig().getString("notes.groups." + key + ".upgrade.name", "<aqua>%group% <i><dark_gray>[Rank]"),
                            placeholders
                    );
                    for (String i : plugin.getConfig().getStringList("notes.groups." + key + ".upgrade.lore")) {
                        lore.add(ColorUtil.parse(i));
                    }
                }

                Material material = Material.getMaterial(materialString);
                ItemStack itemStack = item.get();
                if (material != null) {
                    itemStack = item.get().withType(material);
                }

                ItemMeta meta = itemStack.getItemMeta();
                meta.customName(customName);
                itemStack.setItemMeta(meta);

                itemStack.editPersistentDataContainer(container -> {
                    container.set(IS_NOTE, PersistentDataType.BOOLEAN, true);
                    container.set(LUCKPERMS_GROUP, PersistentDataType.STRING, group);
                });

                if (lore.isEmpty()) lore = null;
                itemStack.lore(lore);

                item.set(itemStack);
            }
        });
        return item.get();
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
                grantGroup(player, groupString);
            }
        } else {
            grantGroup(player, groupString);
        }
    }

    public boolean isNote(ItemStack item) {
        return Boolean.TRUE.equals(item.getPersistentDataContainer().get(IS_NOTE, PersistentDataType.BOOLEAN));
    }

    private void grantGroup(Player player, String groupString) {
        if (hasGroup(player, groupString)) {
            return;
        }

        Group group = util.getLuckPerms().getGroupManager().getGroup(groupString);
        if (group == null) return;
        PermissionNode node = PermissionNode.builder("group." + groupString)
                .value(true)
                .build();
        util.getLuckPerms().getUserManager().modifyUser(player.getUniqueId(), user -> {
            user.data().add(node);
        });
    }

    private boolean hasGroup(Player player, String groupString) {
        PermissionNode node = PermissionNode.builder("group." + groupString)
                .value(true)
                .build();
        return util.getLuckPerms().getUserManager().getUser(player.getUniqueId()).data().contains(node, NodeEqualityPredicate.ONLY_KEY) == Tristate.TRUE;
    }
}
