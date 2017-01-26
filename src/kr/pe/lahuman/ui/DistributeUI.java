package kr.pe.lahuman.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

import kr.pe.lahuman.client.Client;
import kr.pe.lahuman.client.FTPClientImpl;
import kr.pe.lahuman.client.SFTP2ClientImpl;
import kr.pe.lahuman.core.BuilderMain;
import kr.pe.lahuman.core.ConSave;
import kr.pe.lahuman.ui.groups.UIGroups;
import kr.pe.lahuman.ui.runner.MessageDialog;
import kr.pe.lahuman.utils.BuilderConstant;
import kr.pe.lahuman.utils.BuilderUtil;
import kr.pe.lahuman.xml.XMLParser;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
//import org.eclipse.jface.dialogs.MessageDialog;
//import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

public class DistributeUI {

	public static Logger log = Logger.getLogger(DistributeUI.class);

	static {
		String layout = "%d %-5p [%t] %-17c{2} (%13F:%L) %3x - %m%n";
		String logfilename = "DailyLog.log";
		String datePattern = ".yyyy-MM-dd ";

		PatternLayout patternlayout = new PatternLayout(layout);
		DailyRollingFileAppender appender = null;
		ConsoleAppender consoleAppender = null;
		try {
			appender = new DailyRollingFileAppender(patternlayout, logfilename,
					datePattern);
			consoleAppender = new ConsoleAppender(patternlayout);

		} catch (IOException e) {
			e.printStackTrace();
		}
		log.addAppender(appender);
		log.addAppender(consoleAppender);
		log.setLevel(Level.DEBUG);
	}

	Display display = new Display();
	Shell shell = new Shell(display, SWT.CLOSE);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

//		Image i = ImageDescriptor.createFromURL(
//				ImagePath.class.getResource("distribution.png")).createImage();
//		shell.setImage(i);
		
		new DistributeUI();
	}

	private final String btnText = "DIRECTORY";
	private DistributeUI() {

		initShell();
		UIGroups ui = new UIGroups();

		//UI PART
		Composite wholeComposite = ui.wholeAreaUI(shell);
		final Group commonGroup = ui.makeCommonInfoUI(wholeComposite);
		makeChoiseFile(commonGroup, "SOURCE PATH :", btnText);
		makeChoiseFile(commonGroup, "EXCEL PATH :", "FILE");
		
		
		final Group backupGroup = ui.makeGroupUI(wholeComposite,
				"BACKUP INFORMATION", 3);

		final Button isBackupBtn = new Button(backupGroup, SWT.CHECK);
		isBackupBtn.setText("BACKUP");
		GridData btnData = new GridData(GridData.FILL_HORIZONTAL);
		btnData.horizontalSpan = 3;
		isBackupBtn.setLayoutData(btnData);
		
		makeChoiseFile(backupGroup, "BACKUP PATH :", btnText);
		
		((Button)backupGroup.getChildren()[3]).setEnabled(false);
		
		isBackupBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (isBackupBtn.getSelection()) {
                	((Button)backupGroup.getChildren()[3]).setEnabled(true);            
                } else {
                	((Button)backupGroup.getChildren()[3]).setEnabled(false);
                	((Text)backupGroup.getChildren()[2]).setText("");
                }
            }
        });
		
		final Group ftpGroup = ui.makeGroupUI(wholeComposite, "FTP INFORMATION", 2);
		{
			makeFtpInfo(ftpGroup, "URL :");
			makeFtpInfo(ftpGroup, "PORT :", "21");
			makeFtpInfo(ftpGroup, "UPLOAD PATH : ");
			makeFtpInfo(ftpGroup, "USER NAME :");
			makeFtpInfo(ftpGroup, "PASSWORD :");
	
			Label label = new Label(ftpGroup, SWT.NULL);
			label.setText("ENCODE TYPE : ");
			label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Combo encodeTypeCombo = new Combo(ftpGroup, SWT.DROP_DOWN
					| SWT.READ_ONLY);
			encodeTypeCombo.add("UTF-8");
			encodeTypeCombo.add("EUC-KR");
			encodeTypeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			encodeTypeCombo.select(0);
			
			Label ftpInfoLabel = new Label(ftpGroup, SWT.NULL);
			ftpInfoLabel.setText("FTP TYPE : ");
			ftpInfoLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Combo ftpInfoCombo = new Combo(ftpGroup, SWT.DROP_DOWN
					| SWT.READ_ONLY);
			ftpInfoCombo .add("FTP");
			ftpInfoCombo .add("SFTP");
			ftpInfoCombo .setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			ftpInfoCombo .select(0);
		}
		
		
		final Group shellGroup = ui.makeGroupUI(wholeComposite, "SHELL INFORMATION", 3);
		{
			makeChoiseFile(shellGroup, "BEFORE SHELL PATH :", "XML");
			makeChoiseFile(shellGroup, "AFTER SHELL PATH :", "XML");
		}
		
		// BTN group
		Group btnGroup = makeBtnAreaUI(wholeComposite);
		
		//make btn
		Button loadBtn = makeLoadBtn(commonGroup, backupGroup, ftpGroup,shellGroup, 
				btnGroup);
		makeSaveBtn(commonGroup, backupGroup, ftpGroup, shellGroup, btnGroup, loadBtn);
		makeDistributeBtn(commonGroup, backupGroup, ftpGroup,shellGroup, btnGroup);
		
		//shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();	
	}

	private void initShell() {
		//에러나..... 실행하면...
//		shell.setImage(new Image(display, ImagePath.class.getResource("distribution.ico").getPath()));
		shell.setText("J-DISTRIBUTE by lahuman");
		shell.setLayout(new FillLayout());
		shell.setSize(400, 550);
	}

	

	private Group makeBtnAreaUI(Composite wholeComposite) {
		Group btnGroup = new Group(wholeComposite, SWT.NONE);
		org.eclipse.swt.layout.FormLayout layout = new org.eclipse.swt.layout.FormLayout();
		layout.marginLeft = layout.marginRight = 5;
		btnGroup.setLayout(layout);
		btnGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return btnGroup;
	}

	private void makeDistributeBtn(final Group commonGroup,
			final Group backupGroup, final Group ftpGroup,final Group shellGroup, Group btnGroup) {
		Button distributBtn = new Button(btnGroup, SWT.PUSH);
		FormData distributData = new FormData();
		distributData.right = new FormAttachment(100);
		distributBtn.setLayoutData(distributData);
		distributBtn.setText("DISTIRIBUTE");

		distributBtn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final Shell dialog = new Shell(shell, SWT.DIALOG_TRIM
						| SWT.APPLICATION_MODAL);
				dialog.setLayout(new FillLayout());
				dialog.setSize(400, 120);
				dialog.setText("Result ");

				Composite wholeComposite = new Composite(dialog, SWT.NONE);
				GridLayout layoutWhole = new GridLayout();
				layoutWhole.numColumns = 1;
				wholeComposite.setLayout(layoutWhole);
				Label infoLab = new Label(wholeComposite, SWT.NONE);
				infoLab.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
				infoLab.setText("Distributiong........");
				final ProgressBar bar = new ProgressBar(wholeComposite,
						SWT.SMOOTH);
				bar.setBounds(10, 10, 200, 20);
				bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

				final Button clsBtn = new Button(wholeComposite, SWT.PUSH);
				clsBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
				clsBtn.setText("OK");
				clsBtn.setEnabled(false);
				clsBtn.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetSelected(SelectionEvent arg0) {
						dialog.close();
					}

					@Override
					public void widgetDefaultSelected(SelectionEvent arg0) {

					}
				});
				//validate input data!!!
				
		
				if(checkStr(((Text) commonGroup.getChildren()[1]).getText(), "SOURCE PATH")){
					return;
				}
				if(checkStr(((Text) commonGroup.getChildren()[4]).getText(), "EXCEL PATH")){
					return;
				}
			
				if(((Button) backupGroup.getChildren()[0]).getSelection()){
					BuilderConstant.IS_BACKUP = "Y";
					if(checkStr(((Text) backupGroup.getChildren()[2]).getText(), "BACKUP PATH")){
						return;
					};
					BuilderConstant.BACKUP_PATH = ((Text) backupGroup.getChildren()[2]).getText();
				}else{
					BuilderConstant.IS_BACKUP = "N";
					BuilderConstant.BACKUP_PATH = "";
				}
				
					
				if(checkStr(((Text) ftpGroup.getChildren()[1]).getText(), "FTP URL")){
					return;
				}
				if(checkStr(((Text) ftpGroup.getChildren()[3]).getText(), "FTP PORT")){
					return;
				}
				if(checkStr(((Text) ftpGroup.getChildren()[5]).getText(), "UPLOAD PATH")){
					return;
				}
				if(checkStr(((Text) ftpGroup.getChildren()[7]).getText(), "USER NAME")){
					return;
				}
				if(checkStr(((Text) ftpGroup.getChildren()[9]).getText(), "PASSWORD")){
					return;
				}
				
				//setValue
				
				BuilderConstant.SOURCE_PATH = ((Text) commonGroup.getChildren()[1]).getText();
				BuilderConstant.EXCEL_PATH = ((Text) commonGroup.getChildren()[4]).getText();
				BuilderConstant.FTP_URL = ((Text) ftpGroup.getChildren()[1]).getText();
				try{
					BuilderConstant.FTP_PORT=Integer.parseInt( ((Text) ftpGroup.getChildren()[3]).getText());
				}catch(Exception e){
					showMessage("FTP PORT JUST USED NUMBER");
					return;
				}
				BuilderConstant.UPLOAD_PATH = ((Text) ftpGroup.getChildren()[5]).getText();
				BuilderConstant.FTP_USERNAME = ((Text) ftpGroup.getChildren()[7]).getText();
				BuilderConstant.FTP_PASSWORD = ((Text) ftpGroup.getChildren()[9]).getText();
				
				BuilderConstant.ENCODE_TYPE = ((Combo) ftpGroup.getChildren()[11]).getText();
				
				BuilderConstant.FTP_TYPE = ((Combo) ftpGroup.getChildren()[13]).getText();
				
				BuilderConstant.BEFORE_SHELL_PATH = ((Text) shellGroup.getChildren()[1]).getText();
				BuilderConstant.AFTER_SHELL_PATH = ((Text) shellGroup.getChildren()[4]).getText();
				
				try {
					List<String> pathList = BuilderMain.loadExcel();
					int maxBarSize = pathList.size();
					
					if ("Y".equals(BuilderConstant.IS_BACKUP)) {
						maxBarSize = maxBarSize*2;
					}
					
					if(!"".equals(BuilderConstant.BEFORE_SHELL_PATH))
						maxBarSize++;
					
					if(!"".equals(BuilderConstant.AFTER_SHELL_PATH))
						maxBarSize++;
					
					bar.setMaximum(maxBarSize);
					BuilderConstant.MAX_BAR_SIZE =maxBarSize; 
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				try{
					new Thread() {
						public void run() {

							Client client = null;
//							SSHClient client = null;
							
							//Before Shell run
							try {
								runShell(bar, clsBtn, BuilderConstant.BEFORE_SHELL_PATH, 1);
							} catch (Exception e2) {
								log.debug(e2.toString());
								showMessage(e2.getMessage());
								closeDialog(dialog);
								 return;
							}
							
							
							try {
//								client = BuilderMain.getFtpClient();
								if(BuilderConstant.FTP_TYPE.equals("FTP")){
									client = new FTPClientImpl();
								}else{
									client = new SFTP2ClientImpl();
								}
								client.connect();
							} catch (Exception e1) {
							 	log.debug(e1.toString());
								showMessage(e1.getMessage());
								closeDialog(dialog);
								 return;
							}
	//
							int[] totalcnt = new int[1];
							int listCnt =0;
							try {
								
								List<String> pathList = BuilderMain.loadExcel();
								listCnt = pathList.size();
								//bar.setMaximum(listCnt);

								if ("Y".equals(BuilderConstant.IS_BACKUP)) {
									//bar.setMaximum(listCnt*2);
									backup(dialog, bar, clsBtn, client,
											listCnt, pathList);
								}
								
								// FTP upload
								//BuilderMain.uploadPathList(client, pathList);
								upload(dialog, bar, clsBtn, client, totalcnt,
										listCnt, pathList);
								// testCase(client);
							} catch (Exception e) {
								log.error(e);
								e.printStackTrace();
								showMessage(e.toString());
								closeDialog(dialog);
							} finally {
								client.disconnect();
							}
							
							//After Shell run
							try {
								runShell(bar, clsBtn, BuilderConstant.AFTER_SHELL_PATH, BuilderConstant.MAX_BAR_SIZE);
							} catch (Exception e) {
								log.debug(e.toString());
								showMessage(e.getMessage());
								closeDialog(dialog);
								 return;
							}
							
						}

						private void runShell(final ProgressBar bar,
								final Button clsBtn, String shellPath,
								final int beforeBarLagnth) throws Exception {
							if("".equals(shellPath)){
								return;
							}
							try{
								final XMLParser xp = new XMLParser(shellPath);
								final NodeList result = xp.getNodeList(BuilderConstant.SERVER);
								
								log.info("inputpath : " + shellPath);
										try{
											for(int i[]= new int[1]; i[0]<result.getLength(); i[0]++){
												Node node = result.item(i[0]);
												log.info("---------------------------------------");
												log.info("SERVER INFO  : NAME=>["+xp.getString(node, BuilderConstant.NAME)+"] IP=>[" + xp.getString(node, BuilderConstant.IP)+"]");
//												log.info("ID/PW  : [" + xp.getString(node, Constants.ID)+"/"+ xp.getString(node, Constants.PW)+"]");
												
												ConSave cs = new ConSave(xp.getString(node, BuilderConstant.NAME), xp.getString(node, BuilderConstant.IP), Integer.parseInt(xp.getString(node, BuilderConstant.PORT)), xp.getString(node, BuilderConstant.ID), xp.getString(node, BuilderConstant.PW));
												//종료를 위해 exit 명령어 삽입
												List<String> command = xp.getList(node, BuilderConstant.COMMAND);
												command.add("exit");
												log.info("=> SAVE COMMAND START ");
												log.info("=> " + command);
												cs.saveCommand(command);
												log.info("=> SAVE COMMAND END ");
												cs.disconnect();
												log.info("---------------------------------------");
											}
										}catch(Exception e){
											log.debug(e.toString());
											showMessage(e.getMessage());
											throw new RuntimeException(e.getMessage());
										}
										
										int i[] = new int[1];
										i[0] = beforeBarLagnth;
										barUpdate(bar, clsBtn, i);
								
							}catch(Exception e){
								log.debug(e.toString());
								if(e instanceof SAXParseException){
									showMessage("XML 형식을 다시 확인 하세요.");
								}else{
									showMessage(e.getMessage());
								}
								throw new Exception(e.getMessage());
							}
						}

						private void closeDialog(final Shell dialog) {
							display.syncExec(new Runnable() {
								@Override
								public void run() {
									if(dialog.isVisible())
										dialog.close();	
								}
							});
						}

						private void upload(final Shell dialog,
								final ProgressBar bar, final Button clsBtn,
								Client client, int[] totalcnt, int listCnt,
								List<String> pathList) throws Exception {
							String fixSourcePath = BuilderConstant.SOURCE_PATH;
							log.debug("*********UPLOAD START*********");
							
							for (final int[] i = new int[1]; i[0] < listCnt; i[0]++) {
								try{
									String pathString = pathList.get(i[0]);
									
									client.upload(pathString, fixSourcePath);
									
								}catch(Exception e){
									throw e;
								}
								
								if (display.isDisposed()){
									dialog.close();									
									return;
								}
								if ("Y".equals(BuilderConstant.IS_BACKUP)) {
									totalcnt[0] = listCnt + i[0];
									barUpdate(bar, clsBtn, totalcnt);
								}else{
									barUpdate(bar, clsBtn, i);
								}
							}
							log.debug("*********UPLOAD END*********");
						}

						private void backup(final Shell dialog,
								final ProgressBar bar, final Button clsBtn,
								Client client, int listCnt,
								List<String> pathList) throws Exception {
							String fixBackupPath = BuilderConstant.BACKUP_PATH+"/"+BuilderUtil.getDateTime();
							log.debug("*********BACKUP START*********");
							for (final int[] i = new int[1]; i[0] < listCnt; i[0]++) {
								try {
									String pathString = pathList.get(i[0]);
									client.backup(pathString, fixBackupPath);
								} catch (Exception th) {
									throw th;
								}
								if (display.isDisposed()){
									dialog.close();										
									return;
								}
								barUpdate(bar, clsBtn, i);
							}
							log.debug("*********BACKUP END*********");
						}


						private void barUpdate(final ProgressBar bar,
								final Button clsBtn, final int[] i) {
							display.syncExec(new Runnable() {
								public void run() {
									
									if (bar.isDisposed())
										return;
									// bar update
									bar.setSelection(i[0]+1);
									if (bar.getMaximum() <=( i[0]+1)) {
										clsBtn.setEnabled(true);
									}
								}
							});
						}
						
						
						private void barUpdate(final ProgressBar bar) {
							display.syncExec(new Runnable() {
								public void run() {
									
									if (bar.isDisposed())
										return;
									// bar update
									
									bar.setSelection(bar.getSelection()+1);
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									bar.setSelection(bar.getSelection()-1);
									
								}
							});
						}
						
					}.start();
				}catch(Exception e){
					display.syncExec(new Runnable() {
						@Override
						public void run() {
							if(dialog.isVisible())
								dialog.close();	
						}
					});
				}
				dialog.open();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {

			}
		});
	}

	private void makeSaveBtn(final Group commonGroup, final Group backupGroup,
			final Group ftpGroup, final Group shellGroup, Group btnGroup, Button loadBtn) {
		Button saveBtn = new Button(btnGroup, SWT.PUSH);
		FormData saveData = new FormData();
		saveData.left = new FormAttachment(loadBtn, 5);
		saveBtn.setLayoutData(saveData);
		saveBtn.setText("SAVE CONFIG");
		saveBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String[] filterNames = new String[] { "Properties File" };
				String[] filterExtensions = new String[] { "*.properties" };
				FileDialog dialog = makeFileDialog(filterNames, filterExtensions, SWT.SAVE);
				try {
					String propertiesFile  = dialog.open();
					
					Properties prop = new Properties();
					
					prop.setProperty("common.fix.source.path", ((Text) commonGroup.getChildren()[1]).getText());
					prop.setProperty("excel.path", ((Text) commonGroup.getChildren()[4]).getText());

					prop.setProperty("ftp.url", ((Text) ftpGroup.getChildren()[1]).getText());
					prop.setProperty("ftp.port", ((Text) ftpGroup.getChildren()[3]).getText());
					
					prop.setProperty("ftp.upload.path", ((Text) ftpGroup.getChildren()[5]).getText());
					prop.setProperty("ftp.username", ((Text) ftpGroup.getChildren()[7]).getText());
					prop.setProperty("ftp.password", ((Text) ftpGroup.getChildren()[9]).getText());
					
					
					prop.setProperty("common.encode", ((Combo) ftpGroup.getChildren()[11]).getText());
					prop.setProperty("ftp.type", ((Combo) ftpGroup.getChildren()[13]).getText());
					
					prop.setProperty("before.shell.path", ((Text) shellGroup.getChildren()[1]).getText());
					prop.setProperty("after.shell.path", ((Text) shellGroup.getChildren()[4]).getText());
					
					if(((Button) backupGroup.getChildren()[0]).getSelection()){
						prop.setProperty("common.backup", "Y");
						prop.setProperty("common.fix.backup.path", ((Text) backupGroup.getChildren()[2]).getText())	;
					}else{
						prop.setProperty("common.backup", "N");
						prop.setProperty("common.fix.backup.path", "")	;
					}
					
		          OutputStream stream = new FileOutputStream(propertiesFile);
		          prop.store(stream, "Distribute");
		          stream.close();
		     
				}catch(Exception e){
					log.debug(e.toString());
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}
		});
	}

	private Button makeLoadBtn(final Group commonGroup,
			final Group backupGroup, final Group ftpGroup, final Group shellGroup, Group btnGroup) {
		Button loadBtn = new Button(btnGroup, SWT.PUSH);
		FormData loadData = new FormData();
		loadData.left = new FormAttachment(0, 0);

		loadBtn.setLayoutData(loadData);
		loadBtn.setText("LOAD CONFIG");
		
		loadBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				//load file
				String[] filterNames = new String[] { "Properties File" };
				String[] filterExtensions = new String[] { "*.properties" };
				FileDialog dialog = makeFileDialog(filterNames, filterExtensions);
				try {
					String propertiesFile  = dialog.open();
					Properties properties = new Properties();
					FileInputStream file = null;

					try {
						file = new FileInputStream(propertiesFile);
						properties.load(file);
					} catch (IOException e) {
						log.debug(e.toString());
					}
					
					((Text) commonGroup.getChildren()[1]).setText(properties.getProperty("common.fix.source.path"));
					((Text) commonGroup.getChildren()[4]).setText(properties.getProperty("excel.path"));

					((Text) ftpGroup.getChildren()[1]).setText(properties.getProperty("ftp.url"));
					((Text) ftpGroup.getChildren()[3]).setText(properties.getProperty("ftp.port"));
					
					((Text) ftpGroup.getChildren()[5]).setText(properties.getProperty("ftp.upload.path"));
					((Text) ftpGroup.getChildren()[7]).setText(properties.getProperty("ftp.username"));
					((Text) ftpGroup.getChildren()[9]).setText(properties.getProperty("ftp.password"));
					
					((Combo) ftpGroup.getChildren()[11]).setText(properties.getProperty("common.encode"));
					((Combo) ftpGroup.getChildren()[13]).setText(properties.getProperty("ftp.type"));
					
					((Text) shellGroup.getChildren()[1]).setText(properties.getProperty("before.shell.path"));
					((Text) shellGroup.getChildren()[4]).setText(properties.getProperty("after.shell.path"));
					
					if("Y".equals(properties.getProperty("common.backup"))){
						((Button)backupGroup.getChildren()[3]).setEnabled(true);        
						((Button) backupGroup.getChildren()[0]).setSelection(true);
						((Text) backupGroup.getChildren()[2]).setText(properties.getProperty("common.fix.backup.path"));
					}else{
						((Button)backupGroup.getChildren()[3]).setEnabled(false);        
						((Button) backupGroup.getChildren()[0]).setSelection(false);
						((Text) backupGroup.getChildren()[2]).setText("");
					}
					
					file.close();
				} catch (Exception e) {
					log.debug(e.toString());
				}
			}

			
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
		return loadBtn;
	}

	
	
	private void showMessage(final String message) {
		display.syncExec(new MessageDialog(shell, message));
	}
	private boolean checkStr(String str, String pattan){
		if("".equals(str.trim() ))
			showMessage("Check your "+pattan);
		
		return "".equals(str.trim() );
	}
	private void makeFtpInfo(Group ftpGroup, String string) {
		makeFtpInfo(ftpGroup, string, "");
	}
	private void makeFtpInfo(Group ftpGroup, String string, String setVal) {
		Label label = new Label(ftpGroup, SWT.NULL);
		label.setText(string);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Text text = new Text(ftpGroup, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setText(setVal);
	}

	private FileDialog makeFileDialog(String[] filterNames, String[] filterExtensions) {
		return makeFileDialog(filterNames, filterExtensions, SWT.OPEN);
	}
	private FileDialog makeFileDialog(String[] filterNames, String[] filterExtensions, int type) {
		FileDialog dialog = new FileDialog(
				shell, type);
	
		String filterPath = "/";
		String platform = SWT.getPlatform();
		if (platform.equals("win32") || platform.equals("wpf")) {
			filterPath = "c:\\";
		}
		dialog.setFilterNames(filterNames);
		dialog.setFilterExtensions(filterExtensions);
		dialog.setFilterPath(filterPath);
		dialog.setFileName("");
		return dialog;
	}
	
	private void makeChoiseFile(Group commonGroup, String lableText,
			final String btnText) {
		Label label = new Label(commonGroup, SWT.NULL);
		label.setText(lableText);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Text text = new Text(commonGroup, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		text.setEnabled(false);
		Button btn = new Button(commonGroup, SWT.PUSH);
		btn.setText(btnText);
		btn.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btn.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if ("DIRECTORY".equals(btnText)) {
					DirectoryDialog directoryDialog = new DirectoryDialog(shell);

					directoryDialog.setFilterPath("");
					directoryDialog
							.setMessage("Please select a directory and click OK");

					String dir = directoryDialog.open();
					if (dir != null) {
						text.setText(dir);
					}
				}else if("XML".equals(btnText)){
					
					String[] filterNames = new String[] { "XML File" };
					String[] filterExtensions = new String[] { "*.xml" };
					
					FileDialog dialog = makeFileDialog(filterNames, filterExtensions);
					try {
						text.setText(dialog.open());
					} catch (Exception e) {
						text.setText("");
						log.debug(e.toString());
					}
				} else {
					String[] filterNames = new String[] { "Excel File" };
					String[] filterExtensions = new String[] { "*.xls" };
					
					FileDialog dialog = makeFileDialog(filterNames, filterExtensions);
					try {
						text.setText(dialog.open());
					} catch (Exception e) {
						log.debug(e.toString());
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				text.setText("");
			}
		});
	}
}
