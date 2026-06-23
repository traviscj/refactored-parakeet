package io.tcj.mortgage;

import io.tcj.protos.mortgage.Coupon;
import io.tcj.protos.mortgage.Mortgage;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class MortgageCalcRpcTest {

    @Test
    void calcReturnsMonthlyCoupon() {
        Mortgage m = new Mortgage.Builder().principal(100_000.0).apr(0.06).term(30.0).build();

        Coupon coupon = new MortgageCalcRpc().Calc(m);

        assertThat(coupon.monthly).isCloseTo(599.55, within(0.05));
    }

    @Test
    void calcMatchesMortgageParams() {
        Mortgage m = new Mortgage.Builder().principal(742_000.0).apr(0.0625).term(30.0).build();

        Coupon coupon = new MortgageCalcRpc().Calc(m);

        assertThat(coupon.monthly).isEqualTo(new MortgageParams(m).monthly());
    }
}
