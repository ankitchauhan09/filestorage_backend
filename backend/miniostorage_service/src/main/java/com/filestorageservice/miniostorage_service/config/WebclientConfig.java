package com.filestorageservice.miniostorage_service.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

@Configuration
public class WebclientConfig {

    @Bean(name = "StorageManagementWebClient")
    public WebClient storageManagementWebClient() throws Exception {
        KeyStore trustStore = KeyStore.getInstance("JKS");
        try (InputStream trustStoreStream = getClass().getClassLoader().getResourceAsStream("storage_service_truststore.jks")) {
            trustStore.load(trustStoreStream, "filestorage_storage".toCharArray());
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        SslContext sslContext = SslContextBuilder.forClient()
                .trustManager(trustManagerFactory)
                .build();
        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        return WebClient.builder().baseUrl("https://localhost:8004/api/v1/manage-storage").clientConnector(new ReactorClientHttpConnector(httpClient)).build();
    }

}
