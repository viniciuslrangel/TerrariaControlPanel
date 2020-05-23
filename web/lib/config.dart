import 'dart:html';

class Config {
  static const CUSTOM_HOST = String.fromEnvironment("BASE_HOST", defaultValue: null);
  static final HOST = CUSTOM_HOST ?? window.location.host;
  static final WEB_SOCKET = "ws://" + HOST + "/ws/";
  static final PREFIX = "//" + HOST;
  static final QUERY_WS_TOKEN = PREFIX + "/ws/token";
  static final SERVER_START = PREFIX + "/server/start";
  static final SERVER_STOP = PREFIX + "/server/stop";
  static final SERVER_STATUS = PREFIX + "/server/status";
}
