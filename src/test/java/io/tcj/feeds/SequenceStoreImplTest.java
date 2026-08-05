package io.tcj.feeds;

import com.google.inject.Module;
import io.tcj.TraviscjTestModule;
import io.tcj.db.TraviscjDb;
import io.tcj.feeds.impl.SequenceStoreImpl;
import io.tcj.jooq.tables.records.SequencesRecord;
import misk.jooq.JooqTransacter;
import misk.testing.MiskTest;
import misk.testing.MiskTestModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static io.tcj.jooq.tables.Sequences.SEQUENCES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MiskTest(startService = true)
public class SequenceStoreImplTest {
  @MiskTestModule Module module = new TraviscjTestModule();

  @Inject SequenceStoreImpl sequenceStore;
  @Inject @TraviscjDb JooqTransacter transacter;

  // Tests share the codegen schema, so scrub TEST-prefixed sequences for isolation.
  @BeforeEach
  void clean() {
    transacter.transaction(
        session -> {
          session.getCtx().deleteFrom(SEQUENCES).where(SEQUENCES.NAME.startsWith("TEST")).execute();
          return null;
        });
  }

  @Test
  void lookupUnknownReturnsZeroWithoutCreating() {
    assertThat(sequenceStore.lookup("TEST_absent").value()).isZero();
    assertThat(fetch("TEST_absent")).isNull();
  }

  @Test
  void allocateCreatesOnFirstUse() {
    assertThat(sequenceStore.allocate("TEST_seq", 1)).isEqualTo(1);
    assertThat(sequenceStore.lookup("TEST_seq").value()).isEqualTo(1);
  }

  @Test
  void allocateReservesContiguousBatches() {
    // value = last-assigned id: batches of 5 then 3 cover ids 1..5 and 6..8.
    assertThat(sequenceStore.allocate("TEST_seq", 5)).isEqualTo(5);
    assertThat(sequenceStore.allocate("TEST_seq", 3)).isEqualTo(8);
    assertThat(sequenceStore.lookup("TEST_seq").value()).isEqualTo(8);
  }

  @Test
  void allocateBumpsVersionPerAllocation() {
    sequenceStore.allocate("TEST_seq", 5);
    sequenceStore.allocate("TEST_seq", 2);
    SequencesRecord record = fetch("TEST_seq");
    assertThat(record.getValue()).isEqualTo(7);
    assertThat(record.getVersion()).isEqualTo(2);
  }

  @Test
  void sequencesAreIndependent() {
    sequenceStore.allocate("TEST_a", 4);
    sequenceStore.allocate("TEST_b", 1);
    assertThat(sequenceStore.lookup("TEST_a").value()).isEqualTo(4);
    assertThat(sequenceStore.lookup("TEST_b").value()).isEqualTo(1);
  }

  @Test
  void allocateRejectsNonPositiveCount() {
    assertThatThrownBy(() -> sequenceStore.allocate("TEST_seq", 0))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> sequenceStore.allocate("TEST_seq", -3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void resumesFromExternallySeededValue() {
    // Compatibility with feedz: a row seeded elsewhere (value = last-assigned id)
    // must be continued, not reset.
    transacter.transaction(
        session -> {
          session.getCtx()
              .insertInto(SEQUENCES, SEQUENCES.NAME, SEQUENCES.VALUE, SEQUENCES.VERSION)
              .values("TEST_seeded", 28L, 7L)
              .execute();
          return null;
        });
    assertThat(sequenceStore.allocate("TEST_seeded", 2)).isEqualTo(30);
    assertThat(fetch("TEST_seeded").getVersion()).isEqualTo(8);
  }

  private SequencesRecord fetch(String name) {
    return transacter.transaction(
        session -> session.getCtx().selectFrom(SEQUENCES).where(SEQUENCES.NAME.eq(name)).fetchOne());
  }
}
