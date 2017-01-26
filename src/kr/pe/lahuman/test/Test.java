package kr.pe.lahuman.test;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.Selectors;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

public class Test {

  public static final String FTP_URL     = "180.64.114.100";    // ftp.xyz.com
  public static final String FTP_USERNAME = "lahuman";  // ftp_user_01
  public static final String FTP_PASSWORD = "";  // secret_123
  
  public static void main(String[] args) {
    String fileName = "test_"+System.currentTimeMillis()+".txt";
    String filePath = System.getProperty("java.io.tmpdir")+fileName;
    
    System.out.println("File Path: "+filePath);
    
    StandardFileSystemManager manager = new StandardFileSystemManager();
    try {
      // if uploaded file not exist, create a new one
      BufferedWriter writerAnsi = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "Cp1254"));
      writerAnsi.write("Sample text");      
      writerAnsi.flush();
      writerAnsi.close();
    
      // e.g: 
      // sftp://your-ftp-username":your-ftp-password@your-ftp-url/afolder/
      String sftpUri = "sftp://"+FTP_USERNAME+":"+FTP_PASSWORD+"@"+FTP_URL;
          
      FileSystemOptions opts = new FileSystemOptions();
      SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
      
      manager.init();
      
      FileObject fileObject = manager.resolveFile(sftpUri+"/"+fileName, opts);
      
      FileObject localFileObject = manager.resolveFile(filePath);
      
      fileObject.copyFrom(localFileObject, Selectors.SELECT_SELF);
      
    } catch (Exception e) {
      e.printStackTrace();
    }finally {
      manager.close();
    }
  }
}
