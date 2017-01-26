package kr.pe.lahuman.client;


public interface Client {

	void connect() throws Exception;
	
	void disconnect();
	
	void backup(String sourcePath, String fixBackupPath) throws Exception;
	
	void upload(String sourcePath,String fixSourcePath) throws Exception;
}
