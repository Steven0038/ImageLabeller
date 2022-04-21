package com.steven.server.service;

import com.steven.server.model.ModelFileEntity;
import com.steven.server.repository.ModelCacheDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ModelCacheService {

    private final ModelCacheDAO modelCacheDAO;

    @Autowired
    public ModelCacheService(ModelCacheDAO modelCacheDAO) {
        this.modelCacheDAO = modelCacheDAO;
    }

    public ModelFileEntity save(MultipartFile file) throws IOException {
        ModelFileEntity modelFileEntity = new ModelFileEntity();
        modelFileEntity.setId(UUID.randomUUID().toString());
        modelFileEntity.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        modelFileEntity.setContentType(file.getContentType());
        modelFileEntity.setData(file.getBytes());
        modelFileEntity.setSize(file.getSize());

        return modelCacheDAO.save(modelFileEntity);
    }

    public ModelFileEntity save(MultipartFile file, String uid) throws IOException {
        ModelFileEntity modelFileEntity = new ModelFileEntity();
        modelFileEntity.setId(uid);
        modelFileEntity.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        modelFileEntity.setContentType(file.getContentType());
        modelFileEntity.setData(file.getBytes());
        modelFileEntity.setSize(file.getSize());

        return modelCacheDAO.save(modelFileEntity);
    }

    public void save(File file, String fileName, String uid) throws IOException {
        MultipartFile result = new MockMultipartFile(fileName,
                fileName, String.valueOf(MediaType.parseMediaType("application/octet-stream")), Files.readAllBytes(file.toPath()));

        this.save(result, uid);
    }

    public Optional<ModelFileEntity> getFile(String id) {
        return modelCacheDAO.findById(id);
    }

    public void delete() {
        modelCacheDAO.deleteAll();
    }
}
