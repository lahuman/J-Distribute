package kr.pe.lahuman.ui.runner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class MessageDialog implements Runnable {

	Shell shell = null;
	String message = "";
	public MessageDialog(Shell shell, String message) {
		this.shell = shell;
		this.message = message;
	}

	@Override
	public void run() {
		final Shell dialogMsg =
		          new Shell(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		        dialogMsg.setLayout(new GridLayout());
		        dialogMsg.setText("ERROR MASSAGE!");
		        Label msg = new Label(dialogMsg, SWT.NULL);
				msg.setText(message);
		        msg.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
		        
		        Button okButton = new Button(dialogMsg, SWT.PUSH);
		        okButton.setText("OK");
		        okButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		        okButton.addSelectionListener(new SelectionListener() {
		          public void widgetSelected(SelectionEvent e) {
		            dialogMsg.close();
		          }

		          public void widgetDefaultSelected(SelectionEvent e) {
		          }
		        });

		        dialogMsg.pack();
		        dialogMsg.open();

	}

}
