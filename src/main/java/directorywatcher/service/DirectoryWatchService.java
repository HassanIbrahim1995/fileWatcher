package directorywatcher.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
public class DirectoryWatchService {
    public WatchService watchService;
    private volatile boolean stopFlag = false;

    public void startWatching(String watchPath) {
        try {
            watchService = FileSystems.getDefault().newWatchService();
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

    public void pollDirectory() throws InterruptedException {
        WatchKey key;
        while ((key = watchService.poll()) != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                String eventKind = "unknown";
                if (event.kind() == ENTRY_CREATE) {
                    eventKind = "create";
                } else if (event.kind() == ENTRY_DELETE) {
                    eventKind = "delete";
                } else if (event.kind() == ENTRY_MODIFY) {
                    eventKind = "modify";
                }
                log.info("Event kind: {}. File affected: {}.", eventKind, event.context());
            }
            key.reset();
        }
    }

    public void stopWatching() {
        stopFlag = true;
    }

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
}

