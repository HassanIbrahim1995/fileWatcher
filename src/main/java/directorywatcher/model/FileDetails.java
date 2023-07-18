package directorywatcher.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FileDetails {
    private final String fileName;
    private final String action;
    private final String fileContent;
}
