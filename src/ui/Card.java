package ui;

import java.awt.Point;
import java.awt.Rectangle;

public class Card {

	public Rectangle rect, oRect;
	public boolean clicked;
	public float alpha;
	public ImgCard img;
	public boolean hover = false;
	public boolean finished = false;
	
	
	public Card(Rectangle r){
		oRect = new Rectangle(r); 
		rect = r;
		clicked=false;
		alpha= 0.0f;
		img = new ImgCard();
	}
	
	public boolean contains(Point p){
		if(!rect.contains(p)){
			return false;
		}
		Point c = new Point(p.x-rect.x,p.y-rect.y);
		if(c.x<0 || c.y<0 || c.x >img.getImg().getWidth() || c.y > img.getImg().getHeight()){
			return false;
		}
		try{
			int color = img.getImg().getRGB(c.x, c.y);
			int alpha = (color>>24) & 0xff;
		    return alpha == 255;
		}catch(ArrayIndexOutOfBoundsException e){
			return false;
		}
	}
	
	/**
	 * gets this card a new randomized image
	 */
	public void newImage(){
		img = Randomizer.getCard();
	}
	
	/**
	 * plays the sound associated with current image
	 */
	public void playSound(){
		if(img.getQuality() == ImgCard.COMMON){
			Randomizer.playSound("resources/sounds/common.wav");
		}else if(img.getQuality() == ImgCard.RARE){
			Randomizer.playSound("resources/sounds/rare.wav");
		}else if(img.getQuality() == ImgCard.EPIC){
			Randomizer.playSound("resources/sounds/epic.wav");
		}else {
			Randomizer.playSound("resources/sounds/legendary.wav");
		}
	}
	
	public ImgCard getImgCard(){
		return img;
	}
	
	
}
