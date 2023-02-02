package io.tcj.keyvalue;

import com.google.common.base.Strings;
import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import misk.jooq.JooqTransacter;
import org.jetbrains.annotations.NotNull;
import org.jooq.SelectConditionStep;

import javax.inject.Inject;
import java.util.List;
import java.util.UUID;

import static io.tcj.jooq.tables.Kv.KV;

public class KvStore {
  @Inject @TraviscjDb JooqTransacter jooqTransacter;

  public void put(String ns, String k, String v) {
    jooqTransacter.transaction(
        new JooqTransacter.TransacterOptions(),
        jooqSession -> {
          KvRecord kvRecord = new KvRecord();
          kvRecord.setNs(ns);
          kvRecord.setK(k);
          kvRecord.setV(v);

          jooqSession.getCtx().insertInto(KV).set(kvRecord).execute();
          return null;
        });
  }

  public void record(String ns, String v) {
      put(ns, UUID.randomUUID().toString(), v);
  }

  public List<KvRecord> kvRecords(String ns, String kPrefix, Integer limit) {
    return jooqTransacter.transaction(
        new JooqTransacter.TransacterOptions(),
        jooqSession -> {
          @NotNull
          SelectConditionStep<KvRecord> kvRecords = jooqSession.getCtx().selectFrom(KV).where();
          if (!Strings.isNullOrEmpty(ns)) {
            kvRecords = kvRecords.and(KV.NS.eq(ns));
          }
          if (!Strings.isNullOrEmpty(kPrefix)) {
            kvRecords = kvRecords.and(KV.K.startsWith(kPrefix));
          }
          return kvRecords.limit(limit).fetch();
        });
  }

    public KvRecord get(String ns, String k) {
        List<KvRecord> kvRecords = kvRecords(ns, k, 2);
        if (kvRecords.isEmpty()) {
            throw new RuntimeException("found no sequences!");
        }
        if (kvRecords.size() > 1) {
            throw new RuntimeException("found too many sequences!");
        }
        return kvRecords.get(0);
    }
}
