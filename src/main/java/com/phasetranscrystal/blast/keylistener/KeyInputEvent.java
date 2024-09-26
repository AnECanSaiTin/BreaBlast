package com.phasetranscrystal.blast.keylistener;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public abstract class KeyInputEvent extends PlayerEvent {
    private final Player player;
    private final int key;
    private final int action;
    private final int modifiers;

    public KeyInputEvent(Player player, int key, int modifiers, int action) {
        super(player);
        this.player = player;
        this.key = key;
        this.action = action;
        this.modifiers = modifiers;
    }

    public Player getPlayer() {
        return player;
    }

    public int getKey() {
        return key;
    }

    public int getAction() {
        return action;
    }

    public int getModifiers() {
        return modifiers;
    }

    public static class Client extends KeyInputEvent implements ICancellableEvent {
        public final int scanCode;

        public Client(int key, int scanCode, int action, int modifiers) {
            super(Minecraft.getInstance().player, key, modifiers, action);
            this.scanCode = scanCode;
        }
    }

    public static class Server extends KeyInputEvent {
        public Server(ServerPlayer player, int key, int modifiers, int action) {
            super(player, key, modifiers, action);
        }

        @Override
        public ServerPlayer getPlayer() {
            return (ServerPlayer) super.getPlayer();
        }
    }
}
