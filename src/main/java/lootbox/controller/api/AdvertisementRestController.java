package lootbox.controller.api;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import jakarta.validation.Valid;
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
import java.util.Random;

@RestController
@RequestMapping("/api")
public class AdvertisementRestController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AdvertisementRepository advertisementRepo;
    private final ModelMapper modelMapper;
    String keyPath = "src/main/resources/infra3-leemans-freddy-0f7fb09ef959.json";
    GoogleCredentials credentials;
    String BUCKET_NAME = "lootbox-bucketeu";
    Storage storage;

    public AdvertisementRestController(AdvertisementRepository advertisementRepo, ModelMapper modelMapper) throws IOException {
        this.advertisementRepo = advertisementRepo;
        this.modelMapper = modelMapper;
        this.credentials = GoogleCredentials.fromStream(new FileInputStream(keyPath));
        this.storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
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

    @DeleteMapping("/delete-advertisement/{id}")
    public ResponseEntity<Void> deleteAdvertisement(@PathVariable int id) {
        if (advertisementRepo.existsById((long) id)) {
            String path = advertisementRepo.findById((long) id).get().getImage();
            advertisementRepo.deleteById((long) id);
            deleteFile(path);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    private void deleteFile(String imgPath) {
        storage.delete(BUCKET_NAME, imgPath);
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
        logger.info(image.getMediaLink());
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

        String filename = path.substring(path.lastIndexOf('/') + 1);
        path = filename.substring(0, filename.indexOf('?'));
        return path;
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




}
