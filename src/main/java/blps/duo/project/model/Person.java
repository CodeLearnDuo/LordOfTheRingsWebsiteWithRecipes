package blps.duo.project.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("person")
public class Person {
    @Id
    private Long id;
    private String email;
    private String username;
    private String password;
    @Column("person_race_id")
    private Long personRaceId;
    @Column("a_leader")
    private boolean aLeader;

    public Person(String email, String username, String password, Long personRaceId) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.personRaceId = personRaceId;
    }
}
