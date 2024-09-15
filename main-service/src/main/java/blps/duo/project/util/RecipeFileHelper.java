package blps.duo.project.util;

import org.camunda.bpm.engine.variable.value.FileValue;
import org.camunda.bpm.engine.variable.Variables;

public class RecipeFileHelper {

    public static FileValue buildFile(String filename, byte[] fileContent, String mimeType) {
        return Variables
                .fileValue(filename)
                .file(fileContent)
                .mimeType(mimeType)
                .create();
    }
}
