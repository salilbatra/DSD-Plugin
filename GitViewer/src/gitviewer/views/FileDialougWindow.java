package gitviewer.views;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import gitviewer.views.GitViewer.CommitInfo;
import gitviewer.views.GitViewer.GitHelper;

public class FileDialougWindow extends Dialog{

	private Repository repository;
	private CommitInfo info;
	
	protected FileDialougWindow(Shell parentShell, Repository repository, Object obj) {
		super(parentShell);
		this.repository = repository;
		this.info = (CommitInfo) obj;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	  protected Control createDialogArea(Composite parent) {
	    Composite container = (Composite) super.createDialogArea(parent);
	    
	    GridData gd; 

	    gd = new GridData( 
	    GridData.FILL_BOTH | 
	    GridData.GRAB_HORIZONTAL | 
	    GridData.GRAB_VERTICAL ); 
	    
	    Table table = new Table(container, SWT.CENTER | SWT.H_SCROLL | SWT.V_SCROLL);
	    table.setLayoutData(gd);
	    table.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				Table table = (Table)e.getSource();
				TableItem item = table.getItem(table.getSelectionIndex());
				String path = (String) item.getData();
				RevWalk revWalk = new RevWalk(repository);
				revWalk.setRevFilter(
						CommitTimeRevFilter.before(info.commit.getCommitTime()*1000L)
						);
				revWalk.setTreeFilter(TreeFilter.ANY_DIFF);
				
				revWalk.sort(RevSort.COMMIT_TIME_DESC);
				try {
					
					ObjectId objectId = repository.resolve(Constants.HEAD);
					revWalk.markStart(revWalk.parseCommit(objectId));
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//					RevCommit oldCommit = revWalk.next();
//						for(RevCommit oldCommit : revWalk){
							
							AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, objectId);
				            AbstractTreeIterator newTreeParser = prepareTreeParser(repository, info.commit.getId());
				             
				            try (Git git = new Git(repository)) {
				                List<DiffEntry> diff = git.diff().
				                        setOldTree(oldTreeParser).
				                        setNewTree(newTreeParser).
				                        setPathFilter(PathFilter.create(path)).
				                        call();
				                for (DiffEntry entry : diff) {
				                    System.out.println("Entry: " + entry + ", from: " + entry.getOldId() + ", to: " + entry.getNewId());
				                    try (DiffFormatter formatter = new DiffFormatter(byteArrayOutputStream)) {
				                        formatter.setRepository(repository);
				                        formatter.format(entry);
				                    }
				                }
				                
				                
				            } catch (GitAPIException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
//						}
						showMessage(byteArrayOutputStream.toString());
					
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				
				
			}
		});
	    TableColumn tableColumn = new TableColumn(table, SWT.LEAD);
	    tableColumn.setText("File Path");
	    
	    
	    table.setHeaderVisible(true);
	    try(TreeWalk walk = new TreeWalk(repository)) {			
			walk.addTree(info.tree);
			walk.setRecursive(true);
			while(walk.next()){
				TableItem tableItem = new TableItem(table, SWT.NONE);
				FileMode fileMode = walk.getFileMode(0);
				fileMode.getObjectType();
				ObjectLoader loader = repository.open(walk.getObjectId(0));
				loader.getSize();
				tableItem.setText(
						new String[]{
						walk.getPathString()
						//, String.valueOf(loader.getSize()), getFileMode(fileMode)
						
						}
					);
				tableItem.setData(walk.getPathString());
				tableItem.addListener(1, new Listener() {
					
					@Override
					public void handleEvent(Event event) {
						// TODO Auto-generated method stub
						
					}
				});
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    tableColumn.pack();
	    
	    

	    return container;
	  }

	private static String getFileMode(FileMode fileMode) {
        if (fileMode.equals(FileMode.EXECUTABLE_FILE)) {
            return "Executable File";
        } else if (fileMode.equals(FileMode.REGULAR_FILE)) {
            return "Normal File";
        } else if (fileMode.equals(FileMode.TREE)) {
            return "Directory";
        } else if (fileMode.equals(FileMode.SYMLINK)) {
            return "Symlink";
        } else {
            // there are a few others, see FileMode javadoc for details
            throw new IllegalArgumentException("Unknown type of file encountered: " + fileMode);
        }
    }
	
	private void showMessage(String message) {
		DiffViewer viewer = new DiffViewer(super.getParentShell(), message);
		viewer.open();
	}

	
	private static AbstractTreeIterator prepareTreeParser(Repository repository, ObjectId objectId) throws IOException,
    MissingObjectException,
    IncorrectObjectTypeException {
// from the commit we can build the tree which allows us to construct the TreeParser
		try (RevWalk walk = new RevWalk(repository)) {
			RevCommit commit = walk.parseCommit(objectId);
			RevTree tree = walk.parseTree(commit.getTree().getId());
			
			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			try (ObjectReader oldReader = repository.newObjectReader()) {
				oldTreeParser.reset(oldReader, tree.getId());
			}
			
			walk.dispose();

    return oldTreeParser;
		}
}
	// overriding this methods allows you to set the
	  // title of the custom dialog
	  @Override
	  protected void configureShell(Shell newShell) {
	    super.configureShell(newShell);
	    newShell.setText("Affected Files");
	  }
}
