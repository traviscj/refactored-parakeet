package io.tcj.feeds.impl;

import io.tcj.feeds.api.DbFeedFetcher;
import io.tcj.feeds.api.FeedEntry;

import java.util.List;

public class DbFeedFetcherImpl implements DbFeedFetcher {
    @Override
    public int publish(int limit) {
        return 0;
    }

    @Override
    public List<FeedEntry<?>> fetchUnpublished(long sequenceValue, int limit) {
        return null;
    }
}
