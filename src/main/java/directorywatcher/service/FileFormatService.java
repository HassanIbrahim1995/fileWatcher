package directorywatcher.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FileFormatService {

    public String getFileContent(Path filePath) throws IOException {
        String fileExtension = getFileExtension(filePath);

        if (StringUtils.hasText(fileExtension)) {
            StringBuilder contentBuilder = new StringBuilder();
            String fileContent = Files.readString(filePath);

            switch (fileExtension) {
                case "txt" -> contentBuilder.append("Text File Content:\n").append(fileContent);
                case "csv" -> contentBuilder.append("CSV File Content:\n").append(fileContent);
                case "xml" -> contentBuilder.append("XML File Content:\n").append(fileContent);
                case "json" -> contentBuilder.append("JSON File Content:\n").append(fileContent);
                case "docx" -> contentBuilder.append("Word (DOCX) File Content:\n").append(fileContent);
                case "pdf" -> contentBuilder.append("PDF File Content:\n");
                default -> contentBuilder.append("Unsupported file format.");
            }
            return contentBuilder.toString();
        } else {
            return "Invalid file.";
        }
    }

    private String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1);
        }
        return null;
    }
}

