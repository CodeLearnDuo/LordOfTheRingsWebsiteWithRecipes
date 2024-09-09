package blps.duo.project.dto.responses;

import java.util.Arrays;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

import java.util.Base64;

@Data
public class ShortRecipeResponse {
    private Long id;
    private String title;
    private byte[] logo;
    private RaceResponse race;
    private double rank;

    public ShortRecipeResponse(Long id, String title, byte[] logo, RaceResponse race, double rank) {
        this.id = id;
        this.title = title;
        this.logo = logo;
        this.race = race;
        this.rank = rank;
    }


    @JsonProperty("logo")
    public String getLogoBase64() {
        return Base64.getEncoder().encodeToString(logo);
    }

    @JsonProperty("logo")
    public void setLogoBase64(String logoBase64) {
        this.logo = Base64.getDecoder().decode(logoBase64);
    }
}

