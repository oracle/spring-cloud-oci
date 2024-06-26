/*
 ** Copyright (c) 2024, Oracle and/or its affiliates.
 ** Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/
 */

package com.oracle.cloud.spring.genai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.oracle.bmc.generativeaiinference.responses.EmbedTextResponse;

/**
 * Interface for embedding text with OCI GenAI Service.
 */
public interface EmbeddingModel {
    /**
     * Embeds a list of text inputs.
     * @param inputs Text inputs to embed.
     * @return The list of EmbedTextResponses for the input texts.
     */
    List<EmbedTextResponse> embedAll(List<String> inputs);
    /**
     * Embeds a single line of text.
     * @param text Text to embed.
     * @return The EmbedTextResponse for the input.
     */
    default EmbedTextResponse embed(String text) {
        return embedAll(Collections.singletonList(text)).get(0);
    }

    /**
     * Convert an EmbedTextResponse to a list of embedding vectors.
     * @param response OCI EmbedTextResponse.
     * @return A list of embedding vectors.
     */
    default List<List<Float>> fromResponse(EmbedTextResponse response) {
        return fromResponses(Collections.singletonList(response));
    }

    /**
     * Convert a list of EmbedTextResponses to a list of embedding vectors.
     * @param responses A list of OCI EmbedTextResponses.
     * @return A list of embedding vectors.
     */
    default List<List<Float>> fromResponses(List<EmbedTextResponse> responses) {
        List<List<Float>> embeddings = new ArrayList<>();
        for (EmbedTextResponse response : responses) {
            embeddings.addAll(response.getEmbedTextResult().getEmbeddings());
        }
        return embeddings;
    }
}
