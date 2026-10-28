package io.tcj.keyvalue;

import com.google.common.base.Strings;
import io.tcj.db.TraviscjDb;
import io.tcj.jooq.tables.records.KvRecord;
import misk.jooq.JooqSession;
import misk.jooq.JooqTransacter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

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

    // Explicit @Inject constructor so KvStore is constructible under strict
    // injectors (e.g. misk's test injector requires @Inject constructors);
    // field injection still supplies jooqTransacter.
    @Inject
    public KvStore() {}

    // Exact (ns, k) match; kvRecords() treats k as a prefix, so it can't decide
    // insert-vs-update ("1042" would match "1042Pleasant" and update nothing).
    public void putOrUpdate(String ns, String k, String v) {
        jooqTransacter.transaction(jooqSession -> {
            KvSession kvSession = new KvSession(jooqSession);
            if (kvSession.update(ns, k, v) == 0) {
                kvSession.put(ns, k, v);
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
        return kvRecords(ns, kPrefix, limit, false);
    }

    public List<KvRecord> kvRecords(String ns, String kPrefix, Integer limit, boolean descending) {
        return jooqTransacter.transaction(
                new JooqTransacter.TransacterOptions(),
                jooqSession -> new KvSession(jooqSession).kvRecords(ns, kPrefix, limit, descending));
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

        // Clearing feed_sync_id re-queues the row for FeedPublisher, so feed
        // consumers see the new value. Returns rows affected (0 = no such key).
        public int update(String ns, String k, String v) {
            return jooqSession.getCtx().update(KV)
                    .set(KV.V, v)
                    .set(KV.UPDATED_AT, DSL.currentLocalDateTime(3)) // DB clock, like the column default
                    .setNull(KV.FEED_SYNC_ID)
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
            return kvRecords(ns, kPrefix, limit, false);
        }

        public List<KvRecord> kvRecords(String ns, String kPrefix, Integer limit, boolean descending) {
            @NotNull
            SelectConditionStep<KvRecord> kvRecords = jooqSession.getCtx().selectFrom(KV).where();
            if (!Strings.isNullOrEmpty(ns)) {
                kvRecords = kvRecords.and(KV.NS.eq(ns));
            }
            if (!Strings.isNullOrEmpty(kPrefix)) {
                kvRecords = kvRecords.and(KV.K.startsWith(kPrefix));
            }
            return kvRecords
                    .orderBy(descending ? KV.K.desc() : KV.K.asc())
                    .limit(limit)
                    .fetch();
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
