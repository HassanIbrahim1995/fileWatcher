package directorywatcher.service;

import directorywatcher.model.FileDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectoryWatchService {
    private final FileFormatService fileFormatService;
    public WatchService watchService;
    private volatile boolean stopFlag = false;
    private String directoryPath;
    private String watchPath;

    /**
     * Starts watching the specified directory for file create, delete, and modify events.
     *
     * @param watchPath The path of the directory to watch.
     */
    public void startWatching(String watchPath) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            this.watchPath = watchPath;
            Path path = Paths.get(watchPath);
            path.register(
                    watchService,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY
            );
            Thread watchThread = new Thread(() -> {
                try {
                    runDirectoryWatch();
                } catch (InterruptedException e) {
                    log.error("Interrupted while watching directory: {}", watchPath, e);
                }
            });
            watchThread.start();
        } catch (IOException e) {
            log.error("IOException", e);
        }
    }

    /**
     * Polls the directory for file create, delete, and modify events.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for events.
     */
    public void pollDirectory() throws InterruptedException {
        WatchKey key;
        while ((key = watchService.poll()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                handleFileEvent(event);
                break;
            }
            key.reset();
        }
    }

    /**
     * Handles a file event by determining the event kind and logging the event information.
     * If it's a create or modify event, it also calls the printFileOnCreateOrModify method.
     *
     * @param event The WatchEvent representing the file event.
     */
    private void handleFileEvent(WatchEvent<?> event) {
        String eventKind = "unknown";
        if (event.kind() == ENTRY_CREATE || event.kind() == ENTRY_MODIFY) {
            eventKind = "create or modify";
            logEvent(eventKind, event.context());
            printFileOnCreateOrModify(event, watchPath);
        } else if (event.kind() == ENTRY_DELETE) {
            eventKind = "delete";
            logEvent(eventKind, event.context());
        }
    }

    /**
     * Logs the file event information.
     *
     * @param eventKind The kind of file event (e.g., create, modify, delete).
     * @param file      The affected file.
     */
    private void logEvent(String eventKind, Object file) {
        log.info("Event kind: {}. File affected: {}.", eventKind, file);
    }


    /**
     * Prints the file information when a file is created or modified.
     *
     * @param event         The WatchEvent representing the file event.
     * @param directoryPath The path of the watched directory.
     */
    private void printFileOnCreateOrModify(WatchEvent<?> event, String directoryPath) {
        String fileName = event.context().toString();
        Path filePath = Paths.get(directoryPath + "/" + fileName);
        try {
            String fileContent = fileFormatService.getFileContent(filePath);
            System.out.println("File created: " + fileName);
            System.out.println("File content: " + fileContent);
        } catch (IOException e) {
            System.out.println("Failed to read file: " + fileName);
        }
    }

    /**
     * Stops watching the directory.
     */
    public void stopWatching() {
        stopFlag = true;
    }

    /**
     * Runs the directory watch loop until stopFlag is set.
     *
     * @throws InterruptedException if the thread is interrupted while waiting for events.
     */
    public void runDirectoryWatch() throws InterruptedException {
        while (!stopFlag) {
            pollDirectory();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public FileDetails getFileDetails() {
        String fileName = "";
        String action = "";
        String fileContent = "";
        return new FileDetails(fileName, action, fileContent);
    }
}

