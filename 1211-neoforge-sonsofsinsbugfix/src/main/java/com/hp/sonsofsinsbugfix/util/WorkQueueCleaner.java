package com.hp.sonsofsinsbugfix.util;

/**
 * 延迟队列清理桥接接口（NeoForge 1.21.1）。
 */
public final class WorkQueueCleaner {
    private static Runnable clearAction;

    private WorkQueueCleaner() {
    }

    public static void register(Runnable action) {
        clearAction = action;
    }

    public static void clearQueue() {
        if (clearAction != null) {
            clearAction.run();
        }
    }
}
