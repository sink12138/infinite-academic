package com.buaa.academic.resource.schedule;

import com.buaa.academic.resource.service.TokenService;
import com.buaa.academic.resource.util.FolderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

import static com.buaa.academic.resource.service.TokenService.TOKEN_LOCK;

@Component
@EnableScheduling
public class TrashCleaner {

    private static final Logger logger = LoggerFactory.getLogger(TrashCleaner.class);

    @Autowired
    private TokenService tokenService;

    @Value("${resource.storage-directory}")
    private String directory;

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanTrash() throws IOException {
        logger.info("Trash cleaning started");
        synchronized (TOKEN_LOCK) {
            File dir = new File(directory).getAbsoluteFile();
            if (!dir.exists() && !dir.mkdirs())
                throw new IOException("cannot create directory");
            File[] folders = dir.listFiles();
            if (folders == null)
                throw new IOException("unexpected null folder list");
            long threshold = System.currentTimeMillis() + Duration.ofDays(30).toMillis();
            for (File folder : folders) {
                String token = folder.getName();
                try {
                    long lastModified = folder.lastModified();
                    if (lastModified > threshold) {
                        FolderUtils.removeFolder(folder);
                        tokenService.removeToken(token);
                        logger.info("Removed: " + token);
                    }
                    else if (lastModified == 0L)
                        throw new IOException("last modified time millis is 0L");
                }
                catch (Exception exception) {
                    logger.warn("Failed to remove " + token);
                    logger.warn("Exception occurred: " + exception.getMessage());
                }
            }
        }
        logger.info("Trash cleaning finished");
    }

}
