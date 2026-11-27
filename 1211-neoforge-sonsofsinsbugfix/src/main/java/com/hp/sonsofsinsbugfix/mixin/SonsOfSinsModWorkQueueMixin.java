package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.util.WorkQueueCleaner;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.minecraft.util.Tuple;
import net.neoforged.fml.util.thread.SidedThreadGroups;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-01（NeoForge 1.21.1）：SonsOfSinsMod 延迟队列 tick<=0 永久内存泄漏、异常隔离、O(N) 迭代与停服清理。
 */
@Mixin(value = SonsOfSinsMod.class, remap = false)
public abstract class SonsOfSinsModWorkQueueMixin {
    @Shadow
    @Final
    private static Collection<Tuple<Runnable, Integer>> workQueue;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void sonsofsinsbugfix$registerCleaner(CallbackInfo ci) {
        WorkQueueCleaner.register(workQueue::clear);
    }

    @Inject(method = "queueServerWork", at = @At("HEAD"), cancellable = true)
    private static void sonsofsinsbugfix$safeQueueServerWork(int tick, Runnable action, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.fixWorkQueueLeaks.get()) {
            return;
        }
        ci.cancel();
        if (action == null) {
            return;
        }
        if (Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER) {
            workQueue.add(new Tuple<>(action, Math.max(1, tick)));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeTick(ServerTickEvent.Post event, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.fixWorkQueueLeaks.get()) {
            return;
        }
        ci.cancel();
        if (workQueue.isEmpty()) {
            return;
        }
        List<Runnable> ready = new ArrayList<>();
        Iterator<Tuple<Runnable, Integer>> it = workQueue.iterator();
        while (it.hasNext()) {
            Tuple<Runnable, Integer> entry = it.next();
            int remaining = entry.getB() - 1;
            if (remaining <= 0) {
                ready.add(entry.getA());
                it.remove();
            } else {
                entry.setB(remaining);
            }
        }
        for (Runnable task : ready) {
            try {
                task.run();
            } catch (Throwable t) {
                SonsOfSinsBugFixMod.LOGGER.error("Uncaught exception in Sons of Sins queued server task", t);
            }
        }
    }
}
