package gitviewer.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DiffViewer extends Dialog{

	private String message;
	protected DiffViewer(Shell parentShell, String message) {
		super(parentShell);
		this.message = message;
		// TODO Auto-generated constructor stub
	}

	@Override
	  protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		Text text = new Text(container, SWT.MULTI | SWT.BORDER | SWT.WRAP 
				| SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		text.setText(this.message);
		
		return container;
	}
	  @Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Diff of File");
	  }
}
