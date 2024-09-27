package com.phasetranscrystal.blast.skill;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class SkillGroup {
    public static final Logger LOGGER = LogManager.getLogger("BreaBlast:Skill/Group");
    public static final Codec<SkillGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.SKILL.byNameCodec().listOf().fieldOf("allowed").forGetter(i -> (List<Skill<?>>) i.allowedSkills.stream().toList()),
            SkillData.CODEC.optionalFieldOf("data").forGetter(s -> Optional.ofNullable(s.currentSkill))
    ).apply(instance, SkillGroup::new));

    public final ImmutableSet<Skill<? super Player>> allowedSkills;
    private SkillData<? super Player> currentSkill;
    private Player player;

    private boolean changed = true;

    //infoCaches don't change
    private Skill<? super Player> skillCache;
    private String stageCache;
    private int inactiveEnergyCache = 0;
    private int activeEnergyCache = 0;
    private int activeTimesCache = 0;

    private SkillGroup(ImmutableSet<Skill<? super Player>> collection) {
        this.allowedSkills = collection;
    }

    @SuppressWarnings("unchecked")
    private SkillGroup(List<Skill<?>> allowed, Optional<SkillData<?>> data) {
        this.allowedSkills = allowed.stream().map(skill -> (Skill<? super Player>) skill).collect(ImmutableSet.toImmutableSet());
        this.currentSkill = (SkillData<? super Player>) data.orElse(null);
    }

    public static SkillGroup create(Collection<Holder<Skill<? super Player>>> collection) {
        if (collection.isEmpty()) throw new IllegalArgumentException("Skill collection is empty");
        return new SkillGroup(collection.stream().map(Holder::value).collect(ImmutableSet.toImmutableSet()));
    }

    public static SkillGroup create(Holder<Skill<? super Player>>... holders) {
        if (holders.length == 0) throw new IllegalArgumentException("Skills array is empty");
        return new SkillGroup(Arrays.stream(holders).map(Holder::value).collect(ImmutableSet.toImmutableSet()));
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


    public Optional<Skill<? super Player>> getCurrentSkill() {
        return Optional.ofNullable(currentSkill).map(d -> d.skill);
    }

    //WARNING: USING THIS TO MODIFY THINGS IS NOT RECOMMENDED UNLESS YOU KNOW WHAT YOU ARE DOING.
    public Optional<SkillData<? super Player>> getCurrentSkillData() {
        return Optional.ofNullable(currentSkill);
    }

    public boolean changeToEmpty() {
        if (currentSkill == null) return false;

        boolean flag = currentSkill.requestDisable();
        currentSkill = null;
        changed = true;
        return flag;
    }

    public boolean changeTo(Skill<? super Player> skill) {
        if (skill == null || !allowedSkills.contains(skill) || (currentSkill != null && currentSkill.skill == skill))
            return false;
        if (currentSkill != null) {
            currentSkill.requestDisable();
            currentSkill = null;
        }
        this.currentSkill = new SkillData<>(skill);
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


    //ONLY CACHE
    public Skill<? super Player> getSkillCache() {
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
        Optional<SkillData<? super Player>> skillOpt = Optional.ofNullable(currentSkill);
        Skill<? super Player> skill = skillOpt.map(d -> d.skill).orElse(null);
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

        //TODO

    }

}
