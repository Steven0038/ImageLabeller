package com.steven.server.service.redis;

import com.steven.server.model.ModelFilePO;
import com.steven.server.repository.ModelFileDAO;
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
public class ModelFileService {

    private final ModelFileDAO modelFileDAO;

    @Autowired
    public ModelFileService(ModelFileDAO modelFileDAO) {
        this.modelFileDAO = modelFileDAO;
    }

    public ModelFilePO save(MultipartFile file) throws IOException {
        ModelFilePO modelFilePO = new ModelFilePO();
        modelFilePO.setId(UUID.randomUUID().toString());
        modelFilePO.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        modelFilePO.setContentType(file.getContentType());
        modelFilePO.setData(file.getBytes());
        modelFilePO.setSize(file.getSize());

        return modelFileDAO.save(modelFilePO);
    }

    public ModelFilePO save(MultipartFile file, String uid) throws IOException {
        ModelFilePO modelFilePO = new ModelFilePO();
        modelFilePO.setId(uid);
        modelFilePO.setName(StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename())));
        modelFilePO.setContentType(file.getContentType());
        modelFilePO.setData(file.getBytes());
        modelFilePO.setSize(file.getSize());

        return modelFileDAO.save(modelFilePO);
    }

    public void save(File file, String fileName, String uid) throws IOException {
        MultipartFile result = new MockMultipartFile(fileName,
                fileName, String.valueOf(MediaType.parseMediaType("application/octet-stream")), Files.readAllBytes(file.toPath()));

        this.save(result, uid);
    }

    public Optional<ModelFilePO> getFile(String id) {
        return modelFileDAO.findById(id);
    }

    public void delete() {
        modelFileDAO.deleteAll();
    }
}
