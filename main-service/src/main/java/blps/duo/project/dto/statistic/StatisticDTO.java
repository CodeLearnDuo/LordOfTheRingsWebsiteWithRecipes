package blps.duo.project.dto.statistic;

import java.io.Serializable;

public record StatisticDTO(
        StatisticPerson person,
        StatisticRecipe recipe,
        double rankChange,
        long timestamp
) implements Serializable {
}
