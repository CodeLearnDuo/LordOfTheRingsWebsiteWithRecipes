package blps.duo.project.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@Table("recipe")
public class Recipe {

    @Id
    private Long id;
    private String title;
    private String description;
    @Lob
    private byte[] logo;
    private Race race;
    private double rank;

}
