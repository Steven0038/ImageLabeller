package com.steven.server.model.mongo;

import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.Map;

public class ModelManagePO {

    @Id
    private String id;

    private String ip;
    private String modelId;
    private String modelName;
    private String countryName;
    private String cityName;
    private int postal;
    private Map<String, Integer> dateCounts;
    private int countTotal;
    private Date createTime;
    private Date updateTime;

    public Map<String, Integer> getDateCounts() {
        return dateCounts;
    }

    public void setDateCounts(Map<String, Integer> dateCounts) {
        this.dateCounts = dateCounts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getPostal() {
        return postal;
    }

    public void setPostal(int postal) {
        this.postal = postal;
    }

    public int getCountTotal() {
        return countTotal;
    }

    public void setCountTotal(int countTotal) {
        this.countTotal = countTotal;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}