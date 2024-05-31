package blps.duo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("leader")
@AllArgsConstructor
public class Leader {

    @Column("email")
    private String email;
    @Id
    @Column("race_id")
    private Long raceId;
    @Column("race_title")
    private String raceTitle;
}
