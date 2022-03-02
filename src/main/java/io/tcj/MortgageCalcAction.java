package io.tcj;

import misk.web.Get;
import misk.web.PathParam;
import misk.web.RequestHeaders;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;
import okhttp3.Headers;

public class MortgageCalcAction implements WebAction {
  @Get(pathPattern = "/mortgage/{principal}/{apr}/{term}")
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  public MortgageCalcResponse mortgage(
      @PathParam("principal") Double principal,
      @PathParam("apr") Double apr,
      @PathParam("term") Double term,
      @RequestHeaders Headers headers) {
    double L = principal;
    double c = apr / 12;
    double n = term * 12;

    double numerator = c * Math.pow(1 + c, n);
    double denominator = Math.pow(1 + c, n) - 1;

    return new MortgageCalcResponse(L * numerator / denominator);
  }

  static class MortgageCalcResponse {
    private final double monthly;

    MortgageCalcResponse(double monthly) {
      this.monthly = monthly;
    }
  }
}
