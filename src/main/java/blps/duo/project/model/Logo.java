package blps.duo.project.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@Setter
public class Logo implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String id;
    private final byte[] image;

}
