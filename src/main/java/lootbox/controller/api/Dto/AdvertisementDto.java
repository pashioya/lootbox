package lootbox.controller.api.Dto;

import lombok.*;

@AllArgsConstructor
@ToString
@NoArgsConstructor
@Getter
@Setter
public class AdvertisementDto {
    private String email;
    private String phoneNumber;
    private String image;
    private String title;
    private String description;
}
