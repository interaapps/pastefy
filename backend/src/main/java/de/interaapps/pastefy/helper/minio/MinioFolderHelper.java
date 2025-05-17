package de.interaapps.pastefy.helper.minio;

import de.interaapps.pastefy.Pastefy;
import io.minio.MinioClient;

public class MinioFolderHelper {
    public static void createFolderIfNotExists() {
        MinioClient minioClient = Pastefy.getInstance().getMinioClient();
    }
}
