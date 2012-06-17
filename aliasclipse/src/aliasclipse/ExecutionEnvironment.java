package aliasclipse;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import aliasclipse.XMLFileParser.MenuItem;

/**
 * Contains variables for execution of commands like current file name, selected text etc.
 * This class is created everytime new command is executed.
 */
public class ExecutionEnvironment
{
	private final ArrayList<String> myCommands;
	private final ArrayList<MenuItem.ENV> myEnvs;
	
    private String mySelectedText;
    private String mySelectedPath;
    private String myEnclosingDir;
    private String myEnclosingMethod;

    private IWorkbench myWorkbench = PlatformUI.getWorkbench();
    private IWorkbenchWindow myActiveWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    
    public ExecutionEnvironment(ArrayList<String> commands,
                                ArrayList<MenuItem.ENV> envs)
    {
        myCommands = commands;
        myEnvs = envs;
    }
    
    /**
     * Executes the given command.
     */
    public boolean execute()
    {
        System.out.println("Executing: " + myCommands + " envs " + myEnvs);
        
        for (int i = 0; i < myCommands.size(); i++) {
            switch(myEnvs.get(i)) {
            case UNIX:
                runUnixCommand(myCommands.get(i));
                break;
            case WIN:
                runWinCommand(myCommands.get(i));
                break;
            case ECLIPSE:
                runEclipseAction(myCommands.get(i));
                break;
            default:
                System.out.println("Invalid mapping in the xml file. No env: "
                    + myEnvs.get(i));
            }
        }
        
        return true;
    }
    
    private boolean runEclipseAction(String string)
    {
        return true;        
    }

    private boolean runWinCommand(String command)
    {
        return true;
    }
    
    private boolean runUnixCommand(String command)
    {
        return Exec.executeSshCommand(command);
    }
    
    /**
     * Shows url represented by given string using external browser.
     */
    public void showInExternalBrowser(String url)
    {
        try {
            myWorkbench.getBrowserSupport().getExternalBrowser().openURL(new URL(url));
        }
        catch (PartInitException | MalformedURLException e) {
            showMessage("Cannot open url " + url + " Exception " + e.getMessage());
        }
    }

    /**
     * Shows given message to user in case of exceptions.
     */
    private void showMessage(String text)
    {
        MessageDialog.openInformation(myActiveWorkbenchWindow.getShell(),
                                      "Aliasclipse", 
                                      text);
        
    }
}
