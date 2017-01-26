package kr.pe.lahuman.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import kr.pe.lahuman.utils.BuilderConstant;
import kr.pe.lahuman.utils.BuilderUtil;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import com.jcraft.jsch.JSchException;

@Deprecated
public class SFTPClientImpl implements Client {

	static Logger log = Logger.getLogger(SFTPClientImpl.class);
	private StandardFileSystemManager manager = null;
	private String sftpUri = "";
	private FileSystemOptions opts = new FileSystemOptions();
	
    
	@Override
	public void connect() throws Exception {
		manager = new StandardFileSystemManager();
		sftpUri = "sftp://"+BuilderConstant.FTP_USERNAME+":"+BuilderConstant.FTP_PASSWORD+"@"+BuilderConstant.FTP_URL+":"+ BuilderConstant.FTP_PORT;
	    SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
	    manager.init();
	    try{
	    	
	    	manager.resolveFile(sftpUri );
	    }catch(IOException e){
	    	throw new Exception(e.getMessage());
	    }
	}

	@Override
	public void disconnect() {
		manager.close();
	}

	@Override
	public void backup(String sourcePath, String fixBackupPath) {
		try {
			final String localPath = fixBackupPath+sourcePath;
			BuilderUtil.makeDirs(localPath);
			
			final String serverPath = BuilderConstant.UPLOAD_PATH+sourcePath;
			if(sourcePath.indexOf("*") != -1){
				String realPath = sourcePath.substring(0, sourcePath.length()-1);
				String backupPath = fixBackupPath + realPath ;
//				FTPFile[] rfile = client.listFiles(BuilderConstant.UPLOAD_PATH+realPath);
				FileObject[] fileObjects = manager.resolveFile(sftpUri + "/" + serverPath, opts).getChildren();
				
				backupDirectoy(realPath, backupPath,
						fileObjects);
			}else{
			      backupFile(localPath, serverPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void backupFile(final String localPath, final String serverPath)
			throws FileSystemException {
		FileObject fileObject = manager.resolveFile(sftpUri + "/" + serverPath, opts);
		FileObject localFileObject = manager.resolveFile(localPath);
		localFileObject.copyFrom(fileObject, Selectors.SELECT_SELF);
		log.debug("BACKUP FILE = "+serverPath);
	}


	private void backupDirectoy(
			String realPath, String backupPath,
			FileObject[] rfile) throws IOException,
			FileNotFoundException {
		for(int f = 0; f<rfile.length; f++){
			if(rfile[f].getType() == FileType.FOLDER){
				
				String realPath2 = realPath + rfile[f].getName() + "/";
				String backupPath2 = backupPath + rfile[f].getName() + "/";
				BuilderUtil.makeDirs(backupPath2);
				
				FileObject[] fileObjects = manager.resolveFile(sftpUri+"/"+BuilderConstant.UPLOAD_PATH+realPath2, opts).getChildren();
				backupDirectoy(realPath2, backupPath2,
						fileObjects);
			}else{
				String fileName  = rfile[f].getName().getBaseName();
				final String serverPath = BuilderConstant.UPLOAD_PATH+realPath +fileName;
				final String localPath = backupPath+fileName;
				 backupFile(localPath, serverPath);
			}
		}
		return;
	}
	

	@Override
	public void upload(String sourcePath, String fixSourcePath) {
		try {
			String totalSourcepath = fixSourcePath+sourcePath;
			
			
			String[] dirList = sourcePath.substring(0, sourcePath.lastIndexOf("/")).split("/");
			String tmpDir = "";
			for(int f=1; f<dirList.length ; f++){
				tmpDir +=dirList[f] +"/";
				FileObject fileObject = manager.resolveFile(sftpUri+"/"+BuilderConstant.UPLOAD_PATH+"/"+tmpDir, opts);
				if(!fileObject.exists() || fileObject.getType() == FileType.FILE){
					log.debug("CREATE DIRECTORY : "+tmpDir);
					fileObject.createFolder();
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
				final String clientPath = sftpUri + "/" + BuilderConstant.UPLOAD_PATH+sourcePath;
				upload(sourcePath, totalSourcepath, clientPath);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void upload(String sourcePath, String totalSourcepath,
			final String clientPath) throws FileSystemException {
		FileObject fileObject = manager.resolveFile(clientPath, opts);
		FileObject localFileObject = manager.resolveFile(totalSourcepath);
		fileObject.copyFrom(localFileObject, Selectors.SELECT_SELF);
		log.debug("UPLOAD FILE = "+BuilderConstant.UPLOAD_PATH+sourcePath );
	}
	
	private void uploadDirectory(String realPath, String directoryPath,
			String[] rfile) throws IOException,
			FileNotFoundException {
		for(int f = 0; f<rfile.length; f++){
			String fileName  = rfile[f];
			final String localPath = directoryPath+"/"+fileName;
			File checkDir = new File(localPath);
			if(checkDir.isDirectory()){
				FileObject fileObject = manager.resolveFile(sftpUri+"/"+BuilderConstant.UPLOAD_PATH+realPath+ "/" +  fileName, opts);
				if(!fileObject.exists() || fileObject.getType() == FileType.FILE){
					log.debug("CREATE DIRECTORY : "+realPath+ "/" +  fileName);
					fileObject.createFolder();
				}
				
				String realPath2 = realPath +  fileName+"/";
				String directoryPath2 = localPath ;
				String[] rfile2 = checkDir.list();
				
				uploadDirectory(realPath2,
						directoryPath2, rfile2);
				
			}else{
				final String clientPath = sftpUri + "/" + BuilderConstant.UPLOAD_PATH+realPath +fileName;
				upload(realPath +fileName, localPath, clientPath);
			}
		}
		
		return;
	}

}
