package io.tcj;

import io.tcj.protos.mortgage.Mortgage;

public class MortgageParams {

  public double principal;
  public double apr;
  public double term;

  public MortgageParams(Mortgage mortgage) {
    this(mortgage.principal, mortgage.apr, mortgage.term);
  }
  public MortgageParams(Double principal, Double apr, Double term) {
    this.principal = principal;
    this.apr = apr;
    this.term = term;
  }

  public double monthly() {
    double L = principal;
    double c = apr / 12;
    double n = term * 12;

    double numerator = c * Math.pow(1 + c, n);
    double denominator = Math.pow(1 + c, n) - 1;

    double monthly = L * numerator / denominator;
    return monthly;
  }
}
