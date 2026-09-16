package net.chamosmp.miniranks.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public class GroupSuggestionImpl {
    private static Plugin plugin;

    public static void init(Plugin manager) {
        plugin = manager;
    }

    @GroupSuggestion
    public static CompletableFuture<Suggestions> provide(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        if (plugin == null) return builder.buildFuture();
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("notes.groups");
        if (section == null) return builder.buildFuture();
        section.getKeys(false).stream()
                .map(String::toLowerCase)
                .filter(id -> id.toLowerCase().startsWith(builder.getRemainingLowerCase()) && !id.equals("default"))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
