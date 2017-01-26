package kr.pe.lahuman.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.apache.commons.vfs2.Selectors;
import org.apache.log4j.Logger;

import kr.pe.lahuman.ui.DistributeUI;
import kr.pe.lahuman.utils.BuilderConstant;
import kr.pe.lahuman.utils.BuilderUtil;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

public class SFTP2ClientImpl implements Client {
	Logger log = DistributeUI.log;
	
	ChannelSftp channelSftp = null;
	Session session = null;
	
	@Override
	public void connect() throws Exception {
		//"sftp://"+BuilderConstant.FTP_USERNAME+":"+BuilderConstant.FTP_PASSWORD+"@"+BuilderConstant.FTP_URL+":"+ BuilderConstant.FTP_PORT;
	        Channel channel = null;

	        // 1. JSch 객체를 생성한다.
	        JSch jsch = new JSch();
	        try {
	            // 2. 세션 객체를 생성한다 (사용자 이름, 접속할 호스트, 포트를 인자로 준다.)
	            session = jsch.getSession(BuilderConstant.FTP_USERNAME, BuilderConstant.FTP_URL, BuilderConstant.FTP_PORT);
	            // 3. 패스워드를 설정한다.
	            session.setPassword(BuilderConstant.FTP_PASSWORD);

	            // 4. 세션과 관련된 정보를 설정한다.
	            java.util.Properties config = new java.util.Properties();
	            // 4-1. 호스트 정보를 검사하지 않는다.
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);

	            // 5. 접속한다.
	            session.connect();

	            // 6. sftp 채널을 연다.
	            channel = session.openChannel("sftp");

	            // 7. 채널에 연결한다.
	            channel.connect();
	        } catch (JSchException e) {
	            e.printStackTrace();
	        }

	        // 8. 채널을 FTP용 채널 객체로 캐스팅한다.
	        channelSftp = (ChannelSftp) channel;
	}

	@Override
	public void disconnect() {
		channelSftp.disconnect();
		session.disconnect();
	}

	@Override
	public void backup(String sourcePath, String fixBackupPath) throws SftpException {
		try {
			final String localPath = fixBackupPath+sourcePath;
			BuilderUtil.makeDirs(localPath);
			
			final String serverPath = BuilderConstant.UPLOAD_PATH+sourcePath;
			if(sourcePath.indexOf("*") != -1){
				String realPath = sourcePath.substring(0, sourcePath.length()-1);
				String backupPath = fixBackupPath + realPath ;
				log.info(serverPath.replaceAll("\\*", ""));
				try{
					if(!channelSftp.ls(serverPath).isEmpty())
						backupDirectoy(realPath, backupPath,
								channelSftp.ls(serverPath.replaceAll("\\*", "")));	
				}catch(Exception e){
					log.info("Can not backup directory : "+serverPath);
				}
				
			}else{
				backupFile(localPath, serverPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void upload(String sourcePath, String fixSourcePath) throws Exception {
		
		try {
			String totalSourcepath = fixSourcePath+sourcePath;
			
			
			String[] dirList = sourcePath.substring(0, sourcePath.lastIndexOf("/")).split("/");
			String tmpDir = "";
			for(int f=1; f<dirList.length ; f++){
				tmpDir +=dirList[f] +"/";
				try{
					channelSftp.cd(BuilderConstant.UPLOAD_PATH+"/"+tmpDir);
				}catch(Exception e){
					log.debug("CREATE DIRECTORY : "+tmpDir);
					try {
						channelSftp.mkdir(BuilderConstant.UPLOAD_PATH+"/"+tmpDir);
					} catch (SftpException e1) {
						log.error(e1);
						log.error("CREATE DIRECTORY ERROR: "+tmpDir);
						e1.printStackTrace();
						return ;
					}
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
				final String clientPath = BuilderConstant.UPLOAD_PATH+sourcePath;
				try {
					upload(sourcePath, totalSourcepath, clientPath);
				} catch (SftpException e) {
					log.error("UPLOAD ERROR: "+clientPath);
					e.printStackTrace();
					return;
				}
			}
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void upload(String sourcePath, String totalSourcepath,
			final String clientPath) throws FileSystemException, SftpException {
		log.debug("UPLOAD FILE = "+BuilderConstant.UPLOAD_PATH+sourcePath );
		channelSftp.put(totalSourcepath, clientPath);
	}
	

	private void uploadDirectory(String realPath, String directoryPath,
			String[] rfile) throws IOException,
			FileNotFoundException, SftpException {
		for(int f = 0; f<rfile.length; f++){
			String fileName  = rfile[f];
			final String localPath = directoryPath+"/"+fileName;
			File checkDir = new File(localPath);
			if(checkDir.isDirectory()){
				try{
					channelSftp.cd(BuilderConstant.UPLOAD_PATH+realPath+ "/" +  fileName);
				}catch(Exception e){
					log.debug("CREATE DIRECTORY : "+realPath+ "/" +  fileName);
					try {
						channelSftp.mkdir(BuilderConstant.UPLOAD_PATH+realPath+ "/" +  fileName);
					} catch (SftpException e1) {
						log.error("CREATE DIRECTORY ERROR: "+realPath+ "/" +  fileName);
						e1.printStackTrace();
					}
				}
				String realPath2 = realPath +  fileName+"/";
				String directoryPath2 = localPath ;
				String[] rfile2 = checkDir.list();
				
				uploadDirectory(realPath2,
						directoryPath2, rfile2);
			}else{
				final String clientPath = BuilderConstant.UPLOAD_PATH+realPath +fileName;
				upload(realPath +fileName, localPath, clientPath);
			}
		}
		
		return;
	}
	

	private void backupFile(final String localPath, final String serverPath)
			throws SftpException {
		log.debug("BACKUP FILE = "+serverPath);
		try{
			if(!channelSftp.ls(serverPath).isEmpty())
				channelSftp.get(serverPath, localPath);	
		}catch(Exception e){
			log.info("Can not backup File : "+serverPath);
		}
		
	}


	private void backupDirectoy(
			String realPath, String backupPath,
			Vector<LsEntry> rfile) throws IOException,
			FileNotFoundException, SftpException {
		for(LsEntry ls : rfile){
			if(!"..".equals(ls.getFilename()) && !".".equals(ls.getFilename())){
				if(ls.getAttrs().isDir()){
				
					String realPath2 = realPath + ls.getFilename() + "/";
					String backupPath2 = backupPath + ls.getFilename() + "/";
					BuilderUtil.makeDirs(backupPath2);
					
					backupDirectoy(realPath2, backupPath2,
							channelSftp.ls(BuilderConstant.UPLOAD_PATH+realPath2));
					
				}else{
					String fileName  = ls.getFilename();
					final String serverPath = BuilderConstant.UPLOAD_PATH+realPath +fileName;
					final String localPath = backupPath+fileName;
					backupFile(localPath, serverPath);	
				}
			}
		}
		return;
	}
}

