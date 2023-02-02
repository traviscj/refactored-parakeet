package io.tcj.mortgage;

import com.google.common.collect.ImmutableList;
import io.tcj.protos.kv.Kv;

import java.util.List;

public class NetWorthObservations {
    private NetWorthObservations() {} // don't instantiate utility classes

    public static List<Kv> nwo() {
        return ImmutableList.of(
                new Kv.Builder().ns("NET_WORTH_OBSERVATIONS").k("2023-02-02").v("1234.56").build()
        );
    }
}
