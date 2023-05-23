package lootbox.controller.api.Dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@ToString
@NoArgsConstructor
@Getter
@Setter
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
}
