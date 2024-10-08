package com.phasetranscrystal.blast.skill;

import com.google.common.collect.ImmutableMap;
import com.phasetranscrystal.blast.player.KeyInput;
import com.phasetranscrystal.blast.player.KeyInputEvent;
import com.phasetranscrystal.horiz.EventConsumer;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Behavior<T extends Entity> {
    public static final Behavior<Entity> EMPTY = Behavior.Builder.create().build();

    public final int delay;
    public final Consumer<SkillData<T>> start;
    public final Consumer<SkillData<T>> end;
    public final Consumer<SkillData<T>> chargeReady;
    public final Consumer<SkillData<T>> chargeFull;
    public final Consumer<SkillData<T>> activeEnd;
    public final BiConsumer<SkillData<T>, Integer> inactiveEnergyChange;
    public final BiConsumer<SkillData<T>, Integer> activeEnergyChange;
    public final BiConsumer<SkillData<T>, Integer> chargeChange;
    /**
     * 被监听的按键
     */
    public final IntOpenHashSet keys;
    /**
     * 按键行为<br/>
     * 只有keys中存在的按键才会触发该行为
     */
    public final KeyInput.Consumer<T> keyChange;
    public final ImmutableMap<Class<? extends Event>, BiConsumer<? extends Event, SkillData<T>>> listeners;

    public Behavior(Builder<T> builder) {
        this.delay = Math.max(builder.delay, 1);
        this.inactiveEnergyChange = builder.inactiveEnergyChange;
        this.activeEnergyChange = builder.activeEnergyChange;
        this.chargeChange = builder.chargeChange;
        this.start = builder.start;
        this.end = builder.end;
        this.chargeReady = builder.chargeReady;
        this.chargeFull = builder.chargeFull;
        this.activeEnd = builder.activeEnd;
        this.keys = new IntOpenHashSet(builder.keys);
        this.keyChange = builder.keyChange;
        this.listeners = ImmutableMap.copyOf(builder.listeners);
    }

    public static class Builder<T extends Entity> {
        public final Consumer<SkillData<T>> EMPTY = data -> {
        };
        public final BiConsumer<SkillData<T>, Integer> EMPTY_BI = (data, relate) -> {
        };
        public int delay = 1;
        public BiConsumer<SkillData<T>, Integer> inactiveEnergyChange = EMPTY_BI;
        public BiConsumer<SkillData<T>, Integer> activeEnergyChange = EMPTY_BI;
        public BiConsumer<SkillData<T>, Integer> chargeChange = EMPTY_BI;
        public Consumer<SkillData<T>> chargeReady = EMPTY;
        public Consumer<SkillData<T>> chargeFull = EMPTY;
        public Consumer<SkillData<T>> activeEnd = EMPTY;
        public Consumer<SkillData<T>> start = EMPTY;
        public Consumer<SkillData<T>> end = EMPTY;
        public IntArraySet keys = new IntArraySet();
        public KeyInput.Consumer<T> keyChange = (data, packet) -> {
        };
        public HashMap<Class<? extends Event>, BiConsumer<? extends Event, SkillData<T>>> listeners = new HashMap<>();

        public static <T extends Entity> Builder<T> create() {
            return new Builder<>();
        }

        public static <T extends Entity> Builder<T> create(Behavior<T> root) {
            Builder<T> builder = new Builder<T>();
            builder.inactiveEnergyChange = root.inactiveEnergyChange;
            builder.activeEnergyChange = root.activeEnergyChange;
            builder.chargeChange = root.chargeChange;
            builder.start = root.start;
            builder.end = root.end;
            builder.listeners.putAll(root.listeners);
            return builder;
        }

        public Builder<T> startWith(Consumer<SkillData<T>> consumer) {
            start = consumer;
            return this;
        }

        public Builder<T> setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public Builder<T> chargeChanged(BiConsumer<SkillData<T>, Integer> consumer) {
            this.chargeChange = consumer;
            return this;
        }

        public Builder<T> inactiveEnergyChanged(BiConsumer<SkillData<T>, Integer> consumer) {
            this.inactiveEnergyChange = consumer;
            return this;
        }

        public Builder<T> activeEnergyChanged(BiConsumer<SkillData<T>, Integer> consumer) {
            this.activeEnergyChange = consumer;
            return this;
        }

        public Builder<T> onChargeFull(Consumer<SkillData<T>> consumer) {
            this.chargeFull = consumer;
            return this;
        }

        public Builder<T> onChargeReady(Consumer<SkillData<T>> consumer) {
            this.chargeReady = consumer;
            return this;
        }

        public Builder<T> onActiveEnergyEmpty(Consumer<SkillData<T>> consumer) {
            this.activeEnd = consumer;
            return this;
        }

        public Builder<T> onTick(BiConsumer<EntityTickEvent.Post, SkillData<T>> consumer) {
            listeners.put(EntityTickEvent.Post.class, consumer);
            return this;
        }

        public Builder<T> onHurt(BiConsumer<LivingDamageEvent.Post, SkillData<T>> consumer) {
            listeners.put(LivingDamageEvent.Post.class, consumer);
            return this;
        }

        public Builder<T> onAttack(BiConsumer<EventConsumer.EntityAttackEvent.Post, SkillData<T>> consumer) {
            listeners.put(EventConsumer.EntityAttackEvent.Post.class, consumer);
            return this;
        }

        public Builder<T> onKillTarget(BiConsumer<EventConsumer.EntityKillEvent.Post, SkillData<T>> consumer) {
            listeners.put(EventConsumer.EntityKillEvent.Post.class, consumer);
            return this;
        }

        public <E extends Event> Builder<T> onEvent(Class<E> clazz, BiConsumer<E, SkillData<T>> consumer) {
            listeners.put(clazz, consumer);
            return this;
        }

        public Builder<T> apply(Consumer<Builder<T>> consumer) {
            consumer.accept(this);
            return this;
        }

        public Builder<T> endWith(Consumer<SkillData<T>> consumer) {
            end = consumer;
            return this;
        }

        public Builder<T> onKeyInput(KeyInput.Consumer<T> keyChange, int... keyListeners) {
            //用set来杜绝重复添加
            this.keys = new IntArraySet(keyListeners);

            if (keys.contains(256)) {
                //256为ESC键，不允许绑定
                throw new IllegalArgumentException("Key 256 is reserved for escape key");
            }

            this.keyChange = keyChange;
            return this;
        }

        public Behavior<T> build() {
            return new Behavior<>(this);
        }
    }
}
