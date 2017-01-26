package kr.pe.lahuman.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class SFTP2Test {

	public static void main(String[] args) throws SocketException, IOException {
		new SFTP2Test().upload("/jsch", new File("C:\\eula.1028.txt"));
	}
	
	ChannelSftp channelSftp = null;
	Session session = null;
	  private void connect() {
		  Channel channel = null;

	        // 1. JSch 객체를 생성한다.
	        JSch jsch = new JSch();
	        try {
	            // 2. 세션 객체를 생성한다 (사용자 이름, 접속할 호스트, 포트를 인자로 준다.)
	            session = jsch.getSession("lahuman", "lahuman.iptime.org", 22);
	            // 3. 패스워드를 설정한다.
	            session.setPassword("wjdtndus1");

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
	  public void upload(String catalinaHome, File file) throws SocketException, IOException {
	        // 앞서 만든 접속 메서드를 사용해 접속한다.
	        connect();
	        if(channelSftp == null){
	        	System.out.println("!!!!");
	        	return;
	        }
	        FileInputStream in = null;
	        try {
	            // 입력 파일을 가져온다.
	            in = new FileInputStream(file);

	            System.out.println(channelSftp.ls(catalinaHome ));;
	            
	            // 업로드하려는 위치르 디렉토리를 변경한다.
	            try{
	            	channelSftp.cd(catalinaHome + "/webapps");
	            }catch(Exception e){
	            	//디렉토리가 없을 경우
	            	channelSftp.mkdir(catalinaHome + "/webapps");
	            }
	            
//	            channelSftp.cd(catalinaHome + "/webapps");
	            // 파일을 업로드한다.
	            //channelSftp.put("C:\\tmp\\PaLog.log", catalinaHome + "/webapps/tmp/PaLog.log" );
//	            channelSftp.get(catalinaHome + "/webapps", "C:\\tmp");
	            Vector<LsEntry> v = channelSftp.ls("/jsch/test");
	            
	            
	            for(LsEntry ls : v){
	            	if(!"..".equals(ls.getFilename()) && !".".equals(ls.getFilename())){
	            		System.out.println(ls.getAttrs().isDir());
	            		System.out.println(ls.getFilename());
	            	}
	            }
	            
	        } catch (SftpException e) {
	            e.printStackTrace();
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } finally {
	            try {
	                // 업로드된 파일을 닫는다.
	                in.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        }
	        
	        channelSftp.disconnect();    
	        session.disconnect();
	    }
}
