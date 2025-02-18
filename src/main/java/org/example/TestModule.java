package org.example;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongMaps;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.rusherhack.client.api.events.client.EventUpdate;
import org.rusherhack.client.api.feature.module.ModuleCategory;
import org.rusherhack.client.api.feature.module.ToggleableModule;
import org.rusherhack.core.event.stage.Stage;
import org.rusherhack.core.event.subscribe.Subscribe;
import xaeroplus.feature.render.highlights.ChunkHighlightCache;
import xaeroplus.feature.render.highlights.ChunkHighlightSavingCache;
import xaeroplus.module.impl.OldChunks;
import xaeroplus.util.ChunkUtils;

public class TestModule extends ToggleableModule {
    private long lastUpdate = 0;
    private Long2LongMap oldChunksCache;

    public TestModule() {
        super("test", ModuleCategory.MISC);
    }

    @Subscribe(stage = Stage.POST)
    public void onTick(final EventUpdate event) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        ClientLevel level = mc.level;
        if (level == null) return;
        long now = System.currentTimeMillis();
        if (now - lastUpdate < 5000) return;
        lastUpdate = now;
        OldChunks oldChunks = xaeroplus.module.ModuleManager.getModule(OldChunks.class);
        ChunkHighlightCache cacheInstance = oldChunks.oldChunksCache.get();
        if (cacheInstance instanceof ChunkHighlightSavingCache savingCache) {
            ListenableFuture<Long2LongMap> highlightLoadFuture = savingCache.getHighlightsInCustomWindow(
                ChunkUtils.coordToRegionCoord(Mth.floor(player.getX())),
                ChunkUtils.coordToRegionCoord(Mth.floor(player.getZ())),
                3,
                level.dimension()
            );
            Futures.addCallback(
                highlightLoadFuture,
                new FutureCallback<>() {

                    @Override
                    public void onSuccess(final Long2LongMap result) {
                        oldChunksCache = result;
                    }
                    @Override
                    public void onFailure(final Throwable t) {
                        oldChunksCache = Long2LongMaps.EMPTY_MAP;
                    }
                }, mc);
        }
    }
}
