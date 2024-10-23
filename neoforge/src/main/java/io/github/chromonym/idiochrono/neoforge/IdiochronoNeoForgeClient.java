package io.github.chromonym.idiochrono.neoforge;

import io.github.chromonym.idiochrono.Idiochrono;
import io.github.chromonym.idiochrono.client.IdiochronoClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;

@Mod(value = Idiochrono.MOD_ID, dist = Dist.CLIENT)
public class IdiochronoNeoForgeClient {
    public IdiochronoNeoForgeClient() {
        // Run our common setup.
        IdiochronoClient.init();
    }
}
