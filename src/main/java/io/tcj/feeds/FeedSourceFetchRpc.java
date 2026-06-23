package io.tcj.feeds;

import io.tcj.protos.feeds.FeedFetchRequest;
import io.tcj.protos.feeds.FeedFetchResponse;
import io.tcj.protos.feeds.FeedSourceServiceFetchBlockingServer;
import misk.security.authz.Unauthenticated;
import misk.web.actions.WebAction;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.Map;

/**
 * The single, generic implementation of {@code FeedSourceService.Fetch}
 * (served at {@code /io.tcj.FeedSourceService/Fetch}).
 *
 * <p>Routes by {@link FeedFetchRequest#feed_name} to the registered {@link ExposedFeed},
 * so adding a feed needs no new endpoint and this action never references a concrete
 * DTO type -- the per-feed {@code Dto} is hidden behind {@link ExposedFeed} and the
 * response carries opaque serialized bytes.
 */
public class FeedSourceFetchRpc implements WebAction, FeedSourceServiceFetchBlockingServer {
    @Inject
    Map<String, ExposedFeed> exposedFeeds;

    @NotNull
    @Override
    @Unauthenticated
    public FeedFetchResponse Fetch(@NotNull FeedFetchRequest request) {
        ExposedFeed feed = exposedFeeds.get(request.feed_name);
        if (feed == null) {
            throw new IllegalArgumentException(
                    "no exposed feed named '" + request.feed_name + "' (known: " + exposedFeeds.keySet() + ")");
        }
        return feed.fetch(request);
    }
}
