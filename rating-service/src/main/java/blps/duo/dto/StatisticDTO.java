package blps.duo.dto;

import java.io.Serializable;

public record StatisticDTO(
        StatisticPerson person,
        StatisticRecipe recipe,
        double rankChange,
        long timestamp
) implements Serializable {
}
