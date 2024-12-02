package blps.duo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("leader")
@AllArgsConstructor
@NoArgsConstructor
public class Leader {

    @Id
    @Column("statistic_leader_id")
    private Long statisticLeaderId;
    @Column("email")
    private String email;
    @Column("race_id")
    private Long raceId;
    @Column("race_title")
    private String raceTitle;

    public Leader(String email, Long raceId, String raceTitle) {
        this.email = email;
        this.raceId = raceId;
        this.raceTitle = raceTitle;
    }
}
