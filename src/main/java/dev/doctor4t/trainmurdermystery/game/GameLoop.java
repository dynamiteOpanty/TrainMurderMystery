package dev.doctor4t.trainmurdermystery.game;

import dev.doctor4t.trainmurdermystery.cca.TrainMurderMysteryComponents;
import dev.doctor4t.trainmurdermystery.cca.WorldGameComponent;
import dev.doctor4t.trainmurdermystery.index.TrainMurderMysteryItems;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class GameLoop {
    public static void tick(ServerWorld serverWorld) {
        WorldGameComponent game = TrainMurderMysteryComponents.GAME.get(serverWorld);
        if (game.isRunning()) {
            // check hitman win condition (all targets are dead)
            WinStatus winStatus = WinStatus.HITMEN;
            for (ServerPlayerEntity player : game.getTargets()) {
                if (!isPlayerEliminated(serverWorld, player)) {
                    winStatus = WinStatus.NONE;
                }
            }

            // check passenger win condition (all hitmen are dead)
            if (winStatus == WinStatus.NONE) {
                winStatus = WinStatus.PASSENGERS;
                for (ServerPlayerEntity player : game.getHitmen()) {
                    if (!isPlayerEliminated(serverWorld, player)) {
                        winStatus = WinStatus.NONE;
                    }
                }
            }

            // win display
            if (winStatus != WinStatus.NONE) {
                for (ServerPlayerEntity player : serverWorld.getPlayers()) {
                    player.sendMessage(Text.translatable("game.win." + winStatus.name().toLowerCase(Locale.ROOT)), true);
                    System.out.println("game.win." + winStatus.name().toLowerCase(Locale.ROOT));
                }
                game.setRunning(false);
            }
        }
    }

    public static void startGame(ServerWorld world) {
        TrainMurderMysteryComponents.TRAIN.get(world).setTrainSpeed(130);
        WorldGameComponent gameComponent = TrainMurderMysteryComponents.GAME.get(world);

        List<ServerPlayerEntity> fullPlayerPool = new ArrayList<>(world.getPlayers().stream().filter(serverPlayerEntity -> !serverPlayerEntity.isInCreativeMode() && !serverPlayerEntity.isSpectator()).toList());
        List<ServerPlayerEntity> rolePlayerPool = new ArrayList<>(fullPlayerPool);

        // clear items, clear previous game data
        for (ServerPlayerEntity serverPlayerEntity : rolePlayerPool) {
            serverPlayerEntity.getInventory().clear();
        }
        gameComponent.resetLists();

        // select hitmen
        int hitmanCount = (int) Math.floor(rolePlayerPool.size() * .2f);
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < hitmanCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            player.giveItemStack(new ItemStack(TrainMurderMysteryItems.KNIFE));
            gameComponent.addHitman(player);
            rolePlayerPool.remove(player);
        }

        // select detectives
        int detectiveCount = hitmanCount;
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < detectiveCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            player.giveItemStack(new ItemStack(TrainMurderMysteryItems.REVOLVER));
            gameComponent.addDetective(player);
            rolePlayerPool.remove(player);
        }

        // select targets
        int targetCount = rolePlayerPool.size() / 2;
        Collections.shuffle(rolePlayerPool);
        for (int i = 0; i < targetCount; i++) {
            ServerPlayerEntity player = rolePlayerPool.getFirst();
            player.giveItemStack(new ItemStack(TrainMurderMysteryItems.ROOM_KEY));
            gameComponent.addTarget(player);
            rolePlayerPool.remove(player);
        }

        gameComponent.setRunning(true);
    }

    private static boolean isPlayerEliminated(ServerWorld world, ServerPlayerEntity player) {
        return player == null || !player.isAlive() || player.isCreative() || player.isSpectator();
    }

    public enum WinStatus {
        NONE, HITMEN, PASSENGERS
    }
}
