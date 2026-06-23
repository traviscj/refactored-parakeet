package io.tcj.keyvalue;

import com.google.inject.Module;
import io.tcj.TraviscjTestModule;
import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import misk.jooq.JooqTransacter;
import misk.testing.MiskTest;
import misk.testing.MiskTestModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static io.tcj.jooq.tables.Kv.KV;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MiskTest(startService = true)
public class KvStoreTest {
  @MiskTestModule Module module = new TraviscjTestModule();

  @Inject KvStore kvStore;
  @Inject @TraviscjDb JooqTransacter transacter;

  // Tests share the codegen schema, so scrub the TEST* namespaces first for
  // isolation (KvStore has no delete; go straight at the table).
  @BeforeEach
  void clean() {
    transacter.transaction(
        session -> {
          session.getCtx().deleteFrom(KV).where(KV.NS.startsWith("TEST")).execute();
          return null;
        });
  }

  @Test
  void putThenGet() {
    kvStore.put("TEST_NS", "k1", "v1");
    assertThat(kvStore.get("TEST_NS", "k1").getV()).isEqualTo("v1");
  }

  @Test
  void getOptionalEmptyWhenMissing() {
    assertThat(kvStore.getOptional("TEST_NS", "absent")).isEmpty();
  }

  @Test
  void getThrowsWhenMissing() {
    assertThatThrownBy(() -> kvStore.get("TEST_NS", "absent"))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  void putOrUpdateInsertsThenUpdatesInPlace() {
    kvStore.putOrUpdate("TEST_NS", "k", "first");
    assertThat(kvStore.get("TEST_NS", "k").getV()).isEqualTo("first");

    kvStore.putOrUpdate("TEST_NS", "k", "second");
    assertThat(kvStore.get("TEST_NS", "k").getV()).isEqualTo("second");
    assertThat(kvStore.kvRecords("TEST_NS", "k", 10)).hasSize(1);
  }

  @Test
  void prefixFiltersByKey() {
    kvStore.put("TEST_NS", "alpha", "1");
    kvStore.put("TEST_NS", "alfred", "2");
    kvStore.put("TEST_NS", "beta", "3");
    assertThat(keys(kvStore.kvRecords("TEST_NS", "al", 10)))
        .containsExactlyInAnyOrder("alpha", "alfred");
  }

  @Test
  void limitCapsRowCount() {
    for (int i = 0; i < 5; i++) kvStore.put("TEST_NS", "k" + i, "v" + i);
    assertThat(kvStore.kvRecords("TEST_NS", "", 2)).hasSize(2);
  }

  @Test
  void ordersAscendingByKeyByDefault() {
    kvStore.put("TEST_NS", "c", "3");
    kvStore.put("TEST_NS", "a", "1");
    kvStore.put("TEST_NS", "b", "2");
    assertThat(keys(kvStore.kvRecords("TEST_NS", "", 10))).containsExactly("a", "b", "c");
  }

  @Test
  void ordersDescendingWhenRequested() {
    kvStore.put("TEST_NS", "a", "1");
    kvStore.put("TEST_NS", "c", "3");
    kvStore.put("TEST_NS", "b", "2");
    assertThat(keys(kvStore.kvRecords("TEST_NS", "", 10, true)))
        .containsExactly("c", "b", "a");
  }

  @Test
  void descendingWithLimitOneReturnsLatestByKey() {
    kvStore.put("TEST_NS", "2026-01-01", "a");
    kvStore.put("TEST_NS", "2026-06-22", "b");
    kvStore.put("TEST_NS", "2025-12-31", "c");

    List<KvRecord> latest = kvStore.kvRecords("TEST_NS", "", 1, true);
    assertThat(latest).hasSize(1);
    assertThat(latest.get(0).getK()).isEqualTo("2026-06-22");
  }

  @Test
  void nsListsDistinctNamespacesByPrefix() {
    kvStore.put("TEST_ALPHA", "k", "1");
    kvStore.put("TEST_ALPHA", "k2", "1");
    kvStore.put("TEST_BETA", "k", "2");
    assertThat(kvStore.ns("TEST_", 100)).contains("TEST_ALPHA", "TEST_BETA");
  }

  private static List<String> keys(List<KvRecord> records) {
    return records.stream().map(KvRecord::getK).collect(Collectors.toList());
  }
}
