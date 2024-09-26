package com.phasetranscrystal.blast.skill;

import com.google.common.collect.ImmutableMap;
import com.phasetranscrystal.horiz.EventConsumer;
import com.phasetranscrystal.nonard.event.KeyInputEvent;
import com.phasetranscrystal.nonard.util.SkillKeyInputHandler;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Behavior<T extends Entity> {

    public final int delay;
    public final Consumer<SkillData<T>> start;
    public final Consumer<SkillData<T>> end;
    public final Consumer<SkillData<T>> chargeReady;
    public final Consumer<SkillData<T>> chargeFull;
    public final Consumer<SkillData<T>> activeEnd;
    public final BiConsumer<SkillData<T>, Integer> inactiveEnergyChange;
    public final BiConsumer<SkillData<T>, Integer> activeEnergyChange;
    public final BiConsumer<SkillData<T>, Integer> chargeChange;
    public final IntList keys;
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
        this.keys = builder.keys;
        this.listeners = ImmutableMap.copyOf(builder.listeners);
    }

    public static class Builder<T extends Entity> {
        public final Consumer<SkillData<T>> EMPTY = data -> {
        };
        public final BiConsumer<SkillData<T>, Integer> EMPTY_BI = (data, relate) -> {
        };
        public final SkillKeyInputHandler<T> NO_KEY_HANDLER = (data, key, action, modifiers) -> {
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
        public IntArrayList keys = new IntArrayList();
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

        /**
         * 设置按键监听行为<br>
         * 仅玩家生效<br>
         * 当处于该行为时，按键会被捕获并阻断原版逻辑，执行handler逻辑
         * @param keys 被监听的按键
         * @param consumer 按键处理
         */
        public Builder<T> setKeyInputListener(int[] keys, BiConsumer<KeyInputEvent, SkillData<T>> consumer) {
            this.keys.clear();
            this.keys.addElements(0, keys);
            listeners.put(KeyInputEvent.class, consumer);
            return this;
        }

        public Behavior<T> build() {
            return new Behavior<>(this);
        }
    }
}
