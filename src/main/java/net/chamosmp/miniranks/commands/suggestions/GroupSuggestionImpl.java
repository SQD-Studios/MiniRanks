package net.chamosmp.miniranks.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;

import java.util.concurrent.CompletableFuture;

public class GroupSuggestionImpl {
    @GroupSuggestion
    public static CompletableFuture<Suggestions> provide(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        LuckPermsProvider.get().getGroupManager()
                .getLoadedGroups().stream()
                .map(Group::getName)
                .filter(id -> id.toLowerCase().startsWith(builder.getRemainingLowerCase()) && !id.equals("default"))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
