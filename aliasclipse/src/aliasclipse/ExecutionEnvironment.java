package aliasclipse;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

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
        test();
        
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
        try {
            Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    private boolean runUnixCommand(String command)
    {
        return Exec.executeSshCommand(command);
    }
    
    /**
     * Returns selected text, null if no text is selected.
     */
    public String getSelectedText()
    {
        ISelection selection = myActiveWorkbenchWindow.getSelectionService()
            .getSelection();
        if (selection instanceof ITextSelection) {
            return ((ITextSelection) selection).getText();
        }
        return null;
    }

    /**
     * Gets the full path of the selected file in workbench for current os.
     */
    public String getFullOSPath()
    {
        if (getCurrentFile() != null) {
            return getCurrentFile().getLocation().toOSString();
        }
        return null;
    }

    private IFile getCurrentFile()
    {
        IEditorPart activeEditor = 
        		myActiveWorkbenchWindow.getActivePage().getActiveEditor();
        
        if (activeEditor == null) {
        	return null;
        }
        
		IEditorInput editorInput = activeEditor.getEditorInput();
        
        if (editorInput instanceof IFileEditorInput) {
            FileEditorInput fei = (FileEditorInput) editorInput;
            return fei.getFile();
        }
        return null;
    }

    /**
     * Gets the path relative to the workspace. First element of the path will
     * be the project name.
     */
    public String getRelativePath()
    {
        if (getCurrentFile() != null) {
            return getCurrentFile().getFullPath().toPortableString();
        }
        return null;
    }

    /**
     * Gets the file name without extension which is useful for searching for the classes.
     */
    public String getFileElementWithoutExtn()
    {
        String relativePath = getRelativePath();
        if (relativePath != null) {
            String[] split = relativePath.split("/");
            String lastElem = split[split.length - 1];
            return lastElem.split("\\.")[0];
        }

        return null;
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
    
    public void showInInternalBrowser(String url)
    {
        try {
            myWorkbench.getBrowserSupport()
                .createBrowser(url)
                .openURL(new URL(url));
        }
        catch (PartInitException | MalformedURLException e) {
            showMessage("Cannot open url " + url + " Exception "
                        + e.getMessage());
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
    
    public void test()
    {
        System.out.println("getSelectedText()" + getSelectedText());
        System.out.println("getFullOsPath()" + getFullOSPath());
        System.out.println("getRelativePath" + getRelativePath());
        System.out.println("getfileelement " + getFileElementWithoutExtn());
    }
}
