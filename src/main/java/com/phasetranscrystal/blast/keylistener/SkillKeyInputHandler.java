package com.phasetranscrystal.blast.keylistener;

import com.phasetranscrystal.nonard.skill.SkillData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 用于处理技能按键输入的函数接口。
 */
@FunctionalInterface
public interface SkillKeyInputHandler<T extends LivingEntity> {
    void accept(SkillData<T> data, int key, int action, int modifiers);
}
