package io.tcj;

import misk.config.Config;
import misk.jdbc.DataSourceConfig;
import misk.web.WebConfig;

public class TraviscjConfig implements Config {
  public WebConfig web;

  public DataSourceConfig data_source;
}
