package com.phasetranscrystal.blast.mixin;

import com.phasetranscrystal.blast.keylistener.KeyInputEvent;
import net.minecraft.client.KeyboardHandler;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class BreaBlastKeyboardHandlerMixin {
    @Inject(method = "keyPress", at = @At(value = "INVOKE_ASSIGN", target = "Lcom/mojang/blaze3d/platform/Window;getWindow()J", ordinal = 0), cancellable = true)
    public void onKeyPress_BreaBlast(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (NeoForge.EVENT_BUS.post(new KeyInputEvent.Client(key, scanCode, action, modifiers)).isCanceled()) {
            ci.cancel();
        }
    }
}