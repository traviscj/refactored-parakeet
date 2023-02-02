package io.tcj.feeds;

import java.util.List;

public interface DbFeedFetcher {
    int publish(int limit);

    List<FeedEntry<?>> fetchUnpublished(long sequenceValue, int limit);
}
