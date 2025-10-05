package dev.doctor4t.trainmurdermystery.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.doctor4t.trainmurdermystery.cca.ScoreboardRoleSelectorComponent;
import dev.doctor4t.trainmurdermystery.index.TMMItems;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class ForceRoleCommand {
    public static void register(@NotNull CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("tmm:forceRole").requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("killer").then(CommandManager.argument("players", EntityArgumentType.players()))
                        .executes(context -> forceKiller(context.getSource(), EntityArgumentType.getPlayers(context, "players")))
                ).then(CommandManager.literal("vigilante").then(CommandManager.argument("players", EntityArgumentType.players()))
                        .executes(context -> forceVigilante(context.getSource(), EntityArgumentType.getPlayers(context, "players")))
                )
        );
    }

    private static int forceKiller(@NotNull ServerCommandSource source, @NotNull Collection<ServerPlayerEntity> players) {
        var component = ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
        component.forcedKillers.clear();
        for (var player : players) component.forcedKillers.add(player.getUuid());
        return 1;
    }

    private static int forceVigilante(@NotNull ServerCommandSource source, @NotNull Collection<ServerPlayerEntity> players) {
        var component = ScoreboardRoleSelectorComponent.KEY.get(source.getServer().getScoreboard());
        component.forcedVigilantes.clear();
        for (var player : players) component.forcedVigilantes.add(player.getUuid());
        return 1;
    }
}