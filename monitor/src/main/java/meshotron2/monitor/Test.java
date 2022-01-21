package meshotron2.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Test implements CommandLineRunner {

	@Autowired
	private TransferFile fileTransferService;
	
	private Logger logger = LoggerFactory.getLogger(Test.class);
	
	@Override
	public void run(String... args) throws Exception {
		logger.info("Start download file");
		boolean isDownloaded = fileTransferService.downloadFile("Transferências/http.txt", "/readme.txt");
		logger.info("Download result: " + String.valueOf(isDownloaded));
		
		logger.info("Start upload file");
		boolean isUploaded = fileTransferService.uploadFile("/Transferências/http.txt", "/readme2.txt");
		logger.info("Upload result: " + String.valueOf(isUploaded));
	}

}