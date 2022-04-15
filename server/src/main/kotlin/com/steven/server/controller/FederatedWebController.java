package com.steven.server.controller;

import com.steven.server.core.datasource.FileDataSource;
import com.steven.server.core.datasource.MemoryDataSource;
import com.steven.server.core.FederatedAveragingStrategy;
import com.steven.server.core.datasource.FileDataSourceImpl;
import com.steven.server.core.datasource.MemoryDataSourceImpl;
import com.steven.server.core.datasource.ServerRepositoryImpl;
import com.steven.server.core.domain.model.FederatedServer;
import com.steven.server.core.domain.model.RoundController;
import com.steven.server.core.domain.model.UpdatesStrategy;
import com.steven.server.core.domain.model.UpdatingRound;
import com.steven.server.core.domain.repository.ServerRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

@RestController
@RequestMapping("/service/federatedservice")
public class FederatedWebController {
    private static final Logger log = LoggerFactory.getLogger(FederatedWebController.class);

    private static FederatedServer federatedServer;

    private final String modelDir;

    private final String timeWindow;

    private final String minUpdates;

    private final String layerIndex;


    /**
     * initialize classes and file data source when server start
     */
    @Autowired
    public FederatedWebController(
            @Value("${model_dir}") String modelDir,
            @Value("${time_window}") String timeWindow,
            @Value("${min_updates}") String minUpdates,
            @Value("${layer_index}") String layerIndex
    ) throws IOException {
        this.modelDir = modelDir;
        this.timeWindow = timeWindow;
        this.minUpdates = minUpdates;
        this.layerIndex = layerIndex;

        if (federatedServer == null) {
            // TODO Inject!
            // TODO Properties to SharedConfig
            Properties properties = new Properties();
            properties.load(new FileInputStream("./server/src/main/resources/application.properties"));

            java.nio.file.Path rootPath = Paths.get(Objects.requireNonNull(modelDir));
            FileDataSource fileDataSource = new FileDataSourceImpl(rootPath);
            MemoryDataSource memoryDataSource = new MemoryDataSourceImpl();
            ServerRepository repository = new ServerRepositoryImpl(fileDataSource, memoryDataSource);
            UpdatesStrategy updatesStrategy = new FederatedAveragingStrategy(repository, Integer.parseInt(Objects.requireNonNull(layerIndex)));

            UpdatingRound currentUpdatingRound = repository.retrieveCurrentUpdatingRound();

            long timeWindowValue = Long.parseLong(Objects.requireNonNull(timeWindow));
            int minUpdatesValue = Integer.parseInt(Objects.requireNonNull(minUpdates));

            RoundController roundController = new BasicRoundController(repository, currentUpdatingRound, timeWindowValue, minUpdatesValue);

            federatedServer = FederatedServerImpl.Companion.getInstance();
            federatedServer.initialise(repository, updatesStrategy, roundController, System.out::println, properties);

            // We're starting a new round when the server starts
            roundController.startRound();
        }
    }

    /**
     * server health check
     *
     * @return [String] is server available
     */
    @GetMapping("available")
    public String available() {
        log.info("[available]...");
        return "yes";
    }

    /**
     * client POST on device trained model gradients
     * @param multipartFile model gradients
     * @param samples the number of image size to train this model gradients
     * @return [Boolean] update efficient or not
     */
    @PostMapping("model")
    public Boolean pushGradient(
            final @RequestParam("file") MultipartFile multipartFile,
            final @RequestParam("samples") int samples
    ) {
        log.info("[pushGradient]...");

        try {
            if (multipartFile == null) {
                return false;
            } else {
                byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());

                // TODO compare hash to redis, if not exists, push to redis [hash: file]

                federatedServer.pushUpdate(bytes, samples);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * client get model if client side have no embedded model exits when app starts
     *
     * @return model data file
     */
    @GetMapping("model")
    public ResponseEntity<ByteArrayResource> getFile() {
        log.info("[getFile]...");

        File file = federatedServer.getModelFile();
        String fileName = federatedServer.getUpdatingRound().getModelVersion() + ".zip";

        HttpHeaders header = new HttpHeaders();
        header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        header.add("Cache-Control", "no-cache, no-store, must-revalidate");
        header.add("Pragma", "no-cache");
        header.add("Expires", "0");

        ByteArrayResource resource = null;
        try {
            Path path = Paths.get(file.getAbsolutePath());
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok()
                .headers(header)
                .contentLength(file.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    /**
     * get the current training round data, from currentRound.json
     *
     * @return [String] current round
     */
    @GetMapping("currentRound")
    public String getCurrentRound() {
        log.info("[getCurrentRound]...");

        return federatedServer.getUpdatingRoundAsJson();
    }


}