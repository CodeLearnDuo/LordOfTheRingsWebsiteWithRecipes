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

    @Id
    @Column("statistic_leader_id")
    private Long statisticLeaderId;
    @Column("email")
    private String email;
    @Column("race_id")
    private Long raceId;
    @Column("race_title")
    private String raceTitle;
}
