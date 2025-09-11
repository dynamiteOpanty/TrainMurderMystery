package dev.doctor4t.trainmurdermystery.cca;

import dev.doctor4t.trainmurdermystery.index.TrainMurderMysteryItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorldGameComponent implements AutoSyncedComponent {
    private final World world;

    private boolean running = false;
    private List<UUID> players = new ArrayList<>();
    private List<UUID> hitmen = new ArrayList<>();
    private List<UUID> detectives = new ArrayList<>();
    private List<UUID> targets = new ArrayList<>();

    public WorldGameComponent(World world) {
        this.world = world;
    }

    private void sync() {
        TrainMurderMysteryComponents.TRAIN.sync(this.world);
    }

    public void setRunning(boolean running) {
        this.running = running;
        this.sync();
    }

    public boolean isRunning() {
        return running;
    }

    public void setPlayers(List<UUID> players) {
        this.players = players;
        this.sync();
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public List<UUID> getHitmen() {
        return hitmen;
    }

    public void setHitmen(List<UUID> hitmen) {
        this.hitmen = hitmen;
        this.sync();
    }

    public List<UUID> getDetectives() {
        return detectives;
    }

    public void setDetectives(List<UUID> detectives) {
        this.detectives = detectives;
        this.sync();
    }

    public List<UUID> getTargets() {
        return targets;
    }

    public void setTargets(List<UUID> targets) {
        this.targets = targets;
        this.sync();
    }

    @Override
    public void readFromNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.running = nbtCompound.getBoolean("Running");

    }

    @Override
    public void writeToNbt(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup wrapperLookup) {
        nbtCompound.putBoolean("Running", running);

    }

    public void startGame() {
        if (world instanceof ServerWorld serverWorld) {
            TrainMurderMysteryComponents.TRAIN.get(serverWorld).setTrainSpeed(130);

            List<ServerPlayerEntity> playerPool = new ArrayList<>(serverWorld.getPlayers().stream().filter(serverPlayerEntity -> !serverPlayerEntity.isInCreativeMode() && !serverPlayerEntity.isSpectator()).toList());

            // clear items
            for (ServerPlayerEntity serverPlayerEntity : playerPool) {
                serverPlayerEntity.getInventory().clear();
            }

            // select hitmen
            int hitmanCount = (int) Math.floor(playerPool.size() * .2f);
            ArrayList<UUID> hitmen = new ArrayList<>();
            for (int i = 0; i < hitmanCount; i++) {
                ServerPlayerEntity selectedPlayer = playerPool.get(world.random.nextInt(playerPool.size()));
                hitmen.add(selectedPlayer.getUuid());
                selectedPlayer.giveItemStack(new ItemStack(TrainMurderMysteryItems.KNIFE));
                playerPool.remove(selectedPlayer);
            }
            setHitmen(hitmen);

            // select detectives
            int detectiveCount = hitmanCount;
            ArrayList<UUID> detectives = new ArrayList<>();
            for (int i = 0; i < detectiveCount; i++) {
                ServerPlayerEntity selectedPlayer = playerPool.get(world.random.nextInt(playerPool.size()));
                detectives.add(selectedPlayer.getUuid());
                selectedPlayer.giveItemStack(new ItemStack(TrainMurderMysteryItems.REVOLVER));
                playerPool.remove(selectedPlayer);
            }
            setDetectives(detectives);

            // select targets
            int targetCount = playerPool.size() / 2;
            ArrayList<UUID> targets = new ArrayList<>();
            for (int i = 0; i < targetCount; i++) {
                ServerPlayerEntity selectedPlayer = playerPool.get(world.random.nextInt(playerPool.size()));
                targets.add(selectedPlayer.getUuid());
                selectedPlayer.giveItemStack(new ItemStack(TrainMurderMysteryItems.ROOM_KEY));
                playerPool.remove(selectedPlayer);
            }
            setTargets(targets);


            // debug display
            System.out.println("Hitmen -----------------");
            for (UUID uuid : getHitmen()) {
                System.out.println(world.getPlayerByUuid(uuid).getDisplayName().getString());
            }
            System.out.println("Detectives -----------------");
            for (UUID uuid : getDetectives()) {
                System.out.println(world.getPlayerByUuid(uuid).getDisplayName().getString());
            }
            System.out.println("Targets -----------------");
            for (UUID uuid : getTargets()) {
                System.out.println(world.getPlayerByUuid(uuid).getDisplayName().getString());
            }
        }
    }
}
