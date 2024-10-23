package io.github.chromonym.idiochrono.neoforge;

import net.neoforged.fml.common.Mod;

import io.github.chromonym.idiochrono.Idiochrono;

@Mod(Idiochrono.MOD_ID)
public final class IdiochronoNeoForge {
    public IdiochronoNeoForge() {
        // Run our common setup.
        Idiochrono.init();
    }
}
