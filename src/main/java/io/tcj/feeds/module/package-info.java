/**
 * Generic feed framework: compose a feed from three table-agnostic modules --
 * {@link io.tcj.feeds.module.FeedPublisherModule publish},
 * {@link io.tcj.feeds.module.ExposedFeedModule expose}, and
 * {@link io.tcj.feeds.module.FeedConsumerModule consume} -- each parameterized over a
 * jOOQ record {@code <Rec>} and a wire DTO {@code <Dto>}. See
 * {@code io.tcj.feeds.impl.KvFeedModule} for a worked composition (the "kv" feed).
 *
 * <h2>TODO(DescribeFeeds): topology introspection endpoint</h2>
 * Add a {@code DescribeFeeds} web action that renders the live feed topology as
 * Graphviz DOT and/or Mermaid -- a directed graph of:
 * <pre>
 *   entity/table ({@link io.tcj.feeds.model.FeedTableDef})
 *        -&gt; publisher ({@link io.tcj.feeds.model.PublishedFeedDef})
 *        -&gt; exposure  ({@link io.tcj.feeds.module.ExposedFeedModule.Exposure})
 *        -&gt; consumer  ({@link io.tcj.feeds.model.FeedConsumerDef}, fanned over shards)
 * </pre>
 * ...at least as far as is statically known. To make the graph discoverable, have
 * each module contribute its def to a Guice multibinding (the
 * {@link io.tcj.feeds.model.FeedDef} marker is the intended seam), then walk that
 * registry to emit nodes/edges. Would be an excellent at-a-glance map of
 * who-publishes / exposes / consumes-what across every feed in the service.
 */
package io.tcj.feeds.module;
