package io.github.chromonym.chronoception.client;

import org.jetbrains.annotations.Nullable;

import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import io.github.chromonym.chronoception.Chronoception;
import io.github.chromonym.chronoception.PlayerTimeData;
import net.minecraft.client.item.ClampedModelPredicateProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

public class ChronoceptionClient {
    public static PlayerTimeData playerData = new PlayerTimeData();
    public static ClampedModelPredicateProvider trueClockProvider = new ClampedModelPredicateProvider(){
        private double time;
        private double step;
        private long lastTick;

        @Override
        public float unclampedCall(ItemStack itemStack, @Nullable ClientWorld clientWorld, @Nullable LivingEntity livingEntity, int i) {
            Entity entity;
            Entity entity2 = entity = livingEntity != null ? livingEntity : itemStack.getHolder();
            if (entity == null) {
                return 0.0f;
            }
            if (clientWorld == null && entity.getWorld() instanceof ClientWorld) {
                clientWorld = (ClientWorld)entity.getWorld();
            }
            if (clientWorld == null) {
                return 0.0f;
            }
            double d = clientWorld.getDimension().natural() ? (double)clientWorld.getDimension().getSkyAngle(clientWorld.getLunarTime() - (long)playerData.offset) : Math.random();
            d = this.getTime(clientWorld, d);
            return (float)d;
        }

        private double getTime(World world, double skyAngle) {
            if (world.getTime() != this.lastTick) {
                this.lastTick = world.getTime();
                double d = skyAngle - this.time;
                d = MathHelper.floorMod(d + 0.5, 1.0) - 0.5;
                this.step += d * 0.1;
                this.step *= 0.9;
                this.time = MathHelper.floorMod(this.time + this.step, 1.0);
            }
            return this.time;
        }
    };

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Chronoception.INITIAL_SYNC, (buf, context) -> {
            playerData.offset = buf.readDouble();
            playerData.tickrate = buf.readDouble();
            playerData.baseTickrate = buf.readDouble();
            Chronoception.LOGGER.info("Initial player times - Offset: %s, Rate: %s, Base rate: %s".formatted(playerData.offset, playerData.tickrate, playerData.baseTickrate));
        });
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, Chronoception.PLAYER_TIME_MODIFIED, (buf, context) -> {
            Chronoception.LOGGER.info("Client-stored times - Offset: %s, Rate: %s, Base rate: %s".formatted(playerData.offset, playerData.tickrate, playerData.baseTickrate));
            playerData.offset = buf.readDouble();
            playerData.tickrate = buf.readDouble();
            playerData.baseTickrate = buf.readDouble();
            Chronoception.LOGGER.info("Player times updated - Offset: %s, Rate: %s, Base rate: %s".formatted(playerData.offset, playerData.tickrate, playerData.baseTickrate));
        });
        ClientTickEvent.CLIENT_PRE.register((client) -> {
            if (client.isConnectedToLocalServer()) {
                if (client.getServer().isPaused()) { return; }
            }
            if (client.world != null) {
                if (client.world.getGameRules().getBoolean(GameRules.DO_DAYLIGHT_CYCLE)) {
                    playerData.offset += playerData.tickrate - 1.0;
                    playerData.offset %= 192000.0; // one lunar cycle
                }
            }
        });
    }
}
