package io.tcj.feeds;

import java.util.List;

public interface FeedListener<T extends FeedEntry<T>> {
  void process(List<T> entries);
}
