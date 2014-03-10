package ui;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.ImageIcon;

import sun.misc.Launcher;

/**
 * static variables and helper methods
 * @author Schippi
 *
 */
public class Randomizer {

	private static Random rand = new Random();
	private static ArrayList<ImgCard> common;
	private static ArrayList<ImgCard> rare;
	private static ArrayList<ImgCard> epic;
	private static ArrayList<ImgCard> legendary;
	private static ArrayList<BufferedImage> backs;
	private static String commonPath= "resources/cards/common/";
	private static String rarePath= "resources/cards/rare/";
	private static String epicPath= "resources/cards/epic/";
	private static String legendaryPath= "resources/cards/legendary/";
	private static String backsPath= "resources/backs/";

	static {
			common = new ArrayList<ImgCard>(94);
			epic = new ArrayList<ImgCard>(37);
			legendary = new ArrayList<ImgCard>(34);
			rare = new ArrayList<ImgCard>(81);
			backs = new ArrayList<BufferedImage>(7);
			
			final File jarFile = new File(Randomizer.class.getProtectionDomain().getCodeSource().getLocation().getPath()+"/HearthStoneSim.jar");
			try {
				if(jarFile.isFile()) {  // Run with JAR file
				    JarFile jar;
					jar = new JarFile(jarFile);
					
				    final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
				    while(entries.hasMoreElements()) {
				        final String name = entries.nextElement().getName();
				        final String subs= name.substring(name.lastIndexOf('/')+1);
				        if (name.startsWith(commonPath)) { //filter according to the path
				            add(common,subs);
				        }else if (name.startsWith(rarePath)) { //filter according to the path
				            add(rare,subs);
				        }else if (name.startsWith(epicPath)) { //filter according to the path
				            add(epic,subs);
				        }else if (name.startsWith(legendaryPath)) { //filter according to the path
				            add(legendary,subs);
				        }else if (name.startsWith(backsPath)) { //filter according to the path
				            backs.add(resizeToBig(getIcon("resources/backs/"+subs), 155, 230));
				        }
				    }
				    jar.close();
				} else { // Run with IDE
					 try {
					    URL url = Launcher.class.getResource("/" + commonPath);
					    if (url != null) {
					       final File apps = new File(url.toURI());
					       for (File app : apps.listFiles()) {
					    	   add(common,app.toString());
					       }
					    }
					    
					    url = Launcher.class.getResource("/" + rarePath);
					    if (url != null) {
					       final File apps = new File(url.toURI());
					       for (File app : apps.listFiles()) {
					    	   add(rare,app.toString());
					       }
					    }
					    
					    url = Launcher.class.getResource("/" + epicPath);
					    if (url != null) {
					       final File apps = new File(url.toURI());
					       for (File app : apps.listFiles()) {
					    	   add(epic,app.toString());
					       }
					    }
					    
					    url = Launcher.class.getResource("/" + legendaryPath);
					    if (url != null) {
					       final File apps = new File(url.toURI());
					       for (File app : apps.listFiles()) {
					    	   add(legendary,app.toString());
					       }
					    }
					    
					    url = Launcher.class.getResource("/" + backsPath);
					    if (url != null) {
					       final File apps = new File(url.toURI());
					       for (File app : apps.listFiles()) {
					    	   String s= app.toString();
					    	   backs.add(resizeToBig(getIcon("resources/backs/"+s.substring(s.lastIndexOf('\\')+1)),155,230));
					       }
					    }
					    
					 } catch (URISyntaxException ex) {
				            // never happens
				     }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	private static void add(ArrayList<ImgCard> list, String s){
			String l;
			byte q;
			if(list.equals(common)){
				l="common";
				q = ImgCard.COMMON;
			}else if(list.equals(rare)){
				l="rare";
				q = ImgCard.RARE;
			}else if(list.equals(epic)){
				l="epic";
				q = ImgCard.EPIC;
			}else {
				l="legendary";
				q = ImgCard.LEGENDARY;
			}
			list.add(new ImgCard(getIcon("resources/cards/"+l+"/"+s.substring(s.lastIndexOf('\\')+1)),180,280,q));

	}

	
	public static void playSound(final String s) {
		SoundThread t = new SoundThread(s);
		t.start();
	}
	
	





	private static int nonCommon=0;
	private static int cardCount=0;
	
	public static ImgCard getCard() {
		int choice = rand.nextInt(100);
		List<ImgCard> list;
		
		cardCount++;
		
		if(choice == 0){
			nonCommon++;
			list = legendary;
		}else if(choice < 5){
			nonCommon++;
			list = epic;
		}else if(choice < 20 || (cardCount == 5 && nonCommon == 0)){
			nonCommon++;
			list = rare;
		}else{
			list = common;
		}
		if(cardCount == 5){
			nonCommon = cardCount = 0;
		}
		return list.get(rand.nextInt(list.size()));
	}

	public static Image getIcon(String iconFileHelp) {
		try {
			ImageIcon icon = new ImageIcon(Randomizer.class.getClassLoader()
					.getResource(iconFileHelp));
			return icon.getImage();
		} catch (NullPointerException e) {
				System.out.println(iconFileHelp);
				e.printStackTrace();
				System.exit(1);
		}
		return null;
	}
	
	public static BufferedImage resizeToBig(Image originalImage, int biggerWidth, int biggerHeight) {
	    int type = BufferedImage.TYPE_INT_ARGB;


	    BufferedImage resizedImage = new BufferedImage(biggerWidth, biggerHeight, type);
	    Graphics2D g = resizedImage.createGraphics();

	    g.setComposite(AlphaComposite.Src);
	    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	    g.drawImage(originalImage, 0, 0, biggerWidth, biggerHeight, null);
	    g.dispose();


	    return resizedImage;
	}

	public static BufferedImage getBack() {
		return backs.get(rand.nextInt(backs.size()));
	}

}
