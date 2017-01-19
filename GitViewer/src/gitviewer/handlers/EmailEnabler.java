package gitviewer.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.events.ConfigChangedEvent;
import org.eclipse.jgit.events.ConfigChangedListener;
import org.eclipse.jgit.events.IndexChangedEvent;
import org.eclipse.jgit.events.IndexChangedListener;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class EmailEnabler extends AbstractHandler {
	public static void main(String[] args) {
		EmailSender.sendMail("Hello World");
	}
	static final String GIT_WORKBOOK = "<change>";
	static boolean toggleValue=false;

	/**
	 * The constructor.
	 */
	public EmailEnabler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		/*
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		Command command = event.getCommand();
		boolean value = HandlerUtil.toggleCommandState(command);
		IResourceChangeListener listener = new CustomResourceListener(window);
		if(!value){
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
		}else{
			ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		}
		*/
		Repository repository;
		try {
			repository = new FileRepositoryBuilder().setWorkTree(new File(GIT_WORKBOOK)).build();
			Path dir = repository.getWorkTree().toPath();
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Command command = event.getCommand();
			toggleValue = !HandlerUtil.toggleCommandState(command);
			Thread thread = new Thread(new MyRunnable(dir, watcher));
			thread.start();
			//filelistener(dir, watcher, value);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		return null;
	}

	
	
}

class EmailSender {
	static void sendMail(String Stringmessage){
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", "587");
		
		props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
		
		javax.mail.Authenticator authenticator = new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication("<user name>", "<password>");
			}
		  };
		Session mailsession = Session.getInstance(props,authenticator);
		try {

			Message mimemessage = new MimeMessage(mailsession);
			mimemessage.setFrom(new InternetAddress("sengar.kudleep@gmail.com"));
			mimemessage.setRecipients(Message.RecipientType.TO,
				InternetAddress.parse("sengar.kuldeep@live.com"));
			mimemessage.setSubject("File Changed");
			mimemessage.setText(Stringmessage);

			Transport.send(mimemessage);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
class MyRunnable implements Runnable {

	private Path path;
	private WatchService watcher;
	
	public MyRunnable(Path path, WatchService watcher) {
		super();
		this.path = path;
		this.watcher = watcher;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		if(EmailEnabler.toggleValue){
			try {
				
				path.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (true) {
				WatchKey watcherkey;
			    try {
			        
			    	watcherkey = watcher.take();
			    	if(!EmailEnabler.toggleValue){
			    		return;
			    	}
			    } catch (InterruptedException ex) {
			       return;
			    }
			    
			    for (WatchEvent<?> watchevent : watcherkey.pollEvents()) {
			    	WatchEvent<Path> pev = (WatchEvent<Path>) watchevent;
		            Path file = pev.context();
			    	if(watchevent.kind() == ENTRY_MODIFY){
			    		EmailSender.sendMail("The file is changed :: " + file.getFileName());
			    		System.out.println(file.getFileName().toString());
			    	}
			    	
			    }
			}
		}
		
	}
	
	
	
	
}
