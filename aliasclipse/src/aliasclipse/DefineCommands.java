
package aliasclipse;

import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.menus.ExtensionContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.services.IServiceLocator;

import aliasclipse.XMLFileParser.MenuItem;

/**
 * Defines all menu items to add for "Aliasclipse". The menu items are read from user
 * specified file along with shortcuts and the command to be executed.
 */
public class DefineCommands extends ExtensionContributionFactory
{
    /**
     * Add our own menu manager to flexibly provide shortcuts.
     */
    @Override
    public void createContributionItems(IServiceLocator serviceLocator,
                                        IContributionRoot additions)
    {
        MenuManager menuManager = new MenuManager("Aliasclipse");
        
        ArrayList<MenuItem> list =
        		XMLFileParser.parseDocument(System.getProperty("configfile"));
        
        for (int  i=0; i< list.size(); i++) {
        	MenuItem item = list.get(i);
        	
        	addAction(item.getId(), item.getShortcut(),
        	          item.getCmds(), item.getEnvs(), menuManager);
        }
        additions.addContributionItem(menuManager, null);
    }
    
    
    /**
     * Adds given action to the menu manager.
     */
    private void addAction(final String label,
                           final String shortcut,
                           final ArrayList<String> commands,
                           final ArrayList<MenuItem.ENV> envs,
                           final MenuManager menuMgr)
    {
        Action action = new Action(label) {
            @Override
            public void run()
            {
                ExecutionEnvironment e = new ExecutionEnvironment(commands, envs);
                e.execute();
            }
        };
        
        action.setAccelerator(getKeyCode(shortcut));
        
        menuMgr.add(action);  
        System.out.println("added action");
    }
    
    /**
     * Gets keycode for ctrl+shift+alt+alphanum combinations.
     */
    private int getKeyCode(String shortcut)
    {
        String[] split = shortcut.split("\\+");
        int code = 0;
        
        for (String s: split) {
            if (s.equalsIgnoreCase("CTRL")) {
                code += SWT.CTRL;
            }
            else if (s.equalsIgnoreCase("SHIFT")) {
                code += SWT.SHIFT;
            }
            else if (s.equalsIgnoreCase("ALT")) {
                code += SWT.ALT;
            }            
            else if (s.length() == 1) {
                code += s.charAt(0);
            }           
        }
        return code;
    }
}