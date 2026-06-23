package io.tcj.keyvalue;

import com.google.inject.Module;
import io.tcj.TraviscjTestModule;
import io.tcj.db.TraviscjDb;
import io.tcj.protos.kv.Kv;
import io.tcj.protos.kv.ListKvReq;
import io.tcj.protos.kv.ListKvResp;
import io.tcj.protos.kv.ListNamespaceReq;
import io.tcj.protos.kv.ListNamespaceResp;
import io.tcj.protos.kv.SortOrder;
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

/**
 * Exercises the thin KV web actions over a real KvStore. The actions delegate to
 * KvStore (covered separately) — these assert the request/response mapping, most
 * importantly that ListKvReq.order maps to ascending/descending ordering.
 */
@MiskTest(startService = true)
public class KvRpcTest {
  @MiskTestModule Module module = new TraviscjTestModule();

  @Inject KvStore kvStore;
  @Inject @TraviscjDb JooqTransacter transacter;

  @BeforeEach
  void clean() {
    transacter.transaction(
        session -> {
          session.getCtx().deleteFrom(KV).where(KV.NS.startsWith("TEST")).execute();
          return null;
        });
  }

  private KvListRpc listRpc() {
    KvListRpc rpc = new KvListRpc();
    rpc.kvStore = kvStore;
    return rpc;
  }

  @Test
  void listReturnsValuesAsStrings() {
    kvStore.put("TEST_NS", "k", "hello");

    ListKvResp resp =
        listRpc().List(new ListKvReq.Builder().ns("TEST_NS").k_prefix("").limit(10).build());

    assertThat(resp.kv).hasSize(1);
    assertThat(resp.kv.get(0).k).isEqualTo("k");
    assertThat(resp.kv.get(0).v).isEqualTo("hello");
  }

  @Test
  void listDefaultsToAscending() {
    kvStore.put("TEST_NS", "c", "3");
    kvStore.put("TEST_NS", "a", "1");
    kvStore.put("TEST_NS", "b", "2");

    ListKvResp resp =
        listRpc().List(new ListKvReq.Builder().ns("TEST_NS").k_prefix("").limit(10).build());

    assertThat(respKeys(resp)).containsExactly("a", "b", "c");
  }

  @Test
  void listDescendingWhenOrderIsDesc() {
    kvStore.put("TEST_NS", "a", "1");
    kvStore.put("TEST_NS", "c", "3");
    kvStore.put("TEST_NS", "b", "2");

    ListKvResp resp =
        listRpc()
            .List(
                new ListKvReq.Builder()
                    .ns("TEST_NS")
                    .k_prefix("")
                    .limit(10)
                    .order(SortOrder.DESC)
                    .build());

    assertThat(respKeys(resp)).containsExactly("c", "b", "a");
  }

  @Test
  void putWritesThroughAndEchoesRequest() {
    KvPutRpc rpc = new KvPutRpc();
    rpc.kvStore = kvStore;

    Kv request = new Kv.Builder().ns("TEST_NS").k("pk").v("pv").build();
    Kv response = rpc.Put(request);

    assertThat(response).isEqualTo(request);
    assertThat(kvStore.get("TEST_NS", "pk").getV()).isEqualTo("pv");
  }

  @Test
  void listNamespaceReturnsDistinctNamespacesByPrefix() {
    kvStore.put("TEST_ALPHA", "k", "1");
    kvStore.put("TEST_ALPHA", "k2", "1");
    kvStore.put("TEST_BETA", "k", "2");

    KvListNamespaceRpc rpc = new KvListNamespaceRpc();
    rpc.kvStore = kvStore;

    // null limit -> DEFAULT_LIMIT path
    ListNamespaceResp resp =
        rpc.ListNamespace(new ListNamespaceReq.Builder().ns_prefix("TEST_").build());

    assertThat(resp.ns).contains("TEST_ALPHA", "TEST_BETA");
  }

  private static List<String> respKeys(ListKvResp resp) {
    return resp.kv.stream().map(kv -> kv.k).collect(Collectors.toList());
  }
}
