package aliasclipse;

import java.io.*;
import java.util.ArrayList;

import javax.xml.parsers.*;
import org.w3c.dom.*;

public class XMLFileParser 
{
	static public void main(String[] arg)
	{
		ArrayList<MenuItem> items = parseDocument("H:\\hackathon\\commands.xml");
		
		while(!items.isEmpty()) {
			System.out.println(items.remove(0));
		}
	}
	
	public static ArrayList<MenuItem> parseDocument(String filename)
	{
		try {
			File file = new File(filename);
			if (file.exists()) {
				// Create a factory
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				
				// Use the factory to create a builder
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document doc = builder.parse(filename);
				
				// Get a list of all elements in the document
				NodeList list = doc.getElementsByTagName("Element");
				ArrayList<MenuItem> menu = new ArrayList<>();

				for (int i = 0; i < list.getLength(); i++) {
					// Get element
					Element element = (Element) list.item(i);
					
					String id = element.getAttribute("id");
					String shortcut = element.getAttribute("shortcut");
					ArrayList<String> commands = new ArrayList<>();
					ArrayList<MenuItem.ENV> envs = new ArrayList<>();
					
					NodeList cmdsList = element.getElementsByTagName("cmd");

					for (int j = 0; j < cmdsList.getLength(); j++) {
						Element e = (Element) cmdsList.item(j);
						commands.add(e.getAttribute("value"));
						MenuItem.ENV env;
						
						switch (e.getAttribute("env")) {
			            case "UNIX":
			                env = MenuItem.ENV.UNIX;
			                break;
			            case "WIN":
			                env = MenuItem.ENV.WIN;
			                break;
			            case "ECLIPSE":
			                env = MenuItem.ENV.ECLIPSE;
			                break;
			            default:
			                throw new IllegalArgumentException(
			                    "Unknown environment " + e.getAttribute("env"));
			            }
						
						envs.add(env);
					}
					
					menu.add(new MenuItem(id, shortcut, envs, commands));
				}
				
				return menu;
			} 
			else {
				System.out.print("File not found!");
				return null;
			}
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public static class MenuItem
	{
		public enum ENV { WIN, ECLIPSE, UNIX };
		
		private String id;
		private ArrayList<String> cmds;
		private ArrayList<ENV> envs;
		private String shortcut;
		
		public MenuItem(String id, String shortcut, ArrayList<ENV> envs, ArrayList<String> cmds)
		{
			this.id = id;
			this.shortcut = shortcut;
			this.envs = envs;
			this.cmds = cmds;
		}
		
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public ArrayList<String> getCmds() {
			return cmds;
		}

		public void setCmds(ArrayList<String> cmds) {
			this.cmds = cmds;
		}

		public ArrayList<ENV> getEnvs() {
			return envs;
		}

		public void setEnvs(ArrayList<ENV> envs) {
			this.envs = envs;
		}

		public String getShortcut() {
			return shortcut;
		}

		public void setShortcut(String shortcut) {
			this.shortcut = shortcut;
		}

		@Override
		public String toString()
		{
			return id + '-' + envs + '-' + shortcut + '-' + cmds;
		}
	}
}