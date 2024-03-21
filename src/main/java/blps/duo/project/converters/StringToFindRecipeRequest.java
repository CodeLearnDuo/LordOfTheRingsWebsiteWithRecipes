package blps.duo.project.converters;

import blps.duo.project.dto.requests.FindRecipeRequest;
import org.springframework.core.convert.converter.Converter;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToFindRecipeRequest implements Converter<String, FindRecipeRequest> {

    @Override
    public FindRecipeRequest convert(@NonNull String recipeName) {
        return new FindRecipeRequest(recipeName);
    }
}
