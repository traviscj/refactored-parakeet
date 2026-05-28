package io.tcj.feeds.model;

import com.squareup.wire.Message;

import java.util.List;

/**
 * Typed result of an in-process feed fetch: the next batch of DTOs plus the new
 * cursor. The wire equivalent is the {@code FeedFetchResponse} proto (whose entries
 * are the serialized form of these DTOs).
 *
 * @param <Dto> the wire message type carried by the feed.
 */
public record FetchResult<Dto extends Message<?, ?>>(List<Dto> entries, String nextAfter) {
}
