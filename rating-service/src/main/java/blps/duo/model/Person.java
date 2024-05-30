package blps.duo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("person")
public class Person {

    @Id
    @Column("statistic_person_id")
    private Long statistic_person_id;
    private String email;
    @Column("person_race_id")
    private Long personRaceId;
    @Column("rating_count")
    private Long ratingCount;

    public Person(String email, Long personRaceId, Long ratingCount) {
        this.email = email;
        this.personRaceId = personRaceId;
        this.ratingCount = ratingCount;
    }

}
