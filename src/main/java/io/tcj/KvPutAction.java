package io.tcj;

import misk.web.Get;
import misk.web.PathParam;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;

import javax.inject.Inject;

public class KvPutAction implements WebAction {
    @Inject KvStore kvStore;

    @Get(pathPattern = "/kv/{ns}/{k}/{v}")
    @ResponseContentType(MediaTypes.APPLICATION_JSON)
    public String put(
            @PathParam("ns") String ns,
            @PathParam("ns") String k,
            @PathParam("ns") String v) {
        kvStore.put(ns, k, v);
        return "OK";
    }
}
