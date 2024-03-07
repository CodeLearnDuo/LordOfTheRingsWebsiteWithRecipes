package blps.duo.project.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String email;
    private String username;
    private String password;
    @ManyToOne
    private Race race;
    @Column(columnDefinition = "false")
    private boolean aLeader;

}
