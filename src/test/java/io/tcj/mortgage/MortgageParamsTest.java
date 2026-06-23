package io.tcj.mortgage;

import io.tcj.protos.mortgage.Mortgage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class MortgageParamsTest {

    @Test
    void monthlyMatchesStandardAmortization() {
        // $100k @ 6% APR / 30yr -> $599.55/mo (closed-form amortization).
        MortgageParams p = new MortgageParams(100_000.0, 0.06, 30.0);
        assertThat(p.monthly()).isCloseTo(599.55, within(0.05));
    }

    @Test
    void higherRateCostsMore() {
        double low = new MortgageParams(500_000.0, 0.03, 30.0).monthly();
        double high = new MortgageParams(500_000.0, 0.07, 30.0).monthly();
        assertThat(high).isGreaterThan(low);
    }

    @Test
    void shorterTermRaisesMonthly() {
        double t30 = new MortgageParams(300_000.0, 0.05, 30.0).monthly();
        double t15 = new MortgageParams(300_000.0, 0.05, 15.0).monthly();
        assertThat(t15).isGreaterThan(t30);
    }

    @Test
    void paymentScalesLinearlyWithPrincipal() {
        double one = new MortgageParams(100_000.0, 0.05, 30.0).monthly();
        double four = new MortgageParams(400_000.0, 0.05, 30.0).monthly();
        assertThat(four).isCloseTo(4 * one, within(1e-6));
    }

    @Test
    void constructsFromMortgageProto() {
        Mortgage m = new Mortgage.Builder().principal(200_000.0).apr(0.045).term(30.0).build();
        MortgageParams p = new MortgageParams(m);
        assertThat(p.principal).isEqualTo(200_000.0);
        assertThat(p.apr).isEqualTo(0.045);
        assertThat(p.term).isEqualTo(30.0);
        assertThat(p.monthly()).isCloseTo(1013.37, within(0.05));
    }

    @Test
    void zeroAprYieldsNaN_documentsClosedFormEdgeCase() {
        // c == 0 -> 0/0 -> NaN. The closed-form formula can't represent an
        // interest-free loan; documenting the current behavior rather than
        // asserting a "correct" number.
        assertThat(new MortgageParams(100_000.0, 0.0, 30.0).monthly()).isNaN();
    }
}
