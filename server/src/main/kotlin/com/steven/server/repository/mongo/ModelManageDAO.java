package com.steven.server.repository.mongo;

import com.steven.server.model.mongo.ModelManagePO;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface ModelManageDAO extends MongoRepository<ModelManagePO, String> {

}
