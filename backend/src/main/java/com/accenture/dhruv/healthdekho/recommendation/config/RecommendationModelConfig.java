package com.accenture.dhruv.healthdekho.recommendation.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionProperties;
import org.springframework.ai.model.bedrock.converse.autoconfigure.BedrockConverseProxyChatProperties;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;

import java.util.List;

@Configuration(proxyBeanMethods = false)
class RecommendationModelConfig {

    @Bean
    static BeanFactoryPostProcessor deferBedrockInitialization() {
        // Resolve AWS settings and build the chat model only when recommendations need them.
        var dependencies = List.of(ChatModel.class, BedrockAwsConnectionProperties.class,
                BedrockConverseProxyChatProperties.class, AwsCredentialsProvider.class, AwsRegionProvider.class);
        return factory -> {
            for (Class<?> type : dependencies) {
                for (String name : factory.getBeanNamesForType(type, false, false)) {
                    factory.getBeanDefinition(name).setLazyInit(true);
                }
            }
        };
    }
}
