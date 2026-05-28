package io.tcj.feeds;

import misk.web.Get;
import misk.web.actions.WebAction;

import javax.inject.Inject;
import java.util.Map;

/**
 * Renders the live feed topology as Graphviz DOT and Mermaid:
 * {@code GET /_api/feeds/describe}.
 *
 * <p>Built from the {@code FeedSourceService} registry ({@code Map<feed_name, ExposedFeed>}),
 * so it shows every registered feed and its backing entity: {@code entity -> feed -> exposure}.
 *
 * <p>TODO: only the expose layer is registry-visible today. Publisher batch sizes
 * ({@code PublishedFeedDef}) and consumer shard fan-out ({@code FeedConsumerDef}) live
 * inside PrivateModules and aren't introspectable yet -- to include them, each module
 * should contribute a descriptor to a parent-visible multibinding (the
 * {@code io.tcj.feeds.model.FeedDef} marker is the intended seam).
 */
public class DescribeFeedsWebAction implements WebAction {
    @Inject
    Map<String, ExposedFeed> exposedFeeds;

    @Get(pathPattern = "/_api/feeds/describe")
    public String describe() {
        return mermaid() + "\n" + dot();
    }

    private String mermaid() {
        StringBuilder sb = new StringBuilder("%% mermaid\ngraph LR\n");
        exposedFeeds.forEach((name, feed) -> {
            String entity = feed.recordType().getSimpleName();
            sb.append(String.format(
                    "  %s[\"%s\"]:::entity --> feed_%s(((\"%s\"))):::feed --> src_%s[/\"FeedSourceService.Fetch\"/]:::expose%n",
                    entity, entity, name, name, name));
        });
        sb.append("  classDef entity fill:#cde,stroke:#46a;\n");
        sb.append("  classDef feed fill:#dfc,stroke:#4a4;\n");
        sb.append("  classDef expose fill:#fed,stroke:#a84;\n");
        return sb.toString();
    }

    private String dot() {
        StringBuilder sb = new StringBuilder("/* graphviz dot */\ndigraph feeds {\n  rankdir=LR;\n  node [shape=box,style=rounded];\n");
        exposedFeeds.forEach((name, feed) -> {
            String entity = feed.recordType().getSimpleName();
            sb.append(String.format("  \"%s\" -> \"feed: %s\" -> \"FeedSourceService.Fetch\\n(%s)\";%n",
                    entity, name, name));
        });
        sb.append("}\n");
        return sb.toString();
    }
}
