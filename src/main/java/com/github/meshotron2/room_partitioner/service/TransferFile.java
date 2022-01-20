package com.github.meshotron2.room_partitioner.service;

public interface TransferFile {

    boolean downloadFile(String localFilePath, String removeFilePath);

    boolean uploadFile(String localFilePath, String removeFilePath);
}
