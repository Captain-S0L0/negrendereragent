package com.terriblefriends.negrendereragent.agent;

import java.lang.instrument.Instrumentation;

public class NegRendererAgent {
    public static void premain(String args, final Instrumentation inst) {
        System.out.println("Starting NegRendererAgent...");

        try {
            inst.addTransformer(new RenderItemTransformer());
            inst.addTransformer(new GuiInventoryTransformer());
        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        System.out.println("NegRendererAgent Initialized.");
    }
}
