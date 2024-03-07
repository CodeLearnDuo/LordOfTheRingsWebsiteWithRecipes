package blps.duo.project.model;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
public class Statistic {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @ManyToOne
    private Person from;
    @ManyToOne
    private Recipe to;
    private Timestamp at;

    @PrePersist
    private void onCreate() {
        at = new Timestamp(System.currentTimeMillis());
    }
}
