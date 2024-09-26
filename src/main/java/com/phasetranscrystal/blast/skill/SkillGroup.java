package com.phasetranscrystal.blast.skill;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public class SkillGroup<T extends Entity> {
    public static final Logger LOGGER = LogManager.getLogger("BreaBlast:Skill/Group");

    public final ImmutableSet<Skill<? super T>> allowedSkills;
    private SkillData<? super T> currentSkill;
    private T entity;

    private SkillGroup(ImmutableSet<Skill<? super T>> collection) {
        this.allowedSkills = collection;
    }

    @SuppressWarnings("unchecked")
    private SkillGroup(List<Skill<?>> allowed,SkillData<?> data) {
        this.allowedSkills = allowed.stream().map(skill -> (Skill<? super T>) skill).collect(ImmutableSet.toImmutableSet());
        this.currentSkill = (SkillData<? super T>) data;
    }

    public static <T extends Entity> SkillGroup<T> create(Collection<Holder<Skill<? super T>>> collection) {
        if (collection.isEmpty()) throw new IllegalArgumentException("Skill collection is empty");
        return new SkillGroup<>(collection.stream().map(Holder::value).collect(ImmutableSet.toImmutableSet()));
    }

    public static <T extends Entity> SkillGroup<T> create(Holder<Skill<? super T>>... holders) {
        if (holders.length == 0) throw new IllegalArgumentException("Skills array is empty");
        return new SkillGroup<>(Arrays.stream(holders).map(Holder::value).collect(ImmutableSet.toImmutableSet()));
    }

    public void bindEntity(T entity) {
        if (this.entity != null && !entity.getUUID().equals(this.entity.getUUID())) {
            LOGGER.warn("Entity instance (class={}) already exists. Skipped.", entity.getClass());
            return;
        }

        this.entity = entity;
        if(this.currentSkill != null) currentSkill.bindEntity(entity);
    }


    public Optional<Skill<? super T>> getCurrentSkill() {
        return Optional.ofNullable(currentSkill).map(d -> d.skill);
    }

    //WARNING: USING THIS TO MODIFY THINGS IS NOT RECOMMENDED UNLESS YOU KNOW WHAT YOU ARE DOING.
    public Optional<SkillData<? super T>> getCurrentSkillData() {
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

    public boolean changeTo(Skill<? super T> skill) {
        if (skill == null || !allowedSkills.contains(skill) || (currentSkill != null && currentSkill.skill == skill))
            return false;
        if(currentSkill != null) {
            currentSkill.requestDisable();
            currentSkill = null;
        }
        this.currentSkill = new SkillData<>(skill);
        if(this.entity != null){
            this.currentSkill.bindEntity(this.entity);
        }
        return true;
    }
}
