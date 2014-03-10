package ui;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public class Windo extends JFrame {

	VidPanel panel =new VidPanel();
	public Windo() {
		super("Hearthstone Cardpack Opening Simulator");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.setSize(1024, 760);
		//795
		setResizable(false);
		this.setIconImage(Randomizer.getIcon("resources/icon.png"));
		this.add(panel);
		this.setVisible(true);
	}
	 public void setImage(final BufferedImage aImage)
	  {
	    panel.setImage(aImage);
	  }
	 
	 public static void main(String[] args){
		 new Windo();
	 }
}
