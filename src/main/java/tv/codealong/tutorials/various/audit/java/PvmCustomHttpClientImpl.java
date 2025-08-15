package tv.codealong.tutorials.various.audit.java;

import java.util.Map;

public class PvmCustomHttpClientImpl implements PvmCustomHttpClient {

    private final PvmCustomHttpClient.PvmCustomHttpClientBuilder pvmCustomHttpClientBuilder;
    private PvmCustomHttpClient pvmCustomHttpClient;

    public PvmCustomHttpClientImpl(
            PvmCustomHttpClient pvmCustomHttpClient,
            PvmCustomHttpClient.PvmCustomHttpClientBuilder pvmCustomHttpClientBuilder
    ) {
        this.pvmCustomHttpClient = pvmCustomHttpClient;
        this.pvmCustomHttpClientBuilder = pvmCustomHttpClientBuilder;
    }

    public Object postByRoute(String path, Map<String, Object> headerMap, Object payload) {
        return this.pvmCustomHttpClient.postByRoute(path, headerMap, payload);
    }


}
