package teetime.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Group;

public class PageFailure extends Composite {

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public PageFailure(Composite parent, int style) {
		super(parent, style);
		
		Label lblFailed = new Label(this, SWT.CENTER);
		lblFailed.setText("Failed!");
		lblFailed.setFont(SWTResourceManager.getFont("Lucida Grande", 14, SWT.BOLD));
		lblFailed.setBounds(56, 10, 343, 24);
		
		Group group = new Group(this, SWT.NONE);
		group.setBounds(103, 71, 249, 113);
		
		Label lblAnErrorOccurred = new Label(group, SWT.WRAP);
		lblAnErrorOccurred.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblAnErrorOccurred.setBounds(10, 28, 225, 55);
		lblAnErrorOccurred.setText("An error occurred or the operation was canceled.");
		lblAnErrorOccurred.setAlignment(SWT.CENTER);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
