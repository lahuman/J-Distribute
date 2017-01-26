package kr.pe.lahuman.ui.groups;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

public class UIGroups {

	
	public Composite wholeAreaUI(Shell shell) {
		Composite wholeComposite = new Composite(shell, SWT.NONE);
		GridLayout layoutWhole = new GridLayout();
		layoutWhole.numColumns = 1;
		wholeComposite.setLayout(layoutWhole);
		return wholeComposite;
	}
	
	public Group makeCommonInfoUI(Composite wholeComposite) {
		String commonGrouptitle = "COMMON INFORMATION";
		int commonColumns = 3;

		final Group commonGroup = makeGroupUI(wholeComposite, commonGrouptitle,
				commonColumns);

		// common Information
		
		return commonGroup;
	}
	
	public Group makeGroupUI(Composite wholeComposite,
			String commonGrouptitle, int commonColumns) {
		final Group commonGroup = new Group(wholeComposite, SWT.NONE);
		commonGroup.setText(commonGrouptitle);
		GridLayout layout = new GridLayout();
		layout.numColumns = commonColumns;
		commonGroup.setLayout(layout);

		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		commonGroup.setLayoutData(data);
		return commonGroup;
	}
}
