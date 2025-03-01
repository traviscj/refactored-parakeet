package io.tcj.feeds.impl;

import io.tcj.feeds.api.FeedListener;
import io.tcj.protos.kv.Kv;

import java.util.List;

public class PrintFeedListener implements FeedListener {
    @Override
    public void process(List<Kv> feedEntries) {
        feedEntries.forEach(feedEntry -> {
            System.out.println("feedEntry = " + feedEntry);
        });
    }
}
