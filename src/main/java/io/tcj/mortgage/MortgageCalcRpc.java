package io.tcj.mortgage;

import io.tcj.protos.mortgage.Coupon;
import io.tcj.protos.mortgage.Mortgage;
import io.tcj.protos.mortgage.MortgageServiceCalcBlockingServer;
import misk.web.actions.WebAction;

public class MortgageCalcRpc implements WebAction, MortgageServiceCalcBlockingServer {
  @Override
  public Coupon Calc(Mortgage mortgage) {
    return new Coupon.Builder().monthly(new MortgageParams(mortgage).monthly()).build();
  }
}
