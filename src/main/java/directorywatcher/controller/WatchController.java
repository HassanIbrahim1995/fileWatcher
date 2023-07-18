package directorywatcher.controller;

import directorywatcher.requestObject.WatchDirectoryRequest;
import directorywatcher.service.DirectoryWatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class WatchController {

    private final DirectoryWatchService directoryWatchService;
    @PostMapping("/watch-directory")
    public ResponseEntity<String> watchDirectory(@RequestBody WatchDirectoryRequest request) {
        String path = request.getPath();
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Path must not be null or empty");
        }
        directoryWatchService.startWatching(path);
        return new ResponseEntity<>("Now watching directory: " + path, HttpStatus.OK);
    }

}
