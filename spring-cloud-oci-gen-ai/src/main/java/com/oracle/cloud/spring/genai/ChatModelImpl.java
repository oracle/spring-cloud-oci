/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatDetails;
import com.oracle.bmc.generativeaiinference.model.CohereChatRequest;
import com.oracle.bmc.generativeaiinference.model.CohereChatResponse;
import com.oracle.bmc.generativeaiinference.model.CohereMessage;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.ServingMode;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import com.oracle.bmc.generativeaiinference.requests.ChatRequest;
import com.oracle.bmc.generativeaiinference.responses.ChatResponse;
import lombok.Builder;
import org.springframework.util.Assert;

public class ChatModelImpl implements ChatModel {
    private final GenerativeAiInference client;
    private final ServingMode servingMode;
    private final String compartment;
    private final String preambleOverride;
    private final Double temperature;
    private final Double frequencyPenalty;
    private final Integer maxTokens;
    private final Double presencePenalty;
    private final Double topP;
    private final Integer topK;
    private final InferenceRequestType inferenceRequestType;
    private List<CohereMessage> cohereChatMessages;
    private List<ChatChoice> genericChatMessages;

    @Builder
    public ChatModelImpl(GenerativeAiInference client,
                         ServingMode servingMode,
                         String compartment,
                         String preambleOverride,
                         Double temperature,
                         Double frequencyPenalty,
                         Integer maxTokens,
                         Double presencePenalty,
                         Double topP,
                         Integer topK,
                         InferenceRequestType inferenceRequestType) {
        Assert.notNull(client, "client must not be null");
        Assert.notNull(servingMode, "servingMode must not be null");
        Assert.notNull(compartment, "compartment must not be null");
        this.client = client;
        this.servingMode = servingMode;
        this.compartment = compartment;
        this.preambleOverride = preambleOverride;
        this.temperature = Objects.requireNonNullElse(temperature, 1.0);
        this.frequencyPenalty = Objects.requireNonNullElse(frequencyPenalty, 0.0);
        this.maxTokens = Objects.requireNonNullElse(maxTokens, 600);
        this.presencePenalty = Objects.requireNonNullElse(presencePenalty, 0.0);
        this.topP = Objects.requireNonNullElse(topP, 0.75);
        this.topK = Objects.requireNonNullElse(topK, 0);
        this.inferenceRequestType = Objects.requireNonNullElse(inferenceRequestType, InferenceRequestType.COHERE);
    }

    public ChatResponse chat(String prompt) {
        ChatDetails chatDetails = ChatDetails.builder()
                .compartmentId(compartment)
                .servingMode(servingMode)
                .chatRequest(createChatRequest(prompt))
                .build();
        ChatRequest chatRequest = ChatRequest.builder()
                .body$(chatDetails)
                .build();
        ChatResponse chat = client.chat(chatRequest);
        saveChatHistory(chat);
        return chat;
    }

    private BaseChatRequest createChatRequest(String prompt) {
        switch (inferenceRequestType) {
            case COHERE -> CohereChatRequest.builder()
                    .frequencyPenalty(frequencyPenalty)
                    .maxTokens(maxTokens)
                    .presencePenalty(presencePenalty)
                    .message(prompt)
                    .temperature(temperature)
                    .topP(topP)
                    .topK(topK)
                    .chatHistory(cohereChatMessages)
                    .preambleOverride(preambleOverride)
                    .build();
            case LLAMA -> {
                List<Message> messages = genericChatMessages == null ? new ArrayList<>() : genericChatMessages.stream()
                        .map(ChatChoice::getMessage)
                        .collect(Collectors.toList());
                List<ChatContent> contents = List.of(TextContent.builder()
                                .text(prompt)
                                .build());
                UserMessage message = UserMessage.builder()
                        .name("USER")
                        .content(contents)
                        .build();
                messages.add(message);
                return GenericChatRequest.builder()
                        .messages(messages)
                        .frequencyPenalty(frequencyPenalty)
                        .temperature(temperature)
                        .maxTokens(maxTokens)
                        .presencePenalty(presencePenalty)
                        .topP(topP)
                        .topK(topK)
                        .build();
            }
        }
        throw new IllegalArgumentException("Unsupported inference request type: " + inferenceRequestType);
    }

    private void saveChatHistory(ChatResponse chatResponse) {
        BaseChatResponse baseChatResponse = chatResponse.getChatResult().getChatResponse();
        if (baseChatResponse instanceof CohereChatResponse cohereChatResponse) {
            cohereChatMessages = cohereChatResponse.getChatHistory();
        } else if (baseChatResponse instanceof GenericChatResponse genericChatResponse) {
            genericChatMessages = genericChatResponse.getChoices();
        }
        throw new IllegalStateException("Unexpected chat response type: " + baseChatResponse.getClass().getName());
    }
}
