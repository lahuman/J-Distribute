package kr.pe.lahuman.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kr.pe.lahuman.ui.DistributeUI;
import kr.pe.lahuman.utils.BuilderConstant;
import kr.pe.lahuman.utils.BuilderUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

public class FTPClientImpl implements Client {
	Logger log = DistributeUI.log;
	
	private FTPClient client = null;
	
	@Override
	public void connect() throws Exception{

		client = new FTPClient();
		client.setControlEncoding(BuilderConstant.ENCODE_TYPE);

		FTPClientConfig config = new FTPClientConfig();
		client.configure(config);
		client.connect(BuilderConstant.FTP_URL, BuilderConstant.FTP_PORT);
		int reply = client.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			client.disconnect();
		    throw new Exception("FTP server refused connection.");
		}  else {
		    log.debug("Connect successful");
		}

		 boolean login = client
				.login(BuilderConstant.FTP_USERNAME,
						BuilderConstant.FTP_PASSWORD);

		 if(login){
			 log.debug("login succesful");
		}else{
			throw new Exception("LOGIN FAIL.. CHECK YOUR ID OR PASSWORD");
		}
		/*
		 *	FTP.BINARY_FILE_TYPE, FTP.ASCII_FILE_TYPE, FTP.EBCDIC_FILE_TYPE,
		 *	FTP.IMAGE_FILE_TYPE , FTP.LOCAL_FILE_TYPE
		 */
		client.setFileType(FTP.BINARY_FILE_TYPE);

		/*
		 *	ftpClient.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
		 *	FTP.BLOCK_TRANSFER_MODE, FTP.COMPRESSED_TRANSFER_MODE
		 */
//		client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
//		client.setBufferSize(80000);
		
		client.setKeepAlive(true);
		client.setControlKeepAliveTimeout(30);
//		ftp.setControlKeepAliveReplyTimeout(Integer.MAX_VALUE);
//		ftp.setDataTimeout(Integer.MAX_VALUE);
		client.setBufferSize(1024000);
//		client.addProtocolCommandListener(new PrintCommandListener(System.out, true));
//		client.setCopyStreamListener(new CopyStreamListener() {
//			private long megsTotal=0;
//			@Override
//			public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
//				long megs =totalBytesTransferred/100000;
//				for(long i = megsTotal; i<megs; i++){
//					System.out.print("#");
//				}
//				megsTotal = megs;
//			}
//			
//			@Override
//			public void bytesTransferred(CopyStreamEvent event) {
//				bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
//			}
//		});
	}

	@Override
	public void disconnect() {
		if(client != null)
		try {
			client.logout();
			if (client.isConnected()) {
				client.disconnect();
			}
			client = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void backup(String sourcePath, String fixBackupPath) {
		try {
			File getFile = new File(fixBackupPath+sourcePath);
				BuilderUtil.makeDirs(fixBackupPath+sourcePath);
			
			if(sourcePath.indexOf("*") != -1){
				String realPath = sourcePath.substring(0, sourcePath.length()-1);
				String backupPath = fixBackupPath + realPath ;
				FTPFile[] rfile = client.listFiles(BuilderConstant.UPLOAD_PATH+realPath);
				
				backupDirectoy(realPath, backupPath,
						rfile);
			}else{
				OutputStream outputStream = new FileOutputStream(getFile);
				log.debug("BACKUP FILE = "+BuilderConstant.UPLOAD_PATH+sourcePath);
				boolean result = client.retrieveFile(BuilderConstant.UPLOAD_PATH+sourcePath, outputStream);
				outputStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void backupDirectoy(
			String realPath, String backupPath,
			FTPFile[] rfile) throws IOException,
			FileNotFoundException {
		for(int f = 0; f<rfile.length; f++){
			if(rfile[f].isDirectory()){
				
				String realPath2 = realPath + rfile[f].getName() + "/";
				String backupPath2 = backupPath + rfile[f].getName() + "/";
				BuilderUtil.makeDirs(backupPath2);
				FTPFile[] rfile2 = client.listFiles(BuilderConstant.UPLOAD_PATH+realPath2);
				backupDirectoy(realPath2, backupPath2,
						rfile2);
			}else{
				String fileName  = rfile[f].getName();
				OutputStream outputStream = new FileOutputStream(backupPath+fileName);
				log.debug("BACKUP FILE = "+BuilderConstant.UPLOAD_PATH+realPath +fileName);
				boolean result = client.retrieveFile((BuilderConstant.UPLOAD_PATH+realPath +fileName), outputStream);
				outputStream.close();
			}
		}
		
		return;
	}
	
	@Override
	public void upload(String sourcePath,String fixSourcePath) {
		try {
			File putFile = new File(fixSourcePath+sourcePath);
			client.changeWorkingDirectory(BuilderConstant.UPLOAD_PATH);
			
			String[] dirList = sourcePath.substring(0, sourcePath.lastIndexOf("/")).split("/");
			String tmpDir = "";
			for(int f=1; f<dirList.length ; f++){
				tmpDir +=dirList[f] +"/";
				if(client.listFiles(tmpDir).length == 0){
					log.debug("CREATE DIRECTORY : "+tmpDir);
					client.makeDirectory(tmpDir);
				}
			}
	
			if(sourcePath.indexOf("*") != -1){
				String realPath = sourcePath.substring(0, sourcePath.length()-1);
				File directory = new File(fixSourcePath + realPath );
				String directoryPath = fixSourcePath + realPath ;
				
				if(directory.isDirectory()){
					String[] rfile = directory.list();
					uploadDirectory(realPath,
							directoryPath, rfile);
				}
			}else{
				InputStream inputStream = new FileInputStream(putFile);
				boolean result = client.storeFile(BuilderConstant.UPLOAD_PATH+sourcePath, inputStream);
				log.debug("UPLOAD FILE = "+BuilderConstant.UPLOAD_PATH+sourcePath +" : "+((result)?"SUCCESS":"FAIL"));
				inputStream.close();
			}
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void uploadDirectory(String realPath, String directoryPath,
			String[] rfile) throws IOException,
			FileNotFoundException {
		for(int f = 0; f<rfile.length; f++){
			String fileName  = rfile[f];
			File checkDir = new File(directoryPath+"/"+fileName);
			if(checkDir.isDirectory()){
				if(client.listFiles(realPath+ "/" +  fileName).length == 0){
					client.makeDirectory(realPath+ "/" +  fileName);
				}
				
				String realPath2 = realPath +  fileName+"/";
				String directoryPath2 = directoryPath+"/"+fileName ;
				String[] rfile2 = checkDir.list();
				
				uploadDirectory(realPath2,
						directoryPath2, rfile2);
				
			}else{
				InputStream inputStream = new FileInputStream(directoryPath+"/"+fileName);
				boolean result = client.storeFile(BuilderConstant.UPLOAD_PATH+realPath +fileName, inputStream);
				log.debug("UPLOAD FILE = "+BuilderConstant.UPLOAD_PATH+realPath +fileName +" : "+((result)?"SUCCESS":"FAIL"));
				inputStream.close();
			}
		}
		
		return;
	}
}
