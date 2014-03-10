package ui;

import java.awt.Image;
import java.awt.image.BufferedImage;


public class ImgCard {

	private BufferedImage img;
	private BufferedImage origImg;
	private byte quality;
	public static final byte COMMON = 0;
	public static final byte RARE = 1;
	public static final byte EPIC = 2;
	public static final byte LEGENDARY = 3;
	
	/**
	 * image with quality attached to it
	 */
	public ImgCard(){
		img= null; //error
	}
	
	public ImgCard(Image i,byte q){
		img = Randomizer.resizeToBig(i,150,250);
		origImg = img;
		quality=q;
	}
	public ImgCard(Image i,int x, int y, byte q){
		img = Randomizer.resizeToBig(i,x,y);
		origImg = img;
		setQuality(q);
	}
	
	public BufferedImage getImg(){
		return img;
	}
	public void setImg(BufferedImage i){
		img=i;
	}
	public void newSize(int x, int y){
		img = Randomizer.resizeToBig(origImg,x,y);
	}

	/**
	 * @param quality the quality to set
	 */
	public void setQuality(byte quality) {
		this.quality = quality;
	}

	/**
	 * @return the quality
	 */
	public byte getQuality() {
		return quality;
	}
	
	
}
