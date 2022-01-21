package meshotron2.monitor;

public interface TransferFile {
    
    boolean downloadFile(String localFilePath, String removeFilePath);

    boolean uploadFile(String localFilePath, String removeFilePath);
}
