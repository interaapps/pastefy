package de.interaapps.pastefy.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import org.javawebstack.webutils.config.Config;

import java.util.stream.Collectors;

public class AnthropicAIProvider implements AIProvider {
    private static final String DEFAULT_MODEL = Model.CLAUDE_3_HAIKU_20240307.asString();
    private static final long MAX_TOKENS = 512L;

    private final AnthropicClient client;
    private final String model;

    public AnthropicAIProvider(Config config) {
        client = AnthropicOkHttpClient.builder()
                .apiKey(config.get("ai.antrophic.token"))
                .build();
        model = config.get("ai.model", DEFAULT_MODEL);
    }

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        MessageCreateParams params = MessageCreateParams.builder()
                .maxTokens(MAX_TOKENS)
                .model(model)
                .system(systemPrompt)
                .addUserMessage(userPrompt)
                .build();

        Message message = client.messages().create(params);
        return message.content().stream()
                .map(content -> content.text().get().text())
                .collect(Collectors.joining("\n"));
    }

    @Override
    public String getName() {
        return "anthropic";
    }

    @Override
    public String getModel() {
        return model;
    }
}
