package io.tcj;

import io.tcj.protos.mortgage.Coupon;
import io.tcj.protos.mortgage.Mortgage;
import misk.web.Get;
import misk.web.Post;
import misk.web.QueryParam;
import misk.web.RequestBody;
import misk.web.RequestContentType;
import misk.web.RequestHeaders;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;
import okhttp3.Headers;
import okio.ByteString;

public class MortgageCalcProtoAction implements WebAction {
  @Post(pathPattern = "/mortgageProto")
  @RequestContentType(MediaTypes.APPLICATION_PROTOBUF)
  @ResponseContentType(MediaTypes.APPLICATION_PROTOBUF)
  public Coupon mortgageProto(
      @RequestBody Mortgage mortgage,
      @RequestHeaders Headers headers) {
    return new Coupon.Builder()
        .monthly(new MortgageParams(mortgage.principal, mortgage.apr, mortgage.term).monthly())
        .build();
  }
}
