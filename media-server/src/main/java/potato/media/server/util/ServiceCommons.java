package potato.media.server.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author zh_zhou
 * created at 2020/02/07 23:07
 * Copyright [2020] [zh_zhou]
 */
public class ServiceCommons {
    public static final String TRACE_ID_HTTP_HEADER = "CLOGGING_TRACE_ID";
    public static final String SPAN_ID_HTTP_HEADER = "CLOGGING_SPAN_ID";

    public static final String APP_ID_HTTP_HEADER = "SOA20-Client-AppId";
    public static final String ServiceAppIdHttpHeaderKey = "SOA20-Service-AppId";
    public static final String ServiceHostIPHttpHeaderKey = "SOA20-Service-HostIP";
    public static final String ServiceLatencyHttpHeaderKey = "SOA20-Service-Latency";

    public static final String X_CLIENT_SOCKET_TIMEOUT = "x-soa20-client-socket-timeout";
    public static final String X_CLIENT_CONNECT_TIMEOUT = "x-soa20-client-connect-timeout";
    public static final String X_CLIENT_CONNECTION_REQUEST_TIMEOUT = "x-soa20-client-connection-request-timeout";
    public static final String X_CALLER = "x-soa20-caller";

    public static final String H5GATEWAY_SPECIAL_HEADER = "x-gate-request.toplevel.uuid";
    public static final String H5GATEWAY_REQUEST_IP_HEADER = "x-gate-request.client.ip";
    public static final String H5GATEWAY_RESPONSE_DATA_HEADER_PREFIX_HEADER = "x-gate-response.";
    public static final String WEBAPI_SPECIAL_HEADER = "x-webapi-request-toplevel-uuid";
    public static final String WEBAPI_REQUEST_IP_HEADER = "x-webapi-request-client-ip";
    public static final String X_REAL_IP = "X-Real-IP";

    public static final String REQUEST_SOURCE = "x-ctrip-request-source";
    public static final String GATE_REQUEST_SOURCE = "x-gate-req-src";

    public static final String H5_REQUEST_PAGE_ID = "x-ctrip-pageid";

    public static final String LOCALHOST_IP = "127.0.0.1";
    public static final List<String> LOCALHOST_IP_V6 = Arrays.asList("::1", "0:0:0:0:0:0:0:1");

    public static final String HTTP = "http";
    public static final String HTTPS = "https";

    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_SERVICE_PORT = 8080;

    public static final String RibbonUrlKey = "url";
    public static final String RibbonHealthCheckUrlKey = "healthCheckUrl";
    public static final String ArtemisSubEnvKey = "subenv";
    public static final String ArtemisAppIdKey = "appid";
    public static final String ARTEMIS_FRAMEWORK_VERSION_KEY = "framework-version";
    public static final String ARTEMIS_CODE_GENERATOR_VERSION_KEY = "code-generator-version";
    public static final String ARTEMIS_START_UP_TIME_KEY = "start-up-time";

    public static final String CDUBBO_BRIDGE_INVOKER = "cDubboBridgeInvoker";
}
