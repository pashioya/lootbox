package lootbox.controller.Dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

public class AdvertisementDto {
    private String email;
    private String phoneNumber;
    private String image;
    private String title;
    private String description;

    public AdvertisementDto(String email, String phoneNumber, String image, String title, String description) {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
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
