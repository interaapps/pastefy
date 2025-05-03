package de.interaapps.pastefy.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Model;
import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.algorithm.TagListing;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;

import java.util.stream.Collectors;

public class PasteAI {
    AnthropicClient client;
    public PasteAI(Pastefy pastefy) {
        client = AnthropicOkHttpClient.builder()
                .apiKey(pastefy.getConfig().get("ai.antrophic.token"))
                .build();
    }

    private MessageCreateParams.Builder builder() {
        return MessageCreateParams.builder()
                .maxTokens(512L)
                .model(Model.CLAUDE_3_HAIKU_20240307);
    }

    public String generateInfo(Paste paste) {
        MessageCreateParams params = builder()
                .system("Extract tags and a description from the following input. " +
                        "There are some default tags like lang-{programming_language}. To describe the language use always the lang-{programming_language} tag!\n"+
                        "In system_warnings you can add security information for running this code or potential security flaws of the code. Max description 100. Severity: 1-10 (10 most dangerous, 1 harmless). If there is nothing above 5, leave it undefined.\n"+
                        "(optional) suggested_filename: You can suggest another title if the current might be not that good\n" +
                        "max characters for description: 200\n"+
                        "dangerous: mark as dangerous if it's obviously dangerous and would harm users. Ignore examples to show the user something\n" +
                        "Respond ONLY with a JSON object in this format: \n" +
                        "{\n" +
                        "    \"tags\": [string],\n" +
                        "    \"description\": string\n" +
                        "    \"system_warnings\": ?[{\"description\": string, severity: number}]\n" +
                        "    \"suggested_filename\": ?string\n" +
                        "    \"dangerous\": boolean\n" +
                        "}")
                .addUserMessage("Title: "+ paste.getTitle() + ". Contents (Stripped to max. 1000 chars): "+paste.getContent())
                .build();

        Message message = client.messages().create(params);
        return message.content().stream().map(c -> c.text().get().text()).collect(Collectors.joining("\n"));
    }

    public AbstractObject generateTags(Paste paste) {
        String contents = paste.getContent();
        if (contents.length() > 500)
            contents = contents.substring(0, 500);

        MessageCreateParams params = builder()
                .system("Generate tags (max. 6 tags and max length 30 chars), a file_name (with extension) and file_extension (without dot) for this code. You can ignore file_name and file_extension if you can't find anything obvious you can leave it empty." +
                        "There are some default tags like lang-{programming_language}. Tags only contain a-z 1-9 and -.\n"+
                        "Respond ONLY with a JSON object in this format. Never generate anything else than JSON!: \n" +
                        "{\n" +
                        "    \"tags\": [string],\n" +
                        "    \"file_name\": string" +
                        "    \"file_extension\": string" +
                        "}")
                .addUserMessage("Title: "+ paste.getTitle() + ". Contents (Stripped to max. 500 chars): " + contents)
                .build();

        Message message = client.messages().create(params);
        return AbstractElement.fromJson(message.content().stream().map(c -> c.text().get().text()).collect(Collectors.joining("\n"))).object();
    }

    public String generateTagDescription(TagListing tagListing) {
        MessageCreateParams params = builder()
                .system("Generate a description for the tag given by the user. Max 150 chars. Respond ONLY with the description. Never generate anything else than JSON!")
                .addUserMessage(tagListing.tag)
                .build();

        Message message = client.messages().create(params);
        return message.content().stream().map(c -> c.text().get().text()).collect(Collectors.joining("\n"));
    }

}
