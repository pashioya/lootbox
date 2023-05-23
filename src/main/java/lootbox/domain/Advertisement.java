package lootbox.domain;


import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@ToString
@NoArgsConstructor
@Getter
@Setter
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    private String image;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String description;

    public Advertisement(String email, String phoneNumber, String image, String title, String description) {
            this.email = email;
            this.phoneNumber = phoneNumber;
            this.image = image;
            this.title = title;
            this.description = description;
        }
}
