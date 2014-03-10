package ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JPanel;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.IAudioSamples;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

public class VidPanel extends JPanel {

	private BufferedImage cImage;
	private IStreamCoder audioCoder = null;
	private IStreamCoder videoCoder = null;
	private static SourceDataLine mLine;
	private IContainer container = IContainer.make();
	private IVideoResampler resampler = null;
	private float screenAlpha= 0.0f;
	private BufferedImage shadedImage,statsglow,packGlow;
	private RescaleOp[] cGlow = new RescaleOp[5];
	
	private Rectangle slot = new Rectangle(530,240,170,250);
	private volatile int numLeft = 5;

	private static long mSystemVideoClockStartTime;
	private static long mFirstVideoTimestampInStream;

	private MouseAdapter card, done, statsListener;
	private PackListener packL;
	private HoverCard hover;
	private Card top, topRight, topLeft, botLeft, botRight, doneBtn, statsBtn, backBtn, pack, glowBox, doneBtnOrigin;
	private Image shop, glow, backI, legend, screen;
	private boolean vidOver = false;
	private int numImg=0;
	
	
	private int[] counter = new int[5];
	private boolean stats=false;

	private Image temp = Randomizer.getIcon("resources/statsBtn.png");
	
	//where the magic happens
	public VidPanel() {
		super();
		screen = Randomizer.resizeToBig(Randomizer.getIcon("resources/cards.png"),1024,768);
		shop = Randomizer.resizeToBig(Randomizer.getIcon("resources/shop2.png"),1024,768);
		backI = Randomizer.getBack();
		
		legend = Randomizer.resizeToBig(Randomizer.getIcon("resources/legend.png"),64,230);
		
		counter[0]=0;
		counter[1]=0;
		counter[2]=0;
		counter[3]=0;
		counter[4]=0;
		
		float[] offsets = new float[4];
		float[] scalesW = { 0.7f, 0.7f, 0.7f, 0.1f };
		cGlow[0] = new RescaleOp(scalesW, offsets, null);
		
		float[] scalesB = { 0.3f, 0.3f, 0.8f, 0.4f };
		cGlow[1] = new RescaleOp(scalesB, offsets, null);
		
		float[] scalesP = { 0.8f, 0.1f, 0.8f, 0.1f };
		cGlow[2] = new RescaleOp(scalesP, offsets, null);
		
		float[] scalesO = { 0.8f, 0.45f, 0.1f, 0.1f };
		cGlow[3] = new RescaleOp(scalesO, offsets, null);
		
		float[] scales = { 0.4f, 0.4f, 0.8f, 0.3f };
		cGlow[4] = new RescaleOp(scales, offsets, null);
		
		
		glow = Randomizer.resizeToBig(Randomizer.getIcon("resources/cards/glow.png"),230,350);
		statsglow = toBufferedImage(Randomizer.resizeToBig(Randomizer.getIcon("resources/cards/glow.png"),135,80));
		packGlow = toBufferedImage(Randomizer.resizeToBig(Randomizer.getIcon("resources/cloud.png"),250,330));
		
		shadedImage = toBufferedImage(glow);		
		
		initCards();
		initMouseAdapter();
		this.setSize(1024, 768);
		
		statsListener = new HoverMenu();
		
		packL = new PackListener();
		this.addMouseListener(packL);
		this.addMouseMotionListener(packL);
		this.addMouseMotionListener(statsListener);
		this.addMouseListener(statsListener);
	}
	
	//converts Images to Buffered ones
	private BufferedImage toBufferedImage(Image i) {
		BufferedImage bi = new
	    BufferedImage(i.getWidth(null), i.getHeight(null), BufferedImage.TYPE_INT_ARGB);
		Graphics g = bi.getGraphics();
		g.drawImage(i, 0, 0, null);
	
        return bi;
    }
	
//	initializes the cards
	private void initCards() {
		top = new Card(new Rectangle(530, 45, 180, 280));
		topLeft = new Card(new Rectangle(330, 115, 180, 280));
		topRight = new Card(new Rectangle(750, 120, 180, 280));
		botLeft = new Card(new Rectangle(415, 398, 180, 280));
		botRight = new Card(new Rectangle(650, 405, 180, 280));
		topLeft.alpha = topRight.alpha = top.alpha = botLeft.alpha = botRight.alpha = 0.0f;
		
		pack  = new Card(new Rectangle(28, 248, 150, 250));
		pack.alpha=1.0f;
		pack.img.setImg(toBufferedImage(Randomizer.getIcon("resources/pack.png")));
		
		glowBox  = new Card(new Rectangle(300, 0, 768, 680));
		glowBox.img.setImg(toBufferedImage(Randomizer.getIcon("resources/glowBox.png")));
		
		doneBtn = new Card(new Rectangle(555,340,150,80));
		doneBtnOrigin = new Card(new Rectangle(555,340,150,80));
		doneBtn.img.setImg(Randomizer.resizeToBig(Randomizer.getIcon("resources/done2.png"),150,80));
		doneBtnOrigin.img.setImg(Randomizer.resizeToBig(Randomizer.getIcon("resources/done2.png"),150,80));
		
		
		statsBtn = new Card(new Rectangle(40,560,135,135));
		statsBtn.alpha = 1.0f;
		statsBtn.img.setImg(Randomizer.resizeToBig(temp, 135, 135));
		
		backBtn = new Card(new Rectangle(925,690,65,30));
		backBtn.alpha = 1.0f;
		backBtn.img.setImg(Randomizer.resizeToBig(Randomizer.getIcon("resources/backHover.png"),65,30));
		
		
		
	}

	/**
	 * if a MAIN card contains given point
	 * @param p point of clickage
	 * @return  null or card that contains the point
	 */
	private Card containsCard(Point p){
		final Card c;
		if (top.contains(p)) {
			c=top;
		} else if (topLeft.contains(p) ) {
			c=topLeft;
		} else if (topRight.contains(p)) {
			c=topRight;
		} else if (botLeft.contains(p)) {
			c=botLeft;
		} else if (botRight.contains(p)) {
			c=botRight;
		} else {
			return null;
		}
		return c;
	}
	
	/**
	 * manages which cards are hovered over
	 * @author Schippi
	 */
	private class HoverCard extends MouseAdapter{
		public volatile Card lastC = new Card(new Rectangle());
		private Object sync= new Object();
		@Override
		public void mouseMoved(MouseEvent m){
			final Card c = containsCard(m.getPoint());
			synchronized(sync){
				if(c!= null && !c.equals(lastC)){
					lastC = c;
					//maaaaaaaaaybe TODO
					top.hover = topLeft.hover = topRight.hover = botLeft.hover = botRight.hover = false;
					lastC.hover=true;
					repaint();
					Randomizer.playSound("resources/sounds/hover.wav");
					return;
				}else {
					if(lastC!=null && !lastC.equals(c) 
//							&& lastC.alpha == 0.0f
							){
						lastC.hover=false;
						repaint();
					}
					if(c == null){
						lastC = null;
					}
				}
			}
		}
		@Override
		public void mouseExited(MouseEvent m){
			mouseMoved(m);
		}
	}
	
	/**
	 * manages the drag and drop of the cardpack
	 * @author Schippi
	 *
	 */
	private class PackListener extends MouseAdapter{
		Rectangle origin;
		boolean pressed;
		Point click;
		Thread t,sound;
		
		public PackListener(){
			origin= new Rectangle(pack.rect);
			pressed=false;
			sound = new Thread();
		}
		
		public boolean isPressed(){
			return pressed;
		}
		
		public void mouseMoved(MouseEvent m){
			Point p =m.getPoint();
			boolean old = pack.hover;
			if(pack.contains(p)){
				pack.hover=true;
			}else{
				pack.hover=false;
			}
			if((old ^ pack.hover) && (pressed || origin.equals(pack.rect))){
				repaint(pack.rect.x-45,pack.rect.y-60,250,330);
			}
		}
		public void mousePressed(MouseEvent m){
			Point p =m.getPoint();
			shadedImage = toBufferedImage(Randomizer.resizeToBig(Randomizer.getIcon("resources/cloud.png"),280,400));
			if(pack.contains(p) && MouseEvent.BUTTON1==m.getButton()){
				pressed=true;
				pack.hover=true;
				stats=false;
				click = new Point(p.x-origin.x,p.y-origin.y);
				t=new GlowBoxThread();
				t.start();
				Randomizer.playSound("resources/sounds/packGrab.wav");
				sound = new SoundThread("resources/sounds/grab.wav",true);
				sound.start();
			}else{
				pressed=false;
				pack.hover=false;
			}
			
		}
	
		public void mouseReleased(MouseEvent m){
			Point p =m.getPoint();
			Rectangle r= new Rectangle(pack.rect);
			if(slot.contains(p) && pressed){
				pack.rect.x=slot.x+10;
				pack.rect.y=slot.y+20;
				pressed = false;
				pack.hover=false;
				resetAndRun();
			}else if(pressed){
				pressed = false;
				t.interrupt();
				glowBox.alpha=0.0f;
				pack.rect=new Rectangle(origin);
				if(!pack.contains(p)){
					pack.hover=false;
				}
				repaint(r.x-45,r.y-60,250,330);
				repaint(pack.rect);
				repaint(glowBox.rect);
				Randomizer.playSound("resources/sounds/packGrabLetGo.wav");
			}
			sound.interrupt();
			
			
		}
		public void mouseDragged(MouseEvent m){
			Point p =m.getPoint();
			if(pressed){
				Rectangle r= new Rectangle(pack.rect);
				pack.rect.x= p.x-click.x;
				pack.rect.y= p.y-click.y;
				repaint(r.x-45,r.y-60,250,330);
				repaint(pack.rect);
				repaint();
			}
			
		}
	}
	
	/**
	 * manages the glow of the blue background-lines
	 * @author Schippi
	 *
	 */
	private class GlowBoxThread extends Thread{
		public void run(){
			long t = System.currentTimeMillis() + 510;
			while (t > System.currentTimeMillis() && packL.isPressed() && !isInterrupted()) {
				float alpha = (t - System.currentTimeMillis()) / 510f;
				glowBox.alpha = 1-alpha;
				repaint(glowBox.rect);
				try {
					Thread.sleep(15);
				} catch (InterruptedException e) {
					continue;
				}
			}
			if(packL.isPressed()){
				glowBox.alpha = 1.0f;
			}else{
				glowBox.alpha = 0.0f;
			}
			repaint(glowBox.rect);
		}
		
		@Override
		public void interrupt(){
			super.interrupt();
			if(packL.isPressed()){
				glowBox.alpha = 1.0f;
			}else{
				glowBox.alpha = 0.0f;
			}
			repaint(glowBox.rect);
		}
		
	}
	
	/**
	 * manages the stats and backButtons
	 * @author Schippi
	 *
	 */
	private class HoverMenu extends MouseAdapter{
		
		public void mouseReleased(MouseEvent m){
			if(statsBtn.contains(m.getPoint())){
				stats = !stats;
				Randomizer.playSound("resources/sounds/shopClick.wav");
				repaint(540, 255, 250,250);
				repaint(455, 685, 350, 60);
				repaint(5,710,330,30);
			}else if(backBtn.contains(m.getPoint())){
				Randomizer.playSound("resources/sounds/shopClick.wav");
				sleep(200);
				System.exit(0);
			}
		}
		
		private void sleep(long l){
			try {
				Thread.sleep(l);
			} catch (InterruptedException e) {}
		}
		
		@Override
		public void mouseMoved(MouseEvent m){
			boolean prevStatsHover = statsBtn.hover;
			boolean prevBackBtnHover = (backBtn.alpha==1.0f);
			
			
			if(statsBtn.contains(m.getPoint())){
				statsBtn.hover=true;
			}else{
				statsBtn.hover=false;
			}
			
			if(backBtn.rect.contains(m.getPoint())){
				backBtn.alpha=1.0f;
			}else{
				backBtn.alpha=0.0f;
			}
			
			if(prevBackBtnHover ^ (backBtn.alpha==1.0f)){// = XOR
				repaint(backBtn.rect);
			}
			if(prevStatsHover ^ statsBtn.hover){// = XOR
				repaint(statsBtn.rect);
			}
			
			if((!prevStatsHover && statsBtn.hover) || (!prevBackBtnHover && (backBtn.alpha==1.0f))){
				Randomizer.playSound("resources/sounds/shopHover.wav");
			}
		}
		@Override
		public void mouseExited(MouseEvent m){
			mouseMoved(m);
		}
	}
	
	/**
	 * resets most values, randomizes new cards and starts the video in a new thread
	 */
	private void resetAndRun(){
		removeMouseListener(packL);
		removeMouseMotionListener(packL);
		statsBtn.hover = false;
		removeMouseMotionListener(statsListener);
		removeMouseListener(statsListener);
		new Thread() {
			public void run() {
				backI = Randomizer.getBack();
				vidOver=false;
				stats = false;
				numImg=0;
				counter[0]++; // increase numPacks
				top.clicked = topLeft.clicked = topRight.clicked = botLeft.clicked = botRight.clicked = false;
				newImages();
				numLeft = 5;
				playVid();
				screenAlpha=1.0f;
				vidOver=true;
				repaint();
			}
		}.start();
		pack.alpha=0.0f;
		pack.rect=new Rectangle(pack.oRect);
	}
	
	/**
	 * initializes (some) mouseadapters, namely the ones for the cards and the doneButton
	 */
	private void initMouseAdapter() {
		
		class Done extends MouseAdapter {
			
			@Override
			public void mouseMoved(MouseEvent m) {
				if (!doneBtn.hover && (doneBtn.contains(m.getPoint()))) {
					doneBtn.hover=true;
					doneBtn.rect.y+=10;
					repaint(doneBtn.rect.x-5,doneBtn.rect.y-15,doneBtn.rect.width+10,doneBtn.rect.height+30);
					Randomizer.playSound("resources/sounds/shopHover.wav");
				}else if(doneBtn.hover && !doneBtn.contains(m.getPoint()) && !doneBtnOrigin.contains(m.getPoint())){
					doneBtn.hover=false;
					doneBtn.rect.y-=10;
					repaint(doneBtn.rect.x-5,doneBtn.rect.y-15,doneBtn.rect.width+10,doneBtn.rect.height+30);
				}
			}
			
			@Override
			public void mouseReleased(MouseEvent m) {
				if(m.getButton()!=MouseEvent.BUTTON1){
					return;
				}
				if (doneBtn.contains(m.getPoint())&& vidOver) {
					Randomizer.playSound("resources/sounds/shopClick.wav");
					Randomizer.playSound("resources/sounds/done.wav");
					removeMouseListener(done);
					removeMouseMotionListener(done);
					removeMouseMotionListener(hover);
					new Thread() {
						public void run() {
							long t = System.currentTimeMillis() + 1000;
							while (t > System.currentTimeMillis()) {
								float alpha = (t - System.currentTimeMillis()) / 1000f;
								top.alpha = topRight.alpha = topLeft.alpha = botLeft.alpha = botRight.alpha = doneBtn.alpha = screenAlpha = glowBox.alpha= alpha;
								statsBtn.alpha = pack.alpha = 1 - alpha; 
								repaint();

								try {
									Thread.sleep(25);
								} catch (InterruptedException e) {
								}
							}
							top.alpha = topRight.alpha = topLeft.alpha = botLeft.alpha = botRight.alpha = doneBtn.alpha = screenAlpha = glowBox.alpha = 0.0f;
							statsBtn.alpha = pack.alpha = 1.0f; 
							doneBtn.rect=new Rectangle(doneBtn.oRect);
							top.finished = topRight.finished = topLeft.finished = botLeft.finished = botRight.finished = doneBtn.finished = false;
							top.hover = topLeft.hover = topRight.hover = botLeft.hover = botRight.hover = false;
							hover.lastC = new Card(new Rectangle());
							repaint();
							addMouseMotionListener(packL);
							addMouseListener(packL);
							addMouseMotionListener(statsListener);
							addMouseListener(statsListener);
						}

					}.start();
				}
			}

		}
		;
		class CardTop extends MouseAdapter {

			@Override
			public void mouseReleased(MouseEvent m) {
				if(m.getButton()!=MouseEvent.BUTTON1){
					return;
				}
				final Card c;
				Point p = m.getPoint();
				if (top.rect.contains(p) && !top.clicked) {
					c=top;
				} else if (topLeft.rect.contains(p) && !topLeft.clicked) {
					c=topLeft;
				} else if (topRight.rect.contains(p) && !topRight.clicked) {
					c=topRight;
				} else if (botLeft.rect.contains(p) && !botLeft.clicked) {
					c=botLeft;
				} else if (botRight.rect.contains(p) && !botRight.clicked) {
					c=botRight;
				} else{
					return;
				}

				c.clicked = true;
				counter[c.getImgCard().getQuality()+1]++;
				c.playSound();
				
				new Thread() {
					public void run() {
						
						long t = System.currentTimeMillis() + 1000;
						while (t > System.currentTimeMillis()) {
							c.alpha =  1 - (t - System.currentTimeMillis()) / 1000f;
							repaint(c.rect);
							
							if (numLeft == 1){
								doneBtn.alpha= c.alpha;
								repaint(doneBtn.rect);
							}
							
							try {
								Thread.sleep(25);
							} catch (InterruptedException e) {
							}
						}
						c.alpha= 1.0f;
						c.finished = true;
						repaint(c.rect);
						if (numLeft == 1) {
							removeMouseListener(card);
							numLeft = 5;
							addMouseListener(done);
							addMouseMotionListener(done);
						}else{
							numLeft--;
						}

					}
				}.start();
			}

		}
		;


		done = new Done();
		card = new CardTop();
		hover = new HoverCard();
	}

	/**
	 * shuffles cards so one card isnt always the gaguanteed rare
	 */
	private void newImages(){
		List<Card> list = new ArrayList<Card>(5);
		list.add(top);
		list.add(topRight);
		list.add(topLeft);
		list.add(botLeft);
		list.add(botRight);
		Collections.shuffle(list);
		for (Card card : list) {
			card.newImage();
		}
	}
	
	/**
	 * plays the cardexploding video (mostly taken from xuggler examples
	 */
	@SuppressWarnings("deprecation")
	protected void playVid() {

		String filename = "video.mp4";
		
		// Let's make sure that we can actually convert video pixel formats.
		if (!IVideoResampler
				.isSupported(IVideoResampler.Feature.FEATURE_COLORSPACECONVERSION))
			throw new RuntimeException(
					"you must install the GPL version of Xuggler (with IVideoResampler support) for this demo to work");

		// Create a Xuggler container object
		container = IContainer.make();

		// Open up the container
		if (container.open(filename, IContainer.Type.READ, null) < 0)
			throw new IllegalArgumentException("could not open file: "
					+ filename);

		// query how many streams the call to open found
		int numStreams = container.getNumStreams();

		// and iterate through the streams to find the first audio stream
		int videoStreamId = -1;
		videoCoder = null;
		int audioStreamId = -1;
		audioCoder = null;
		for (int i = 0; i < numStreams; i++) {
			// Find the stream object
			IStream stream = container.getStream(i);
			// Get the pre-configured decoder that can decode this stream;
			IStreamCoder coder = stream.getStreamCoder();

			if (videoStreamId == -1
					&& coder.getCodecType() == ICodec.Type.CODEC_TYPE_VIDEO) {
				videoStreamId = i;
				videoCoder = coder;
			} else if (audioStreamId == -1
					&& coder.getCodecType() == ICodec.Type.CODEC_TYPE_AUDIO) {
				audioStreamId = i;
				audioCoder = coder;
			}
		}
		if (videoStreamId == -1 && audioStreamId == -1)
			throw new RuntimeException(
					"could not find audio or video stream in container: "
							+ filename);

		/*
		 * Check if we have a video stream in this file. If so let's open up our
		 * decoder so it can do work.
		 */
		resampler = null;
		if (videoCoder != null) {
			if (videoCoder.open() < 0)
				throw new RuntimeException(
						"could not open audio decoder for container: "
								+ filename);

			if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24) {
				// if this stream is not in BGR24, we're going to need to
				// convert it. The VideoResampler does that for us.
				resampler = IVideoResampler.make(videoCoder.getWidth(),
						videoCoder.getHeight(), IPixelFormat.Type.BGR24,
						videoCoder.getWidth(), videoCoder.getHeight(),
						videoCoder.getPixelType());
				if (resampler == null)
					throw new RuntimeException(
							"could not create color space resampler for: "
									+ filename);
			}
			/*
			 * And once we have that, we draw a window on screen
			 */
		}

		if (audioCoder != null) {
			if (audioCoder.open() < 0)
				throw new RuntimeException(
						"could not open audio decoder for container: "
								+ filename);

			/*
			 * And once we have that, we ask the Java Sound System to get itself
			 * ready.
			 */
			try {
				openJavaSound(audioCoder);
			} catch (LineUnavailableException ex) {
				throw new RuntimeException(
						"unable to open sound device on your system when playing back container: "
								+ filename);
			}
		}

		/*
		 * Now, we start walking through the container looking at each packet.
		 */
		IPacket packet = IPacket.make();
		mFirstVideoTimestampInStream = Global.NO_PTS;
		mSystemVideoClockStartTime = 0;
		while (container.readNextPacket(packet) >= 0) {
			/*
			 * Now we have a packet, let's see if it belongs to our video stream
			 */
			if (packet.getStreamIndex() == videoStreamId) {
				/*
				 * We allocate a new picture to get the data out of Xuggler
				 */
				IVideoPicture picture = IVideoPicture.make(
						videoCoder.getPixelType(), videoCoder.getWidth(),
						videoCoder.getHeight());

				/*
				 * Now, we decode the video, checking for any errors.
				 */
				int bytesDecoded = videoCoder.decodeVideo(picture, packet, 0);
				if (bytesDecoded < 0)
					throw new RuntimeException("got error decoding audio in: "
							+ filename);

				/*
				 * Some decoders will consume data in a packet, but will not be
				 * able to construct a full video picture yet. Therefore you
				 * should always check if you got a complete picture from the
				 * decoder
				 */
				if (picture.isComplete()) {
					IVideoPicture newPic = picture;
					/*
					 * If the resampler is not null, that means we didn't get
					 * the video in BGR24 format and need to convert it into
					 * BGR24 format.
					 */
					if (resampler != null) {
						// we must resample
						newPic = IVideoPicture.make(
								resampler.getOutputPixelFormat(),
								picture.getWidth(), picture.getHeight());
						if (resampler.resample(newPic, picture) < 0)
							throw new RuntimeException(
									"could not resample video from: "
											+ filename);
					}
					if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
						throw new RuntimeException(
								"could not decode video as BGR 24 bit data in: "
										+ filename);

					long delay = millisecondsUntilTimeToDisplay(newPic);
					// if there is no audio stream; go ahead and hold up the
					// main thread. We'll end
					// up caching fewer video pictures in memory that way.
					try {
						if (delay > 0)
							Thread.sleep(delay);
					} catch (InterruptedException e) {
						return;
					}

					// And finally, convert the picture to an image and display
					// it
					setImage(Utils.videoPictureToImage(newPic));

				}
			} else if (packet.getStreamIndex() == audioStreamId) {
				/*
				 * We allocate a set of samples with the same number of channels
				 * as the coder tells us is in this buffer.
				 * 
				 * We also pass in a buffer size (1024 in our example), although
				 * Xuggler will probably allocate more space than just the 1024
				 * (it's not important why).
				 */
				IAudioSamples samples = IAudioSamples.make(1024,
						audioCoder.getChannels());

				/*
				 * A packet can actually contain multiple sets of samples (or
				 * frames of samples in audio-decoding speak). So, we may need
				 * to call decode audio multiple times at different offsets in
				 * the packet's data. We capture that here.
				 */
				int offset = 0;

				/*
				 * Keep going until we've processed all data
				 */
				while (offset < packet.getSize()) {
					int bytesDecoded = audioCoder.decodeAudio(samples, packet,
							offset);
					if (bytesDecoded < 0)
						throw new RuntimeException(
								"got error decoding audio in: " + filename);
					offset += bytesDecoded;
					/*
					 * Some decoder will consume data in a packet, but will not
					 * be able to construct a full set of samples yet. Therefore
					 * you should always check if you got a complete set of
					 * samples from the decoder
					 */
					if (samples.isComplete()) {
						// note: this call will block if Java's sound buffers
						// fill up, and we're
						// okay with that. That's why we have the video
						// "sleeping" occur
						// on another thread.
						playJavaSound(samples);
					}
				}
			} else {
				/*
				 * This packet isn't part of our video stream, so we just
				 * silently drop it.
				 */
				do {
				} while (false);
			}

		}
		/*
		 * Technically since we're exiting anyway, these will be cleaned up by
		 * the garbage collector... but because we're nice people and want to be
		 * invited places for Christmas, we're going to show how to clean up.
		 */
		if (videoCoder != null) {
			videoCoder.close();
			videoCoder = null;
		}
		if (audioCoder != null) {
			audioCoder.close();
			audioCoder = null;
		}
		if (container != null) {
			container.close();
			container = null;
		}
		closeJavaSound();

	}

	/**
	 * updates the Image of the Video and fades stuff that is painted over the video at the appropiate frame
	 * @param aImage
	 */
	public void setImage(final BufferedImage aImage) {
		numImg++;
		if(numImg == 70){
			addMouseListener(card);
			addMouseMotionListener(hover);
		}else if(numImg == 55){
			backBtn.alpha = statsBtn.alpha = 0.0f;
		}
		cImage = aImage;
		repaint();
		
		
	}

	
	/**
	 * xuggler play video helper method 
	 */
	private static long millisecondsUntilTimeToDisplay(IVideoPicture picture) {
		/**
		 * We could just display the images as quickly as we decode them, but it
		 * turns out we can decode a lot faster than you think.
		 * 
		 * So instead, the following code does a poor-man's version of trying to
		 * match up the frame-rate requested for each IVideoPicture with the
		 * system clock time on your computer.
		 * 
		 * Remember that all Xuggler IAudioSamples and IVideoPicture objects
		 * always give timestamps in Microseconds, relative to the first decoded
		 * item. If instead you used the packet timestamps, they can be in
		 * different units depending on your IContainer, and IStream and things
		 * can get hairy quickly.
		 */
		long millisecondsToSleep = 0;
		if (mFirstVideoTimestampInStream == Global.NO_PTS) {
			// This is our first time through
			mFirstVideoTimestampInStream = picture.getTimeStamp();
			// get the starting clock time so we can hold up frames
			// until the right time.
			mSystemVideoClockStartTime = System.currentTimeMillis();
			millisecondsToSleep = 0;
		} else {
			long systemClockCurrentTime = System.currentTimeMillis();
			long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime
					- mSystemVideoClockStartTime;
			// compute how long for this frame since the first frame in the
			// stream.
			// remember that IVideoPicture and IAudioSamples timestamps are
			// always in MICROSECONDS,
			// so we divide by 1000 to get milliseconds.
			long millisecondsStreamTimeSinceStartOfVideo = (picture
					.getTimeStamp() - mFirstVideoTimestampInStream) / 1000;
			final long millisecondsTolerance = 50; // and we give ourselfs 50 ms
													// of tolerance
			millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
		}
		return millisecondsToSleep;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.drawImage(shop, 0, 0, null);
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,glowBox.alpha));
		g2d.drawImage(glowBox.getImgCard().getImg(), 275, 40, null);
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
		if(!vidOver && numImg>1){
			g2d.drawImage(cImage, 0, 0, null);
		}else{
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
					screenAlpha));
			g2d.drawImage(screen,-1,1,null);
		}
		
		paintCard(statsBtn, g2d);
		if(stats){
			g2d.drawImage(legend, 560, 255, null);
			g2d.setColor(Color.WHITE);
			g2d.setFont(new Font("Verdana", Font.BOLD, 20));
			g2d.drawString("#           "+counter[0], 540, 300);
			g2d.drawString("#           "+counter[1], 540, 355);
			g2d.drawString("#           "+counter[2], 540, 393);
			g2d.drawString("#           "+counter[3], 540, 430);
			g2d.drawString("#           "+counter[4], 540, 467);
			g2d.setFont(new Font("Verdana", Font.PLAIN, 12));
			g2d.drawString("made by Carsten Schipmann (theSchippi@gmail.com) ", 455, 700);
			g2d.setFont(new Font("Verdana", Font.PLAIN, 9));
			g2d.drawString("thanks to AyllieyaRosa for some art", 515, 720);
			g2d.setFont(new Font("Verdana", Font.PLAIN, 8));
			g2d.drawString("assets from Blizzard Entertainment, this is a fan project and not to be sold", 5, 723);
		}
		
		paintCard(top, g2d);

		// topLeftCard
		paintCard(topLeft, g2d);

		// topRightCard
		paintCard(topRight, g2d);

		// botLeftCard
		paintCard(botLeft, g2d);

		// botRightCard
		paintCard(botRight, g2d);
		
		// done
		paintCard(doneBtn,g2d);
		
		paintCard(pack,g2d);
		
		paintCard(backBtn,g2d);
		
	}

	/**
	 * paints the card at its position with glow if hovored over
	 * @param card card to be painted
	 * @param g2d graphics to paint on
	 */
	private void paintCard(Card card, Graphics2D g2d) {
		
		if(card.hover){
			if(card.equals(statsBtn)){
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
				g2d.setColor(Color.WHITE);
				g2d.drawImage(statsglow, cGlow[1], card.rect.x,card.rect.y-3);
			}else if(card.equals(pack)){
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						1.0f));
				g2d.drawImage(packGlow, cGlow[4], card.rect.x-45,card.rect.y-60);
			}else if(!card.equals(doneBtn)){
				g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
						(card.clicked && card.finished)?card.alpha:1.0f));
//				g2d.drawImage(shadedImage, cGlow[card.img.getQuality()], card.rect.x-20,card.rect.y-20);
				g2d.drawImage(shadedImage, cGlow[card.img.getQuality()], card.rect.x-50,card.rect.y-50);
				g2d.drawImage(backI,card.rect.x+12,card.rect.y+38,null);
			}
		}
		
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				card.alpha));
		g2d.drawImage(card.getImgCard().getImg(), card.rect.x, card.rect.y, null);
	}

	
	/**
	 * xuggler play video helper method
	 */
	private static void openJavaSound(IStreamCoder aAudioCoder)
			throws LineUnavailableException {
		AudioFormat audioFormat = new AudioFormat(aAudioCoder.getSampleRate(),
				(int) IAudioSamples.findSampleBitDepth(aAudioCoder
						.getSampleFormat()), aAudioCoder.getChannels(), true, /*
																			 * xuggler
																			 * defaults
																			 * to
																			 * signed
																			 * 16
																			 * bit
																			 * samples
																			 */
				false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class,
				audioFormat);
		mLine = (SourceDataLine) AudioSystem.getLine(info);
		/**
		 * if that succeeded, try opening the line.
		 */
		mLine.open(audioFormat);
		/**
		 * And if that succeed, start the line.
		 */
		mLine.start();

	}

	/**
	 * xuggler play video helper method
	 */
	private static void playJavaSound(IAudioSamples aSamples) {
		/**
		 * We're just going to dump all the samples into the line.
		 */
		byte[] rawBytes = aSamples.getData()
				.getByteArray(0, aSamples.getSize());
		mLine.write(rawBytes, 0, aSamples.getSize());
	}
	/**
	 * xuggler play video helper method
	 */
	private static void closeJavaSound() {
		if (mLine != null) {
			/*
			 * Wait for the line to finish playing
			 */
			mLine.drain();
			/*
			 * Close the line.
			 */
			mLine.close();
			mLine = null;
		}
	}
}
