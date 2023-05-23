package lootbox.controller;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import jakarta.validation.Valid;
import lootbox.controller.Dto.AdvertisementDto;
import lootbox.controller.Dto.NewAdvertisementDto;
import lootbox.domain.Advertisement;
import lootbox.repository.AdvertisementRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private AdvertisementRepository advertisementRepo;
    private final ModelMapper modelMapper;

    private final String PROJECT_ID = "infra3-freddy-leemans";
    private final String BUCKET_NAME = "gs://bucket-1684830831";
    String keyPath = "src/main/resources/infra3-leemans-freddy-0f7fb09ef959.json";
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath));
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    public AdvertisementRestController(AdvertisementRepository advertisementRepo, ModelMapper modelMapper) throws IOException {
        this.advertisementRepo = advertisementRepo;
        this.modelMapper = modelMapper;
    }

    @PostMapping(path = "/add-advertisement", consumes = {"multipart/form-data"})
    public ResponseEntity<AdvertisementDto> addIdea(@ModelAttribute @Valid NewAdvertisementDto newAdvertisementDto) throws IOException {
        logger.info("hello");
        String path = uploadFile(newAdvertisementDto.getImage());

        Advertisement createdAd = modelMapper.map(newAdvertisementDto, Advertisement.class);
        createdAd.setImage(path);
        logger.info(createdAd.toString());

        advertisementRepo.save(createdAd);
        AdvertisementDto createdAdDto = new AdvertisementDto(createdAd.getEmail(), createdAd.getPhoneNumber(), createdAd.getImage(), createdAd.getTitle(), createdAd.getDescription());
        return new ResponseEntity<>(createdAdDto, HttpStatus.CREATED);
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
