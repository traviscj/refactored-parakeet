package io.tcj.keyvalue;

import static io.tcj.jooq.tables.Kv.KV;

import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import java.util.List;
import javax.inject.Inject;
import misk.jooq.JooqTransacter;

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

  public List<KvRecord> kvRecords() {
    return jooqTransacter.transaction(
        new JooqTransacter.TransacterOptions(),
        jooqSession -> jooqSession.getCtx().selectFrom(KV).fetch());
  }

  //    record Kv(String ns, String k, String v){}
}
