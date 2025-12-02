package io.tcj.keyvalue;

import com.google.common.base.Strings;
import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import misk.jooq.JooqSession;
import misk.jooq.JooqTransacter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectConditionStep;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.tcj.jooq.tables.Kv.KV;

public class KvStore {
    @Inject
    @TraviscjDb
    JooqTransacter jooqTransacter;

    public void putOrUpdate(String ns, String k, String v) {
        jooqTransacter.transaction(jooqSession -> {
            KvSession kvSession = new KvSession(jooqSession);
            List<KvRecord> kvRecords = kvSession.kvRecords(ns, k, 1);
            if (kvRecords.isEmpty()) {
                kvSession.put(ns, k, v);
            } else {
                kvSession.update(ns, k, v);
            }
            return null;
        });
    }

    public void put(String ns, String k, String v) {
        jooqTransacter.transaction(
                new JooqTransacter.TransacterOptions(),
                jooqSession -> {
                    new KvSession(jooqSession).put(ns, k, v);
                    return null;
                });
    }

    public void record(String ns, String v) {
        put(ns, UUID.randomUUID().toString(), v);
    }

    public List<KvRecord> kvRecords(String ns, String kPrefix, Integer limit) {
        return jooqTransacter.transaction(
                new JooqTransacter.TransacterOptions(),
                jooqSession -> new KvSession(jooqSession).kvRecords(ns, kPrefix, limit));
    }

    public List<String> ns(String nsPrefix, Integer limit) {
        return jooqTransacter.transaction(
                new JooqTransacter.TransacterOptions(),
                jooqSession -> new KvSession(jooqSession).ns(nsPrefix, limit));

    }

    public static class KvSession {
        private final JooqSession jooqSession;
        public KvSession(JooqSession jooqSession) {
            this.jooqSession = jooqSession;
        }

        public void update(String ns, String k, String v) {
            jooqSession.getCtx().update(KV)
                    .set(KV.V, v)
                    .where(KV.NS.eq(ns).and(KV.K.eq(k)))
                    .execute();
        }
        public void put(String ns, String k, String v) {
            KvRecord kvRecord = new KvRecord();
            kvRecord.setNs(ns);
            kvRecord.setK(k);
            kvRecord.setV(v);

            jooqSession.getCtx().insertInto(KV).set(kvRecord).execute();
        }
        public List<KvRecord> kvRecords(String ns, String kPrefix, Integer limit) {
            @NotNull
            SelectConditionStep<KvRecord> kvRecords = jooqSession.getCtx().selectFrom(KV).where();
            if (!Strings.isNullOrEmpty(ns)) {
                kvRecords = kvRecords.and(KV.NS.eq(ns));
            }
            if (!Strings.isNullOrEmpty(kPrefix)) {
                kvRecords = kvRecords.and(KV.K.startsWith(kPrefix));
            }
            return kvRecords.limit(limit).fetch();
        }

        public List<String> ns(@Nullable String nsPrefix, Integer limit) {
            @NotNull
            SelectConditionStep<KvRecord> kvRecords = jooqSession.getCtx().selectFrom(KV).where();
            if (!Strings.isNullOrEmpty(nsPrefix)) {
                kvRecords = kvRecords.and(KV.NS.startsWith(nsPrefix));
            }
            return kvRecords.limit(limit).fetch().stream()
                    .map(KvRecord::getNs)
                    .distinct()
                    .collect(Collectors.toList());
        }
    }

    public Optional<KvRecord> getOptional(String ns, String k) {
        List<KvRecord> kvRecords = kvRecords(ns, k, 1);
        if (kvRecords.isEmpty()) {
            return Optional.empty();
        }
        if (kvRecords.size() > 1) {
            throw new RuntimeException("found too many sequences!");
        }
        return Optional.ofNullable(kvRecords.get(0));
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
