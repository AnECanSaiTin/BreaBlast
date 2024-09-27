package com.phasetranscrystal.blast.skill;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public class SkillGroup {
    public static final Logger LOGGER = LogManager.getLogger("BreaBlast:Skill/Group");
    public static final Codec<SkillGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Registries.SKILL.byNameCodec().listOf().fieldOf("allowed").forGetter(i -> i.allowedSkills.stream().map(s -> s).toList()),
            SkillData.CODEC.optionalFieldOf("data").forGetter(s -> Optional.ofNullable((Skill<? extends Entity>)((Skill<?>)s.currentSkill)))
    ).apply(instance, SkillGroup::new));

    public final ImmutableSet<Skill<?super Player>> allowedSkills;
    private SkillData<? super Player> currentSkill;
    private Player player;

    //infoCaches don't change
    public boolean enabled = true;
    public ResourceLocation skillName;
    public String stage;
    public int inactiveEnergy = 0;
    public int activeEnergy = 0;
    public int activeTimes = 0;

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

    public void bindEntity(Player entity) {
        if (this.player != null && !entity.getUUID().equals(this.player.getUUID())) {
            LOGGER.warn("Player instance (class={}) already exists. Skipped.", entity.getClass());
            return;
        }

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

    public boolean disable() {
        if (currentSkill != null) {
            boolean flag = currentSkill.requestDisable();
            currentSkill = null;
            return flag;
        }
        return false;
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
        return true;
    }
}
