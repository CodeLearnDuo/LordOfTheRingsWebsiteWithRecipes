package blps.duo.model;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ExpandedStatistic {

    private Long id;
    private double value;
    private Timestamp at;

    private String personEmail;

    private Long personRaceId;
    private Long personRatingCount;

    private Long recipeId;

    private String recipeTitle;
    private Long recipeRaceId;
    private double recipeRating;

}
