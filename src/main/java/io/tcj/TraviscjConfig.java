package io.tcj;

import misk.jdbc.DataSourceConfig;
import misk.web.WebConfig;
import wisp.config.Config;

public class TraviscjConfig implements Config {
  public WebConfig web;

  public DataSourceConfig data_source;
}
