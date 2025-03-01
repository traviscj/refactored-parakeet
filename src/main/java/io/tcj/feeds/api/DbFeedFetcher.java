package io.tcj.feeds.api;

import com.squareup.wire.Message;
import io.tcj.feeds.FeedEntryWebAction;
import io.tcj.feeds.model.FeedCursor;
import org.jooq.UpdatableRecord;

import java.util.List;

// n.b. this is more like a "feed database interactions" class since
// it encapsulates all the stuff related to one particular table.
// Probably possible to parameterize a lot of this around a certain class!
public interface DbFeedFetcher<Rec extends UpdatableRecord<Rec>, Dto extends Message<?,?>> {
    Class<Rec> recordType();

    int publish(List<? extends FeedEntry<Rec>> feedEntries, int limit);

    List<? extends FeedEntry<Rec>> fetchUnpublished(int limit);

    FeedEntryWebAction.FeedFetchResponse<Dto> fetchPublished(FeedCursor feedCursor, int limit);
}
