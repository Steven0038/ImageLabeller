package com.steven.server.repository.redis;

import com.steven.server.model.ModelFilePO;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModelFileDAO extends CrudRepository<ModelFilePO, String> {
}
