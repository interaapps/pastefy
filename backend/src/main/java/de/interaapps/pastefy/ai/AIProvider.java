package de.interaapps.pastefy.ai;

public interface AIProvider {
    String generate(String systemPrompt, String userPrompt);

    String getName();

    String getModel();
}
