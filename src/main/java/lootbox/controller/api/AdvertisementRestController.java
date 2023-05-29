package lootbox.controller.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lootbox.controller.api.Dto.AdvertisementDto;
import lootbox.controller.api.Dto.NewAdvertisementDto;
import lootbox.domain.Advertisement;
import lootbox.repository.AdvertisementRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class AdvertisementRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AdvertisementRepository advertisementRepo;
    private final ModelMapper modelMapper;
    private final String BUCKET_NAME = "bucket-1684830831";

    @PostMapping(path = "/add-advertisement", consumes = {"multipart/form-data"})
    public ResponseEntity<AdvertisementDto> addAdvertisement(@ModelAttribute @Valid NewAdvertisementDto newAdvertisementDto) throws IOException {
        logger.info("Adding advertisement: {}", newAdvertisementDto.getTitle());

        String imagePath = uploadFile(newAdvertisementDto.getImage());

        Advertisement createdAd = modelMapper.map(newAdvertisementDto, Advertisement.class);
        createdAd.setImage(imagePath);
        advertisementRepo.save(createdAd);

        AdvertisementDto createdAdDto = modelMapper.map(createdAd, AdvertisementDto.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAdDto);
    }

    @GetMapping("/advertisement/{id}")
    public ResponseEntity<AdvertisementDto> getAdvertisement(@PathVariable Long id) {
        Advertisement advertisement = advertisementRepo.getReferenceById(id);
        AdvertisementDto advertisementDto = new AdvertisementDto(
                advertisement.getEmail(),
                advertisement.getPhoneNumber(),
                advertisement.getImage(),
                advertisement.getTitle(),
                advertisement.getDescription()
        );
        return new ResponseEntity<>(advertisementDto, HttpStatus.OK);
    }

    @DeleteMapping("/delete-advertisement/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable long id) throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/infra3-leemans-freddy-0f7fb09ef959.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

        Optional<Advertisement> adOptional = advertisementRepo.findById(id);
        if (adOptional.isPresent()) {
            Advertisement ad = adOptional.get();
            String imagePath = ad.getImage();
            advertisementRepo.deleteById(id);
            deleteFile(storage, imagePath);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private void deleteFile(Storage storage, String imagePath) {
        storage.delete(BUCKET_NAME, imagePath);
    }

    public static String generateImageName() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        int randomNum = new Random().nextInt(1000000);
        return "image_" + timestamp + "_" + randomNum;
    }

    public String uploadObject(Storage storage, String objectName, String filePath) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        Blob image = storage.createFrom(blobInfo, Paths.get(filePath));
        logger.info("Uploaded image: {}", image.getMediaLink());
        return image.getMediaLink();
    }

    public String uploadFile(MultipartFile image) throws IOException {
        String dir = System.getProperty("user.dir");
        String originalFilename = image.getOriginalFilename();
        if (originalFilename == null) {
            return null;
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String imageName = generateImageName() + extension;
        String filePath = dir + "/" + imageName;
        image.transferTo(Path.of(filePath));

        try {
            Storage storage = getStorage();
            String imagePath = uploadObject(storage, imageName, filePath);
            deleteLocalFile(filePath);
            return extractFilename(imagePath);
        } catch (Exception e) {
            logger.error("Error uploading image: {}", e.getMessage());
            deleteLocalFile(filePath);
            throw e;
        }
    }

    private Storage getStorage() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream("src/main/resources/infra3-leemans-freddy-0f7fb09ef959.json"));
        return StorageOptions.newBuilder().setCredentials(credentials).build().getService();
    }

    private void deleteLocalFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }

    private String extractFilename(String path) {
        String filename = path.substring(path.lastIndexOf('/') + 1);
        return filename.substring(0, filename.indexOf('?'));
    }

}
