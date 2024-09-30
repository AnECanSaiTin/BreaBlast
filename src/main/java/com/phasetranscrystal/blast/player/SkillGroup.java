package com.phasetranscrystal.blast.player;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.phasetranscrystal.blast.Registries;
import com.phasetranscrystal.blast.skill.Skill;
import com.phasetranscrystal.blast.skill.SkillData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class SkillGroup {
    public static final Logger LOGGER = LogManager.getLogger("BreaBlast:Skill/Group");
    public static final Codec<SkillGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.SKILL.byNameCodec().orElse(null).listOf().fieldOf("allowed").forGetter(i -> (List<Skill<?>>) ((Object) i.unlockedSkills.stream().toList())),
            SkillData.CODEC.optionalFieldOf("data").forGetter(s -> Optional.ofNullable(s.currentSkill))
    ).apply(instance, SkillGroup::new));

    private final Set<Skill<Player>> unlockedSkills = new HashSet<>();
    private SkillData<Player> currentSkill;
    private ServerPlayer player;

    private boolean changed = true;

    //infoCaches don't change
    private Skill<Player> skillCache;
    private String stageCache;
    private int inactiveEnergyCache = 0;
    private int activeEnergyCache = 0;
    private int activeTimesCache = 0;

    public SkillGroup() {
    }

    @SuppressWarnings("unchecked")
    private SkillGroup(List<Skill<?>> allowed, Optional<SkillData<?>> data) {
        allowed.stream().filter(s -> s != null && s.clazz.isAssignableFrom(Player.class)).map(s -> (Skill<Player>) s).forEach(unlockedSkills::add);
        this.currentSkill = (SkillData<Player>) data.orElse(null);
    }

    public void bindEntity(ServerPlayer entity) {
        if (this.player != null && !entity.getUUID().equals(this.player.getUUID())) {
            LOGGER.warn("Player instance (class={}) already exists. Skipped.", entity.getClass());
            return;
        }
        changed = true;

        this.player = entity;
        if (this.currentSkill != null) currentSkill.bindEntity(entity);
    }

    public Optional<Skill<Player>> getCurrentSkill() {
        return Optional.ofNullable(currentSkill).map(d -> d.skill);
    }

    public Optional<SkillData<Player>> getCurrentSkillData() {
        return Optional.ofNullable(currentSkill);
    }

    public boolean changeToEmpty() {
        if (currentSkill == null) return false;

        boolean flag = currentSkill.requestDisable();
        currentSkill = null;
        changed = true;
        return flag;
    }

    public boolean changeTo(Skill<?> skill) {
        if (skill == null || !unlockedSkills.contains(skill) || (currentSkill != null && currentSkill.skill == skill))
            return false;
        if (currentSkill != null) {
            currentSkill.requestDisable();
            currentSkill = null;
        }
        this.currentSkill = new SkillData<>((Skill<Player>) skill);
        if (this.player != null) {
            this.currentSkill.bindEntity(this.player);
        }
        changed = true;
        return true;
    }

    //Client only
    protected void consumeSynPacket(SkillDataSynPacket packet) {
        packet.skill().ifPresent(s -> this.skillCache = s.orElse(null));
        packet.stage().ifPresent(s -> this.stageCache = s.orElse(null));
        packet.inactiveEnergy().ifPresent(e -> this.inactiveEnergyCache = e);
        packet.activeEnergy().ifPresent(e -> this.activeEnergyCache = e);
        packet.activeTimes().ifPresent(e -> this.activeTimesCache = e);
    }

    protected void consumeInputPacket(KeyInputPacket packet) {
        getCurrentSkillData().ifPresent(data -> {
            int compoundKey = packet.var() & 0x7FFF;
            if (data.skill.keys.contains(compoundKey))
                data.skill.keyChange.accept(data, packet);
        });
    }

    public boolean unlock(Holder<Skill<?>> holder) {
        return unlock(holder.value());
    }

    public boolean unlock(Skill<?> skill) {
        if (!unlockedSkills.contains(skill) && skill.clazz.isAssignableFrom(Player.class)) {
            unlockedSkills.add((Skill<Player>) skill);
            return true;
        }
        return false;
    }

    public boolean lock(Holder<Skill<?>> holder) {
        return lock(holder.value());
    }

    public boolean lock(Skill<?> skill) {
        return unlockedSkills.remove(skill);
    }


    //ONLY CACHE
    public Skill<Player> getSkillCache() {
        return skillCache;
    }

    public String getStageCache() {
        return stageCache;
    }

    public int getInactiveEnergyCache() {
        return inactiveEnergyCache;
    }

    public int getActiveEnergyCache() {
        return activeEnergyCache;
    }

    public int getActiveTimesCache() {
        return activeTimesCache;
    }

    protected void tick() {
        if (player == null || (!changed && (currentSkill == null || !currentSkill.isChanged()))) return;

        changed = false;
        if (currentSkill != null) currentSkill.consumeChange();

        SkillDataSynPacket.Mutable mutable = new SkillDataSynPacket.Mutable();
        Optional<SkillData<Player>> skillOpt = Optional.ofNullable(currentSkill);
        Skill<Player> skill = skillOpt.map(d -> d.skill).orElse(null);
        if (Objects.equals(skillCache, skill)) {
            mutable.setSkill(skill);
            this.skillCache = skill;
        }
        String stage = skillOpt.flatMap(SkillData::getBehaviorName).orElse(null);
        if (Objects.equals(stageCache, stage)) {
            mutable.setStage(stage);
            this.stageCache = stage;
        }
        int inactiveEnergy = skillOpt.map(SkillData::getInactiveEnergy).orElse(0);
        if (inactiveEnergyCache != inactiveEnergy) {
            mutable.setInactiveEnergy(inactiveEnergy);
            this.inactiveEnergyCache = inactiveEnergy;
        }
        int activeEnergy = skillOpt.map(SkillData::getActiveEnergy).orElse(0);
        if (activeEnergyCache != activeEnergy) {
            mutable.setActiveEnergy(activeEnergy);
            this.activeEnergyCache = activeEnergy;
        }
        int activeTimes = skillOpt.map(SkillData::getActiveTimes).orElse(0);
        if (activeTimesCache != activeTimes) {
            mutable.setActiveTimes(activeTimes);
            this.activeTimesCache = activeTimes;
        }

        PacketDistributor.sendToPlayer(player, mutable.build());

    }

}
