package de.interaapps.pastefy.controller.pastes;

import de.interaapps.pastefy.controller.HttpController;
import de.interaapps.pastefy.model.database.Paste;
import de.interaapps.pastefy.model.database.User;
import io.undertow.util.FileUtils;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.text.StringEscapeUtils;
import org.javawebstack.http.router.Exchange;
import org.javawebstack.http.router.router.annotation.With;
import org.javawebstack.http.router.router.annotation.params.Attrib;
import org.javawebstack.http.router.router.annotation.params.Path;
import org.javawebstack.http.router.router.annotation.verbs.Get;
import org.javawebstack.http.router.util.MimeType;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class PasteThumbnailController extends HttpController {
    private String template = null;

    {
        try {
            template = FileUtils.readFile(getClass().getClassLoader().getResourceAsStream("templates/thumbnail-svg.svg"));

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("fonts/PlusJakartaSans.ttf")));
            ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("fonts/JetBrainsMono.ttf")));
        } catch (Exception e) {}
    }

    @Get("/{id}/thumbnail.png")
    @With({"auth-login-required-read", "image-rate-limiter"})
    public String getPasteRaw(Exchange exchange, @Path("id") String id, @Attrib("user") User user) throws IOException, TranscoderException {
        Paste paste = Paste.get(id);
        if (paste == null || template == null || paste.isPrivate() || paste.isEncrypted())
            return null;

        exchange.contentType(MimeType.PNG);

        String svgContent = template
                .replace("%title%", StringEscapeUtils.escapeXml11(paste.getTitle()));

        String contents = paste.getContent();
        String[] lines = contents.split("\n");
        for (int i = 0; i < 5; i++) {
            String placeholder = "%content_" + (i + 1) + "%";
            if (i < lines.length) {
                svgContent = svgContent.replace(placeholder, StringEscapeUtils.escapeXml11(lines[i]));
            } else {
                svgContent = svgContent.replace(placeholder, "");
            }
        }

        ByteArrayInputStream svgInputStream = new ByteArrayInputStream(svgContent.getBytes(StandardCharsets.UTF_8));
        TranscoderInput input = new TranscoderInput(svgInputStream);

        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        TranscoderOutput output = new TranscoderOutput(pngOutputStream);

        Transcoder transcoder = new PNGTranscoder();
        transcoder.transcode(input, output);

        pngOutputStream.flush();

        InputStream pngInputStream = new ByteArrayInputStream(pngOutputStream.toByteArray());
        exchange.write(pngInputStream);

        pngInputStream.close();
        pngOutputStream.close();
        svgInputStream.close();
        return null;
    }
}
