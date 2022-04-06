package io.tcj.keyvalue;

import static io.tcj.jooq.tables.Kv.KV;

import com.google.common.base.Strings;
import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import java.util.List;
import javax.inject.Inject;
import misk.jooq.JooqTransacter;
import org.jetbrains.annotations.NotNull;
import org.jooq.SelectConditionStep;

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
}
