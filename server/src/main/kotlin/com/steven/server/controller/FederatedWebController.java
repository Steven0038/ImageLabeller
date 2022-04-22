package com.steven.server.controller;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.RateLimiter;
import com.steven.server.core.FederatedAveragingStrategy;
import com.steven.server.core.datasource.*;
import com.steven.server.core.domain.model.FederatedServer;
import com.steven.server.core.domain.model.RoundController;
import com.steven.server.core.domain.model.UpdatesStrategy;
import com.steven.server.core.domain.model.UpdatingRound;
import com.steven.server.core.domain.repository.ServerRepository;
import com.steven.server.model.ModelFilePO;
import com.steven.server.response.ResponseHandler;
import com.steven.server.service.IpCheckerService;
import com.steven.server.service.redis.ModelFileService;
import com.steven.server.util.CacheKey;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/service/federatedservice")
public class FederatedWebController {
    private static final Logger log = LoggerFactory.getLogger(FederatedWebController.class);

    private static FederatedServer federatedServer;

    RateLimiter rateLimiter = RateLimiter.create(100.0);

    private final ModelFileService modelFileService;

    private final IpCheckerService ipCheckerService;

    private final StringRedisTemplate stringRedisTemplate;

    private final String modelDir;

    private final String timeWindow;

    private final String minUpdates;

    private final String layerIndex;

    private final String tmpModelCacheId = "modelCache123456"; // TODO: assign diff value by request's model type

    /**
     * initialize classes and file data source when server start
     */
    @Autowired
    public FederatedWebController(
            @Value("${model_dir}") String modelDir,
            @Value("${time_window}") String timeWindow,
            @Value("${min_updates}") String minUpdates,
            @Value("${layer_index}") String layerIndex,
            ModelFileService modelFileService,
            IpCheckerService ipCheckerService,
            StringRedisTemplate stringRedisTemplate
    ) throws IOException {
        this.modelDir = modelDir;
        this.timeWindow = timeWindow;
        this.minUpdates = minUpdates;
        this.layerIndex = layerIndex;
        this.modelFileService = modelFileService;
        this.ipCheckerService = ipCheckerService;
        this.stringRedisTemplate = stringRedisTemplate;

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

            // TODO clean all redis cache
            modelFileService.delete();

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
     * client POST it's on-device-trained model gradients
     *
     * @param multipartFile model gradients
     * @param samples       the number of image size to train this model gradients
     * @return [Boolean] update efficient or not
     */
    @PostMapping("model")
    public Boolean pushGradient(
            final @RequestParam("file") MultipartFile multipartFile,
            final @RequestParam("samples") int samples,
            HttpServletRequest httpServletRequest
    ) {
        log.info("[pushGradient]...");

        try {
            if (multipartFile == null) {
                return false;
            } else {
                byte[] bytes = IOUtils.toByteArray(multipartFile.getInputStream());

                if (isCacheExists(bytes)) return false; // reject those requests already exist in redis cache

                // TODO check region restrict
                if (ipCheckerService.isShallNotPass(httpServletRequest.getRemoteAddr())) return false;
//                if (ipCheckerService.isShallNotPass("95.173.136.162")) return false; // FIXME test

                federatedServer.pushUpdate(bytes, samples);

                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * compare update model param hashes to redis, if not exists, push file hash value to redis
     *
     * @param bytes
     * @return [Boolean] cache already exists the same file hash or not
     * @throws IOException
     */
    private boolean isCacheExists(byte[] bytes) throws IOException {
        File targetFile = new File("src/main/resources/targetFile" + UUID.randomUUID() + ".tmp");
        com.google.common.io.Files.write(bytes, targetFile);
        HashCode hashCode = com.google.common.io.Files.hash(targetFile, Hashing.sha1());
        String modelParamsHash = hashCode.toString();

        targetFile.delete();

        String hashStr = stringRedisTemplate.opsForValue().get(CacheKey.PARAM_KEY.getKey() + modelParamsHash);
        if (hashStr == null) {
            log.info("[pushGradient] params: {}. not found in cache, add new one", modelParamsHash);
            stringRedisTemplate.opsForValue().set(CacheKey.PARAM_KEY.getKey() + modelParamsHash, modelParamsHash, 3600, TimeUnit.SECONDS);
        } else {
            log.info("[pushGradient] params: {}. already exists, not do following logic", modelParamsHash);

            return true;
        }

        return false;
    }

    /**
     * client get model if client side have no embedded model exits when app starts
     * <p>
     * TODO get model type by request param
     *
     * @return model data file
     */
    @GetMapping("model")
    public ResponseEntity<Object> getFile() throws IOException {
        log.info("[getFile]...");

        if (!rateLimiter.tryAcquire(1000, TimeUnit.MILLISECONDS)) {
            log.warn("too many request, please retry later!");
            return new ResponseHandler().generateResponse("too many request, please retry later!", HttpStatus.TOO_MANY_REQUESTS, null);
        }

        File file;
        ModelFilePO modelFilePO;

        // get model file from redis first, if not exist, get from system and put to redis
        Optional<ModelFilePO> fileEntityOptional = modelFileService.getFile(CacheKey.MODEL_KEY.getKey() + tmpModelCacheId);
        if (fileEntityOptional.isEmpty()) {
            log.info("[getFile] cache not found, get from system and set cache...");
            file = federatedServer.getModelFile();
            modelFileService.save(file, "testModelCacheName", CacheKey.MODEL_KEY.getKey() + tmpModelCacheId);
            modelFilePO = new ModelFilePO();
            modelFilePO.setData(Files.readAllBytes(file.toPath()));
        } else {
            modelFilePO = fileEntityOptional.get();
        }

        String fileName = federatedServer.getUpdatingRound().getModelVersion() + ".zip";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
//                .contentType(MediaType.valueOf(fileEntity.getContentType()))
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(modelFilePO.getData());
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
