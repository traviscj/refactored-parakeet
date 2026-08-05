package io.tcj.feeds;

import com.google.inject.Module;
import io.tcj.TraviscjTestModule;
import io.tcj.db.TraviscjDb;
import io.tcj.feeds.impl.FeedCursorStoreImpl;
import io.tcj.feeds.model.FeedCursor;
import io.tcj.jooq.tables.records.FeedCursorsRecord;
import misk.jooq.JooqTransacter;
import misk.testing.MiskTest;
import misk.testing.MiskTestModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.tcj.jooq.tables.FeedCursors.FEED_CURSORS;
import static org.assertj.core.api.Assertions.assertThat;

@MiskTest(startService = true)
public class FeedCursorStoreImplTest {
  @MiskTestModule Module module = new TraviscjTestModule();

  @Inject FeedCursorStoreImpl cursorStore;
  @Inject @TraviscjDb JooqTransacter transacter;

  // Tests share the codegen schema, so scrub TEST-prefixed consumers for isolation.
  @BeforeEach
  void clean() {
    transacter.transaction(
        session -> {
          session.getCtx()
              .deleteFrom(FEED_CURSORS)
              .where(FEED_CURSORS.CONSUMER.startsWith("TEST"))
              .execute();
          return null;
        });
  }

  @Test
  void getCreatesRowOnFirstUse() {
    FeedCursor cursor = cursorStore.get("TEST_feed", 0);
    assertThat(cursor.feedSyncId()).isZero();
    assertThat(cursor.shard()).isZero();
    assertThat(cursor.numShards()).isEqualTo(1);

    FeedCursorsRecord record = fetch("TEST_feed", 0);
    assertThat(record).isNotNull();
    assertThat(record.getToken()).isEqualTo("0");
    assertThat(record.getEnabled()).isEqualTo((byte) 1);
    assertThat(record.getShardCount()).isEqualTo(1);
    assertThat(record.getLastFetchedAt()).isNotNull();
  }

  @Test
  void putThenGetRoundTrips() {
    cursorStore.get("TEST_feed", 0);
    cursorStore.put("TEST_feed", 0, "42");
    assertThat(cursorStore.get("TEST_feed", 0).feedSyncId()).isEqualTo(42);
  }

  @Test
  void putOnMissingRowCreatesIt() {
    cursorStore.put("TEST_feed", 0, "7");
    assertThat(cursorStore.get("TEST_feed", 0).feedSyncId()).isEqualTo(7);
    assertThat(fetch("TEST_feed", 0).getEnabled()).isEqualTo((byte) 1);
  }

  @Test
  void shardsTrackIndependentCursors() {
    cursorStore.put("TEST_feed", 0, "10");
    cursorStore.put("TEST_feed", 1, "20");
    assertThat(cursorStore.get("TEST_feed", 0).feedSyncId()).isEqualTo(10);
    assertThat(cursorStore.get("TEST_feed", 1).feedSyncId()).isEqualTo(20);
    // one row per (consumer, shard):
    assertThat(fetch("TEST_feed", 0).getId()).isNotEqualTo(fetch("TEST_feed", 1).getId());
  }

  @Test
  void consumersTrackIndependentCursors() {
    cursorStore.put("TEST_feed_a", 0, "1");
    cursorStore.put("TEST_feed_b", 0, "2");
    assertThat(cursorStore.get("TEST_feed_a", 0).feedSyncId()).isEqualTo(1);
    assertThat(cursorStore.get("TEST_feed_b", 0).feedSyncId()).isEqualTo(2);
  }

  @Test
  void putAdvancesTokenAndBumpsLastFetchedAt() {
    cursorStore.put("TEST_feed", 0, "5");
    FeedCursorsRecord before = fetch("TEST_feed", 0);

    cursorStore.put("TEST_feed", 0, "6");
    FeedCursorsRecord after = fetch("TEST_feed", 0);

    assertThat(after.getToken()).isEqualTo("6");
    assertThat(after.getLastFetchedAt()).isAfterOrEqualTo(before.getLastFetchedAt());
  }

  private FeedCursorsRecord fetch(String consumer, int shard) {
    return transacter.transaction(
        session ->
            session.getCtx()
                .selectFrom(FEED_CURSORS)
                .where(FEED_CURSORS.CONSUMER.eq(consumer).and(FEED_CURSORS.SHARD.eq(shard)))
                .fetchOne());
  }
}
