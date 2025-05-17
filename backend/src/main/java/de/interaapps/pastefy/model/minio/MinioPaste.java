package de.interaapps.pastefy.model.minio;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class MinioPaste {
    public static void store(Paste paste) {
        if (Pastefy.getInstance().isMinioEnabled()) {
            paste.setCachedContents(paste.getContent());

            byte[] bytes = paste.getCachedContents().getBytes();
            try {
                ObjectWriteResponse objectWriteResponse = Pastefy.getInstance().getMinioClient().putObject(
                        PutObjectArgs.builder()
                                .bucket(Pastefy.getInstance().getMinioBucket())
                                .object(paste.createMinioName())
                                .contentType("text/plain")
                                .tags(new HashMap() {{
                                    put("paste", paste.getKey());
                                }})
                                .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                                .build()
                );
                paste.setContent(objectWriteResponse.object());
                paste.setStorageType(Paste.StorageType.S3);
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }

            paste.superSave();
        }
    }

    public static void delete(Paste paste) {
        if (Pastefy.getInstance().isMinioEnabled()) {
            try {
                Pastefy.getInstance().getMinioClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(Pastefy.getInstance().getMinioBucket())
                    .object(paste.getRawContent())
                .build());
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
