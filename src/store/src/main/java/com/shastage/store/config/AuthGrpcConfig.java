package com.shastage.store.config;

import app.generated.AuthServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthGrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel authChannel(
            @Value("${auth.grpc.address}") String address
    ) {
        return ManagedChannelBuilder
                .forTarget(address)
                .usePlaintext()
                .build();
    }

    @Bean
    public AuthServiceGrpc.AuthServiceBlockingStub authStub(
            ManagedChannel authChannel
    ) {
        return AuthServiceGrpc.newBlockingStub(authChannel);
    }
}
