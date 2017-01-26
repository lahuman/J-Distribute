package kr.pe.lahuman.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import kr.pe.lahuman.ui.DistributeUI;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ConSave {
	
	
	Logger log = DistributeUI.log;
	
	private Session session;
	private Channel channel; 
	private String name = ""; 
	public ConSave(String name, String ip, int port, String id, String pw) throws JSchException, IOException{
		this.name = name;
		 JSch jsch = new JSch();         
		 session = jsch.getSession(id, ip, port);       
		 
         session.setPassword(pw);         
         session.setConfig("StrictHostKeyChecking", "no");
//         session.setTimeout( 3000*10 );
         // TCP/IP check setting
         //session.setServerAliveInterval( 2000 );
         session.connect();
	}
	
	public void saveCommand(List<String> commands) throws JSchException, IOException{
		channel = session.openChannel("shell");
		//영문 기준으로 환경 변경
		((ChannelShell)channel).setEnv("LANG", "en_US.utf8");
		//ANSI escape sequences 제거 해주는 명령
		((ChannelShell)channel).setPtyType("dumb");
		InputStream in = channel.getInputStream();
		OutputStream ops = channel.getOutputStream();
		PrintStream ps = new PrintStream(ops, true, "UTF-8");
//		channel.setOutputStream(new PrintStream(new ByteArrayOutputStream(), true, "UTF-8"));
		channel.connect(3*1000);
		
	
        //시작 LOG
        log.debug("===============================================");
        log.debug("| SERVER INFO : NAME=>["+name+"] IP=>[" + session.getHost()+"]" +"        |");
        log.debug("===============================================");
        
    	
        try{
        	
//        	InputStream in = channel.getInputStream();
        	byte[] bt = new byte[1024];
        
        
        	int commandNum = 0;
        	while (true) {
        		//다음 명령어 실행
        		callCommand(commands, ps, commandNum++);
        		
        		//명령어 처리가 종료 되었는지 확인 13 이하의 경우 처리 중으로 판단 한다.
        		while(in.available() < 13 ) {
        			try{Thread.sleep(2000);}catch(Exception ee){}
        		}
        		
        		
        		while (in.available() > 0) {
        			int i = in.read(bt, 0, 1024);
        			if (i < 0)
        				break;
        			String str = new String(bt, 0, i);
        			// displays the output of the command executed.
        			log.debug(str);
        		}
        		
        		
        		if (channel.isClosed()) {
        			
        			break;
        		}
        		try{Thread.sleep(100);}catch(Exception ee){}
        	}
        }catch(Exception e){
        	System.out.println(e);
        }finally{
        	if(ps != null)
        		ps.close();
            //종료 LOG
        		log.debug("===============================================");
        		log.debug("");
        		log.debug("");
        		log.debug("");
        }
	}

	private void callCommand(List<String> commands, PrintStream ps,
			int commandNum) {
		if(commands.size() > commandNum){
			ps.println(commands.get(commandNum));
			//ssh 명령어를 이용 하기 위해서는, 1초간의 텀을 두어야 제대로 동작 한다.
			try{Thread.sleep(1000);}catch(Exception ee){}
		}
	}
	public void disconnect(){
		if(channel != null)
			channel.disconnect();
		if(session != null)
			session.disconnect();
	}

}
