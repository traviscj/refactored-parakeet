package io.tcj.feeds.impl;

import io.tcj.db.TraviscjDb;
import io.tcj.feeds.api.SequenceStore;
import io.tcj.feeds.model.Sequence;
import io.tcj.jooq.tables.records.SequencesRecord;
import misk.jooq.JooqTransacter;
import org.jooq.DSLContext;

import javax.inject.Inject;

import static io.tcj.jooq.tables.Sequences.SEQUENCES;

/**
 * {@link SequenceStore} backed by the dedicated {@code sequences} table.
 *
 * <p>Allocation is compatible with the feedz Python side, which does: ensure the
 * row exists, {@code UPDATE sequences SET value = value + 1, version = version + 1
 * WHERE name = ?}, then reads {@code value}. Both sides agree that {@code value}
 * is the last already-assigned id, so they can safely share a sequence row (e.g.
 * {@code feed-kv} for the kv feed).
 */
public class SequenceStoreImpl implements SequenceStore {
    @Inject
    @TraviscjDb
    JooqTransacter jooqTransacter;

    // Explicit @Inject constructor so the class is constructible under strict
    // injectors; field injection still supplies jooqTransacter.
    @Inject
    public SequenceStoreImpl() {}

    @Override
    public Sequence lookup(String sequenceName) {
        return jooqTransacter.transaction(session -> {
            SequencesRecord record = session.getCtx()
                    .selectFrom(SEQUENCES)
                    .where(SEQUENCES.NAME.eq(sequenceName))
                    .fetchOne();
            return new Sequence(sequenceName, record == null ? 0L : record.getValue());
        });
    }

    @Override
    public long allocate(String sequenceName, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("allocate(" + sequenceName + ", " + count + "): count must be > 0");
        }
        return jooqTransacter.transaction(session -> {
            DSLContext ctx = session.getCtx();
            // Create-on-first-use; value/version default to 0 (= nothing assigned yet).
            ctx.insertInto(SEQUENCES, SEQUENCES.NAME)
                    .values(sequenceName)
                    .onDuplicateKeyIgnore()
                    .execute();
            // Atomic reservation under the row lock; same shape as feedz's UPDATE.
            ctx.update(SEQUENCES)
                    .set(SEQUENCES.VALUE, SEQUENCES.VALUE.plus(count))
                    .set(SEQUENCES.VERSION, SEQUENCES.VERSION.plus(1))
                    .where(SEQUENCES.NAME.eq(sequenceName))
                    .execute();
            return ctx.select(SEQUENCES.VALUE)
                    .from(SEQUENCES)
                    .where(SEQUENCES.NAME.eq(sequenceName))
                    .fetchSingle()
                    .value1();
        });
    }
}
