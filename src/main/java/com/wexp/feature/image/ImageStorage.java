package com.wexp.feature.image;

import org.springframework.core.io.Resource;

import java.io.InputStream;

public interface ImageStorage {
    void store(String storageKey, InputStream input) throws Exception;
    Resource load(String storageKey);
    void delete(String storageKey);
    long size(String storageKey) throws Exception;
}
