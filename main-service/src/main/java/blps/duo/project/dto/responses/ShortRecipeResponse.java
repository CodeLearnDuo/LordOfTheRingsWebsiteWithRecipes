package blps.duo.project.dto.responses;

import java.util.Arrays;
import java.util.Objects;

public record ShortRecipeResponse(
        Long id,
        String title,
        byte[] logo,
        RaceResponse raceName,
        double rank
) {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortRecipeResponse that = (ShortRecipeResponse) o;
        return Double.compare(rank, that.rank) == 0 && Objects.equals(id, that.id) && Objects.equals(title, that.title) && Arrays.equals(logo, that.logo) && Objects.equals(raceName, that.raceName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, title, raceName, rank);
        result = 31 * result + Arrays.hashCode(logo);
        return result;
    }

    @Override
    public String toString() {
        return "ShortRecipeResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", logo=" + Arrays.toString(logo) +
                ", raceName=" + raceName +
                ", rank=" + rank +
                '}';
    }
}
