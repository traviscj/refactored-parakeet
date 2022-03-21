package io.tcj;

import misk.web.Post;
import misk.web.RequestBody;
import misk.web.RequestContentType;
import misk.web.RequestHeaders;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;
import okhttp3.Headers;

public class MortgageCalc2Action implements WebAction {

  @Post(pathPattern = "/mortgage2")
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  public MortgageCalcResponse mortgage(
      @RequestBody MortgageParams mortgageParams,
      @RequestHeaders Headers headers) {
    return new MortgageCalcResponse(mortgageParams.monthly());
  }

}
