package io.tcj.feeds.api;

import io.tcj.protos.kv.Kv;

import java.util.List;

public interface FeedListener {
  void process(List<Kv> entries);
}
