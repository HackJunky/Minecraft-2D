import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;
import org.xml.sax.SAXException;

/**
 * The DatabaseManager takes the primary user/information database and converts it to serializable information, 
 * from it's XML counterpart. It also takes any network commands, and converts those back into XML to reflect
 * changes made from the clients.
 * @author hackjunky
 *
 */
public class Database {
	//XML DOM Classes
	DocumentBuilderFactory dbFactory;
	File dbFile;
	DocumentBuilder dbBuilder;
	Document doc;

	Util util;

	public Database(Util u) {
		util = u;
		util.Log("Database ADO starting...");
		try {
			makeFS();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void refresh() {		
		dbFile = new File("world.dat");
		dbFactory = DocumentBuilderFactory.newInstance();
		dbBuilder = null;
		try {
			dbBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		doc = null;
		try {
			doc = dbBuilder.parse(dbFile);
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		doc.getDocumentElement().normalize();
	}

	/**
	 * Save the DB.XML file, and then Refresh to reflect the file changes.
	 */
	public void save() {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(dbFile);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		refresh();
	}

	public void makeFS() throws IOException {
		util.Log("Creating world datafile...");
		if (!new File("world.dat").exists()) {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null;
			try {
				docBuilder = docFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			Document doc = docBuilder.newDocument();
			Element rootElement = doc.createElement("World");
			rootElement.appendChild(doc.createElement("Players"));
			rootElement.appendChild(doc.createElement("Region"));
			doc.appendChild(rootElement);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = null;
			try {
				transformer = transformerFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			}
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File("world.dat"));
			try {
				transformer.transform(source, result);
			} catch (TransformerException e) {
				e.printStackTrace();
			}
			refresh();
		}
	}

	synchronized public void storePlayer(Player p) {
		try {
			NodeList nList = doc.getElementsByTagName(p.getName());
			if (nList.getLength() == 0) { 
				util.Log("Creating new handle for " + p.getName() + "...");
				nList = doc.getElementsByTagName("Players");
				for (int i = 0; i < nList.getLength(); i++) {
					Node nNode = nList.item(i);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element players = (Element)nNode;
						players.appendChild(doc.createElement(p.getName()));
						nList = doc.getElementsByTagName(p.getName());
					}
				}
			}
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (nNode.getNodeName().toLowerCase().equals(p.getName().toLowerCase())) {
						eElement.setAttribute("Hotbar", convertInventoryToString(p.getHotbar()));
						eElement.setAttribute("Inventory", convertInventoryToString(p.getInventory()));
					}
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String convertInventoryToString(Item[] items) {
		String resp = "";
		for (int i = 0; i < items.length; i++) {
			Item a = items[i];
			resp += a.toString() + ",";
		}
		resp = resp.substring(0, resp.length() - 2);
		return resp;
	}
	
	synchronized public boolean playerExists(String username) {
		NodeList nList = doc.getElementsByTagName(username);
		return (nList.getLength() == 1);
	}

	synchronized public Item[] getPlayerHotbar(String username) {
		try {
			NodeList nList = doc.getElementsByTagName(username);
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (nNode.getNodeName().toLowerCase().equals(username.toLowerCase())) {
						String hotbar = eElement.getAttribute("Hotbar");
						String[] split = hotbar.split(",");
						Item[] converted = new Item[split.length];
						for (int a = 0; a < split.length; a++) {
							String b = split[a];
							String blockName = b.split(";")[0];
							int count = Integer.parseInt(b.split(";")[1]);

							if (!blockName.equals("null")) {
								converted[a] = new Item(blockName, count);
							}
						}
						return converted;
					}
				}
			}
		}catch (Exception e) {
			return null;
		}
		return null;
	}

	synchronized public Item[] getPlayerInventory(String username) {
		try {
			NodeList nList = doc.getElementsByTagName(username);
			for (int i = 0; i < nList.getLength(); i++) {
				Node nNode = nList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) nNode;
					if (nNode.getNodeName().toLowerCase().equals(username.toLowerCase())) {
						String hotbar = eElement.getAttribute("Inventory");
						String[] split = hotbar.split(",");
						Item[] converted = new Item[split.length];
						for (int a = 0; a < split.length; a++) {
							String b = split[a];
							String blockName = b.split(";")[0];
							int count = Integer.parseInt(b.split(";")[1]);

							if (!blockName.equals("null")) {
								converted[a] = new Item(blockName, count);
							}
						}
						return converted;
					}
				}
			}
		}catch (Exception e) {
			return null;
		}
		return null;
	}

}
