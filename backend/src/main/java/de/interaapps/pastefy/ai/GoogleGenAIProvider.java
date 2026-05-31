package de.interaapps.pastefy.ai;

import com.google.genai.Client;
import com.google.genai.types.*;
import org.javawebstack.webutils.config.Config;

public class GoogleGenAIProvider implements AIProvider {
    private static final String DEFAULT_MODEL = "gemini-2.5-flash-lite";
    private static final int MAX_TOKENS = 512;

    private final Client client;
    private final String model;

    public GoogleGenAIProvider(Config config) {
        client = Client.builder()
                .apiKey(config.get("ai.google.token"))
                .build();
        model = config.get("ai.model", DEFAULT_MODEL);
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.1f)
                .maxOutputTokens(MAX_TOKENS)
                .responseMimeType("application/json")
                .systemInstruction(Content.fromParts(Part.fromText(systemPrompt)))
                .build();
        GenerateContentResponse response = client.models.generateContent(model, userPrompt, config);
        return response.text();
    }

    @Override
    public String getName() {
        return "google";
    }

    @Override
    public String getModel() {
        return model;
    }
}
