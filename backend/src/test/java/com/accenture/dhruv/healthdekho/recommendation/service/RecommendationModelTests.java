package com.accenture.dhruv.healthdekho.recommendation.service;

import com.accenture.dhruv.healthdekho.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import software.amazon.awssdk.core.exception.SdkClientException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecommendationModelTests {

    private final ObjectProvider<ChatModel> models = mock();
    private final RecommendationModel recommendationModel =
            new RecommendationModel(models, new ObjectMapper(), "bedrock-model", "");

    @Test
    void returnsServiceUnavailableWhenNoModelIsEnabled() {
        assertUnavailable();
    }

    @Test
    void returnsServiceUnavailableWhenModelInitializationFails() {
        when(models.getIfAvailable()).thenThrow(new BeanCreationException("Bedrock configuration is invalid"));

        assertUnavailable();
    }

    @Test
    void returnsServiceUnavailableWhenBedrockCannotBeReached() {
        ChatModel model = mock(ChatModel.class);
        when(models.getIfAvailable()).thenReturn(model);
        when(model.call(any(Prompt.class))).thenThrow(SdkClientException.create("Connection failed"));

        assertUnavailable();
        verify(model).call(any(Prompt.class));
    }

    private void assertUnavailable() {
        var error = assertThrows(ApiException.class,
                () -> recommendationModel.recommend(30, "Family cover", 12, "HEALTH", List.of()));

        assertThat(error.status()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(error.getMessage()).contains("Please try again later");
    }
}
