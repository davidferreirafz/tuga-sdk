package net.beiker.xletview.remotecontrol;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Vector;

import org.havi.ui.event.HRcEvent;

import net.beiker.xletview.event.EventManager;
import net.beiker.xletview.ui.Img;
import net.beiker.xletview.util.Debug;
import net.n3.nanoxml.XMLWriter;
import net.n3.nanoxml.XMLElement;
import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.IXMLReader;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;

/**
 * 
 * @author Martin Sveden
 */
public class RemoteControl extends Container implements KeyListener{

	private static RemoteControl THE_INSTANCE;
	
	private Container child;
	
	private String imgRoot;
	
	private Hashtable buttons;
	
	private RemoteControl(){
		buttons = new Hashtable();
	}
	
	public static RemoteControl getInstance(){
		if(THE_INSTANCE == null){
			THE_INSTANCE = new RemoteControl();
		}
		return THE_INSTANCE;
	}
	
	public void make(IXMLElement root){		
		
		String width = root.getAttribute("width", "");
		String height = root.getAttribute("height", "");

		setBounds(0, 0, parseInt(width), parseInt(height));	

		imgRoot = root.getAttribute("imgroot", "");
		
		
		resolveGroup(root, this);
		
		// bg color
		try {
			String strBg = root.getAttribute("backgroundcolor", "#000000");
			if(strBg != null){
				Color bg = Color.decode(strBg);		
				setBackground(bg);	
			}
		} catch (NumberFormatException e) {
		}
		
		String bgStr = imgRoot + root.getAttribute("backgroundimage", null);
		if(bgStr != null){
			Img bgImg = new Img(0, 0, bgStr);
			add(bgImg);
		}
//		
	}
	
	
	private void resolveGroup(IXMLElement group, Container cont){
		IXMLElement element = null;
		Vector v = group.getChildrenNamed("buttons");
		
		for (int i = 0; i < v.size(); i++) {
			element = (IXMLElement) v.get(i);
			
			int x = parseInt(element.getAttribute("x", "0"));
			int y = parseInt(element.getAttribute("y", "0"));
			int w = parseInt(element.getAttribute("width", "0"));
			int h = parseInt(element.getAttribute("height", "0"));
			
			Container c = new Container();
			c.setBounds(x, y, w, h);
			cont.add(c);
			
			resolveGroup(element, c);
			
		}
		v = group.getChildrenNamed("button");
		RemoteButton button = null;
		
		for (int i = 0; i < v.size(); i++) {
			element = (IXMLElement) v.get(i);
			String imgPath = imgRoot + element.getAttribute("img", "");
			int x = parseInt(element.getAttribute("x", "0"));
			int y = parseInt(element.getAttribute("y", "0"));
			int w = parseInt(element.getAttribute("width", "0"));
			int h = parseInt(element.getAttribute("height", "0"));
			
			String strKey = element.getAttribute("keycode", "###");						
			
			int keyCode = 0;
			
			try {
				keyCode = Integer.parseInt(strKey);
			} catch (NumberFormatException e) {					
			}
			
			
			try{
				String[] ss = strKey.split("\\.");			
				keyCode = getFieldValue(HRcEvent.class, ss[1]);				
			}
			catch(Exception e){				
			}			
			
			
			File imgFile = new File(imgPath);
			URL imgUrl = null;
			try {
				imgUrl = imgFile.toURL();
			} catch (MalformedURLException e1) {			
				e1.printStackTrace();
			}
			button = new RemoteButton(imgUrl, x, y, w, h, keyCode);
			buttons.put(keyCode + "", button);

			cont.add(button);
			button.addKeyListener(this);

		}
	}
	
	
	private int parseInt(String s){
		int result = 0;
		try {
			result = Integer.parseInt(s);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public Dimension getPreferredSize(){
		return new Dimension(getWidth(), getHeight());
	}
	
	
	
	public void paint(Graphics g){
		Color bg = getBackground();
		if(bg != null){
			g.setColor(bg);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		super.paint(g);
	}
	
	public static void main(String[] args) {
		String s = "KeyEvent.VK_UP";
		int i = KeyEvent.VK_0;
		String[] ss = s.split("\\.");
		
		getFieldValue(HRcEvent.class, ss[1]);
		
		
	}
	
	private static int getFieldValue(Class c, String fieldName) {
		int result = -1000;
		Field[] publicFields = c.getFields();
		for (int i = 0; i < publicFields.length; i++) {
			if(publicFields[i].getName().equals(fieldName)){
			
				Class typeClass = publicFields[i].getType();
				
				try {
					result = publicFields[i].getInt(c);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				String fieldType = typeClass.getName();
				//System.out.println("FOUND - Name: " + fieldName + ", Type: " + fieldType + ", Value: " + result);
			}
		}
		return result;
	}
	
	public void keyTyped(KeyEvent e) {
		//Debug.write(this, "keyTyped " + e);
		fireEvent(e);
	}


	public void keyPressed(KeyEvent e) {
		//Debug.write(this, "keyPressed " + e);
		fireEvent(e);
	}


	public void keyReleased(KeyEvent e) {
		//Debug.write(this, "keyReleased " + e);
		fireEvent(e);
	}
	
	public void setPressed(int keyCode){ 
		Object obj = buttons.get(keyCode + "");
		if(obj instanceof RemoteButton){
			( (RemoteButton)obj).setOn();
		}
	}
	
	public void setReleased(int keyCode){
		Object obj = buttons.get(keyCode + "");
		if(obj instanceof RemoteButton){
			( (RemoteButton)obj).setNormal();
		}
	}
	
	/** 
	 * 
	 * @param keyEvent
	 */
	public void fireEvent(KeyEvent keyEvent){
		EventManager.getInstance().fireRemoteEvent(keyEvent);
	}
}
