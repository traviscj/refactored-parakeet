package io.tcj;

import misk.web.Get;
import misk.web.ResponseContentType;
import misk.web.actions.WebAction;
import misk.web.mediatype.MediaTypes;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

public class KvListAction implements WebAction {
    @Inject
    KvStore kvStore;

    @Get(pathPattern = "/kv")
    @ResponseContentType(MediaTypes.APPLICATION_JSON)
    public List<Kv> list(
            ) {
        return kvStore.kvRecords().stream().map(r -> new Kv(r.getNs(), r.getK(), r.getV())).collect(Collectors.toList());
    }

    record Kv(String ns, String k, String v) {}
}
