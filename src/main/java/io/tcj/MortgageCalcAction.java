package io.tcj;

import misk.web.Get;
import misk.web.PathParam;
import misk.web.RequestBody;
import misk.web.RequestHeaders;
import misk.web.Response;
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
    return new MortgageCalcResponse(new MortgageParams(principal, apr, term).monthly());
  }
}
