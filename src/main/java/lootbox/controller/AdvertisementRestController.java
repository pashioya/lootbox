package lootbox.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AdvertisementRestController {

    private final String PROJECT_ID = "infra3-freddy-leemans";
    private final String BUCKET_NAME = "gs://bucket-1684830831";
    String keyPath = "src/main/resources/infra3-leemans-freddy-0f7fb09ef959.json";
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath));
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    public AdvertisementRestController() throws IOException {
    }

    public static String generateImageName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        int randomNum = new Random().nextInt(1000000);
        return "image_" + timestamp + "_" + randomNum;
    }

    public String uploadObject(String objectName, String filePath) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Blob image = storage.createFrom(blobInfo, Paths.get(filePath));
        return image.getMediaLink();

    }

    public String uploadFile(MultipartFile image) throws IOException {
        String dir = System.getProperty("user.dir");

        String originalFilename = image.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String imageName = generateImageName() + extension;
        String file_path = dir + "/" + imageName;
        image.transferTo(Path.of(file_path));
        String path = uploadObject(image.getOriginalFilename(), file_path);
        File file = new File(file_path);
        file.delete();
        return path;
    }
}
