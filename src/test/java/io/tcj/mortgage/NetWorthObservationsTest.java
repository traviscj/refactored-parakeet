package io.tcj.mortgage;

import io.tcj.protos.kv.Kv;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class NetWorthObservationsTest {

    @Test
    void nwoReturnsTheSeededObservation() {
        List<Kv> obs = NetWorthObservations.nwo();

        assertThat(obs).hasSize(1);
        Kv kv = obs.get(0);
        assertThat(kv.ns).isEqualTo("NET_WORTH_OBSERVATIONS");
        assertThat(kv.k).isEqualTo("2023-02-02");
        assertThat(kv.v).isEqualTo("1234.56");
    }
}
