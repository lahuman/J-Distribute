package kr.pe.lahuman.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import kr.pe.lahuman.utils.BuilderConstant;
import kr.pe.lahuman.utils.BuilderUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;


public class BuilderMain {


	static Logger log = Logger.getLogger(BuilderMain.class);

	static{
		String layout = "%d %-5p [%t] %-17c{2} (%13F:%L) %3x - %m%n";
		String logfilename = "DailyLog.log";
		String datePattern = ".yyyy-MM-dd ";

		PatternLayout patternlayout = new PatternLayout(layout);
		DailyRollingFileAppender appender = null;
		ConsoleAppender consoleAppender = null;
		try {
			appender = new DailyRollingFileAppender(patternlayout, logfilename, datePattern);
			consoleAppender = new ConsoleAppender(patternlayout);

		} catch (IOException e) {
			e.printStackTrace();
		}
		log.addAppender(appender);
		log.addAppender(consoleAppender);
		log.setLevel(Level.DEBUG);
	}

	private BuilderMain(){
		
	}
	/**
	 * @param client
	 * @throws Exception
	 */
	private static void testCase(FTPClient client) throws Exception{
		client.changeWorkingDirectory(BuilderConstant.UPLOAD_PATH);
		if(client.listFiles("abc").length == 0){
			client.makeDirectory("abc");

		}else{
			for(FTPFile file : client.listFiles("abc")){
				log.debug(file.getName());
			}
			log.debug("No Make");
		}

	}

	/**
	 * @param client
	 * @throws IOException
	 */
	public static void disconnectFTP(FTPClient client) throws IOException {
		client.logout();
		if (client.isConnected()) {
			client.disconnect();
		}

	}

	/**
	 * @return
	 * @throws Exception
	 */
	public static FTPClient getFtpClient() throws Exception {
		FTPClient client = new FTPClient();
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
		client.setFileTransferMode(FTP.STREAM_TRANSFER_MODE);
		
		return client;
	}

	/**
	 *
	 * @param client
	 * @param pathList
	 */
	public static void uploadPathList(FTPClient client, List<String> pathList) throws IOException {
		String fixSourcePath = BuilderConstant.SOURCE_PATH;
		log.debug("*********UPLOAD START*********");
		if(client.isConnected()){
			System.out.println("Connected");
		}
		
		for(String pathString : pathList){
			File putFile = new File(fixSourcePath+pathString);

			client.changeWorkingDirectory(BuilderConstant.UPLOAD_PATH);
			String[] dirList = pathString.substring(0, pathString.lastIndexOf("/")).split("/");
			String tmpDir = "";
			for(int i=1; i<dirList.length ; i++){
				tmpDir +=dirList[i] +"/";
				if(client.listFiles(tmpDir).length == 0){
					log.debug("CREATE DIRECTORY : "+tmpDir);
					client.makeDirectory(tmpDir);
				}
			}

			InputStream inputStream = new FileInputStream(putFile);
			boolean result = client.storeFile(BuilderConstant.UPLOAD_PATH+pathString, inputStream);
			log.debug("UPLOAD FILE = "+BuilderConstant.UPLOAD_PATH+pathString +" : "+result);
			inputStream.close();
		}
		log.debug("*********UPLOAD END*********");
	}

	/**
	 * @param client
	 * @param pathList
	 * @throws IOException
	 */
	public static void backupPathList(FTPClient client, List<String> pathList) throws IOException {
		String fixBackupPath = BuilderConstant.BACKUP_PATH+"/"+BuilderUtil.getDateTime();
		log.debug("*********BACKUP START*********");
		for(String pathString : pathList){
			File getFile = new File(fixBackupPath+pathString);
			BuilderUtil.makeDirs(fixBackupPath+pathString);
			OutputStream outputStream = new FileOutputStream(getFile);
			log.debug("BACKUP FILE = "+BuilderConstant.UPLOAD_PATH+pathString);
			boolean result = client.retrieveFile(BuilderConstant.UPLOAD_PATH+pathString, outputStream);
			outputStream.close();
		}
		log.debug("*********BACKUP END*********");
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws BiffException
	 */
	public static List<String> loadExcel() throws BiffException, IOException {
		List<String> pathList = new ArrayList<String>();
		Workbook filePathWorkBook = Workbook.getWorkbook(new File(BuilderConstant.EXCEL_PATH));
		Sheet pathListSheet = filePathWorkBook.getSheet(0);

		for(int row=2; row<pathListSheet.getRows(); row++){
			if("".equals(pathListSheet.getCell(2, row).getContents().trim() )){
				continue;
			}else{
				pathList.add(pathListSheet.getCell(2, row).getContents().trim());
			}
		}

		return pathList;
	}



}
