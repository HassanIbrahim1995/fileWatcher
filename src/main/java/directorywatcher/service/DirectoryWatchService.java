package directorywatcher;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

@Service
@Slf4j
public class DirectoryWatchService {

    private WatchService watchService;

    @Value("${watch.path}")
    private String watchPath;
    @PostConstruct
    public void init() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(watchPath);
            path.register(
                    watchService,
                    ENTRY_CREATE,
                    ENTRY_DELETE,
                    ENTRY_MODIFY
            );
            Thread watchThread = new Thread(this::runDirectoryWatch);
            watchThread.start();
        } catch (IOException e) {
            log.error("IOException", e);
        }
    }


    public void pollDirectory() {
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

    private void runDirectoryWatch() {
        while (true) {
            pollDirectory();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}

