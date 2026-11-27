package com.hp.sonsofsinsbugfix.mixin;

import com.hp.sonsofsinsbugfix.SonsOfSinsBugFixMod;
import com.hp.sonsofsinsbugfix.config.SonsOfSinsBugFixConfig;
import com.hp.sonsofsinsbugfix.util.WorkQueueCleaner;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.mcreator.sonsofsins.SonsOfSinsMod;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.util.thread.SidedThreadGroups;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 修复 MCR-01：SonsOfSinsMod 延迟队列 tick<=0 永久内存泄漏（PuppetOfStrife 传 0 导致永久泄漏 ServerLevel/ServerPlayer）、
 * 缺服务端线程校验、无异常隔离、O(N*M) removeAll 以及停服不清队列。
 */
@Mixin(value = SonsOfSinsMod.class, remap = false)
public abstract class SonsOfSinsModWorkQueueMixin {
    @Shadow
    @Final
    private static Collection<AbstractMap.SimpleEntry<Runnable, Integer>> workQueue;

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
            workQueue.add(new AbstractMap.SimpleEntry<>(action, Math.max(1, tick)));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void sonsofsinsbugfix$safeTick(TickEvent.ServerTickEvent event, CallbackInfo ci) {
        if (!SonsOfSinsBugFixConfig.COMMON.fixWorkQueueLeaks.get()) {
            return;
        }
        ci.cancel();
        if (event.phase != TickEvent.Phase.END || workQueue.isEmpty()) {
            return;
        }
        List<Runnable> ready = new ArrayList<>();
        Iterator<AbstractMap.SimpleEntry<Runnable, Integer>> it = workQueue.iterator();
        while (it.hasNext()) {
            AbstractMap.SimpleEntry<Runnable, Integer> entry = it.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                ready.add(entry.getKey());
                it.remove();
            } else {
                entry.setValue(remaining);
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
