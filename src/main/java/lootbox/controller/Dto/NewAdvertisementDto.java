package lootbox.controller.Dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class NewAdvertisementDto {
    @NotNull
    private String email;
    @NotNull
    private String phoneNumber;
    @NotNull
    private MultipartFile image;
    @NotNull
    private String title;
    @NotNull
    private String description;

    public NewAdvertisementDto(String email, String phoneNumber, MultipartFile image, String title, String description) {
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.image = image;
        this.title = title;
        this.description = description;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
