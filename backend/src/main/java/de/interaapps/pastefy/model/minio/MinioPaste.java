package de.interaapps.pastefy.model.minio;

import de.interaapps.pastefy.Pastefy;
import de.interaapps.pastefy.model.database.Paste;
import io.minio.*;
import io.minio.errors.*;
import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.abstractdata.AbstractObject;
import org.javawebstack.orm.Repo;
import org.javawebstack.webutils.util.IO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MinioPaste extends AbstractObject {
    public MinioPaste setObjectName(String objectName) {
        set("objectName", objectName);
        return this;
    }
    public MinioPaste setBucket(String bucket) {
        set("bucket", bucket);
        return this;
    }
    public MinioPaste setServer(String server) {
        set("server", server);
        return this;
    }
    public MinioPaste setRegion(String region) {
        set("region", region);
        return this;
    }
    public MinioPaste setETAG(String etag) {
        set("etag", etag);
        return this;
    }

    public String getObjectName() {
        return get("objectName").string();
    }

    public String getBucket() {
        return get("bucket", AbstractElement.fromObject(Pastefy.getInstance().getConfig().get("minio.bucket", "pastefy"))).string();
    }

    public String getServer() {
        return get("bucket", AbstractElement.fromObject(Pastefy.getInstance().getConfig().get("server.bucket"))).string();
    }

    public String getRegion() {
        return get("region").string();
    }

    public String getETAG() {
        return get("etag").string();
    }


    public static void store(Paste paste) {
        if (Pastefy.getInstance().isMinioEnabled()) {
            paste.setCachedContents(paste.getContent(false));

            byte[] bytes = paste.getCachedContents().getBytes();
            try {
                ObjectWriteResponse objectWriteResponse = Pastefy.getInstance().getMinioClient().putObject(
                        PutObjectArgs.builder()
                            .bucket(Pastefy.getInstance().getMinioBucket())
                            .object(generateObjectName(paste))
                            .contentType("text/plain")
                            .tags(new HashMap() {{
                                put("userId", paste.getUserId() != null ? paste.getUserId() : "anonymous");
                                put("paste", paste.getKey());
                            }})
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .build()
                );

                String jsonString = new MinioPaste()
                        .setServer(Pastefy.getInstance().getConfig().get("minio.server"))
                        .setBucket(objectWriteResponse.bucket())
                        .setObjectName(objectWriteResponse.object())
                        .setRegion(objectWriteResponse.region() == null ? Pastefy.getInstance().getConfig().get("minio.region") : objectWriteResponse.region())
                        .setETAG(objectWriteResponse.etag())
                        .toJsonString();

                paste.setContent(jsonString);
                paste.setStorageType(Paste.StorageType.S3);

                Map<String, Object> update = new HashMap<>();
                update.put("content", jsonString);
                update.put("storage_type", Paste.StorageType.S3);

                Repo.get(Paste.class)
                        .where("id", paste.getId())
                        .update(update);

            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static MinioPaste fromObject(AbstractObject object) {
        MinioPaste minioPaste = new MinioPaste();
        object.forEach(minioPaste::set);
        return minioPaste;
    }
    public static MinioPaste fromPaste(Paste paste) {
        if (paste.getStorageType() != Paste.StorageType.S3) return null;
        System.out.println(paste.getRawContent());
        return fromObject(AbstractElement.fromJson(paste.getRawContent()).object());
    }

    public static void delete(Paste paste) {
        if (Pastefy.getInstance().isMinioEnabled()) {
            try {
                MinioPaste minioPaste = fromPaste(paste);
                Pastefy.getInstance().getMinioClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(minioPaste.getBucket())
                    .region(minioPaste.getRegion())
                    .object(minioPaste.getObjectName())
                .build());
            } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                     InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                     XmlParserException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static GetObjectResponse get(Paste paste) {
        try {
            MinioPaste minioPaste = fromPaste(paste);
            return Pastefy.getInstance().getMinioClient().getObject(
                    GetObjectArgs.builder()
                        .bucket(minioPaste.getBucket())
                        .region(minioPaste.getRegion())
                        .object(minioPaste.getObjectName())
                        .build()
            );
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            throw new RuntimeException(e);
        }
    }

    public static String generateObjectName(Paste paste) {
        return
                "pastes/" + (paste.getUserId() != null ? paste.getUserId() : "anonymous")
                        + "/"
                        + paste.getKey()
                        + "/contents.txt";
    }
}
