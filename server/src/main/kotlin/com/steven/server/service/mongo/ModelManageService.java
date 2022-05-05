package com.steven.server.service.mongo;

import com.steven.server.model.mongo.ModelManagePO;
import com.steven.server.repository.mongo.ModelManageDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ModelManageService {

    @Autowired
    private ModelManageDAO modelManageDAO;

    @Autowired
    private MongoTemplate mongoTemplate;

    public ModelManagePO save(ModelManagePO modelManagePO) {
        return modelManageDAO.save(modelManagePO);
    }

    public Optional<ModelManagePO> findByIp(String ip) {
        Query query = new Query();
        query.addCriteria(Criteria.where("ip").is(ip));

        ModelManagePO po = mongoTemplate.findOne(query, ModelManagePO.class);

        return po == null ? Optional.empty() : Optional.of(po);
    }

    public ModelManagePO updateCount(ModelManagePO modelManagePO, String todayStr) {
        Map<String, Integer> dateCounts = modelManagePO.getDateCounts();
        dateCounts.put(todayStr, dateCounts.get(todayStr) + 1);

        int countTotal = modelManagePO.getCountTotal() + 1;
        modelManagePO.setCountTotal(countTotal);

//        Query query = new Query();
//        query.addCriteria(Criteria.where("ip").is(modelManagePO.getIp()));
//        Update update = new Update();
//        update.set("countTotal", modelManagePO.getCountTotal());
//        mongoTemplate.updateMulti(query, update, ModelManagePO.class);

        return modelManageDAO.save(modelManagePO);
    }
}
