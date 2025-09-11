package dev.doctor4t.trainmurdermystery;

import dev.doctor4t.trainmurdermystery.cca.TrainMurderMysteryComponents;
import dev.doctor4t.trainmurdermystery.cca.WorldGameComponent;
import dev.doctor4t.trainmurdermystery.command.GiveRoomKeyCommand;
import dev.doctor4t.trainmurdermystery.command.SetTrainSpeedCommand;
import dev.doctor4t.trainmurdermystery.command.StartGameCommand;
import dev.doctor4t.trainmurdermystery.index.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class TrainMurderMystery implements ModInitializer {
    public static final String MOD_ID = "trainmurdermystery";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String name) {
        return Identifier.of(MOD_ID, name);
    }

    @Override
    public void onInitialize() {
        // Registry initializers
        TrainMurderMysterySounds.initialize();
        TrainMurderMysteryEntities.initialize();
        TrainMurderMysteryBlocks.initialize();
        TrainMurderMysteryItems.initialize();
        TrainMurderMysteryBlockEntities.initialize();
        TrainMurderMysteryParticles.initialize();

        // Register commands
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {
            GiveRoomKeyCommand.register(dispatcher);
            SetTrainSpeedCommand.register(dispatcher);
            StartGameCommand.register(dispatcher);
        }));

        // Game loop tick
        ServerTickEvents.START_WORLD_TICK.register(serverWorld -> {
            WorldGameComponent game = TrainMurderMysteryComponents.GAME.get(serverWorld);
            if (game.isRunning()) {

                // check hitman win condition (all targets are dead)
                boolean hitmanWin = true;
                for (UUID targetUuid : game.getTargets()) {
                    if (!isTargetEliminated(serverWorld, targetUuid)) {
                        hitmanWin = false;
                    }
                }

                // hitman win
                if (hitmanWin) {
                    for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                        player.sendMessage(Text.translatable("game_message.win.hitman"));
                    }
                    game.setRunning(false);
                }
            }
        });
    }

    private static boolean isTargetEliminated(ServerWorld world, UUID targetUuid) {
        PlayerEntity targetPlayer = world.getPlayerByUuid(targetUuid);
        return targetPlayer == null || !targetPlayer.isAlive() || targetPlayer.isCreative() || targetPlayer.isSpectator();
    }

// TODO: Add objectives
// TODO: Add tasks
// TODO: Add temp jamming doors with lockpick
// TODO: Remove survival UI
// TODO: Lock brightness option + render distance
// TODO: Add snack cabinet
// TODO: Add drink cabinet
// TODO: Make beds poisonable
// TODO: Make cabinets poisonable
}