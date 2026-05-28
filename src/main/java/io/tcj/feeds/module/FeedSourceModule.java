package io.tcj.feeds.module;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;
import io.tcj.feeds.ExposedFeed;
import io.tcj.feeds.FeedSourceFetchRpc;
import misk.web.WebActionModule;

/**
 * Installs the single generic {@code FeedSourceService.Fetch} endpoint and
 * initializes the feed registry ({@code Map<feed_name, ExposedFeed>}). Install once;
 * each {@link ExposedFeedModule} contributes its feed to the registry. Feed-agnostic.
 */
public final class FeedSourceModule extends AbstractModule {
    @Override
    protected void configure() {
        // ensure the registry exists even with zero feeds registered
        MapBinder.newMapBinder(binder(), String.class, ExposedFeed.class);
        install(WebActionModule.create(FeedSourceFetchRpc.class));
    }
}
