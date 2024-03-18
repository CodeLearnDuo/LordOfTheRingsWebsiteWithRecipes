package blps.duo.project.converters;

import blps.duo.project.dto.ApiToken;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class StringToApiTokenConverter implements Converter<String, ApiToken> {

    @Override
    public ApiToken convert(@NonNull String source) throws NumberFormatException {
        return new ApiToken(Long.parseLong(source));
    }
}
