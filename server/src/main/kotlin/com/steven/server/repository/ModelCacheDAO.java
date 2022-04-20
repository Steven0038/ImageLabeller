package com.steven.server.repository;

import com.steven.server.model.ModelFileEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelCacheDAO extends CrudRepository<ModelFileEntity, String> {
}
