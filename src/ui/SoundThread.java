package ui;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * plays the sound when run in a loop if specified
 */
public class SoundThread extends Thread{
	private Clip sound;
	private URL url;
	boolean loop = false;

	public SoundThread(String s){
		url =Randomizer.class.getClassLoader()
		.getResource(s);
		try {
			sound = (Clip) AudioSystem.getLine(new Line.Info(
					Clip.class));
			sound.open(AudioSystem.getAudioInputStream(url));
//			FloatControl volume = (FloatControl) sound
//			.getControl(FloatControl.Type.MASTER_GAIN);
//			volume.setValue(3);
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}
	public SoundThread(String s, boolean loo){
		this(s);
		loop=loo;
	}
	
	public void run() {
			sound.setFramePosition(0);
			sound.start();
			sound.addLineListener(new LineListener(){
				@Override
				public void update(LineEvent arg0) {
					if(arg0.getType().equals(LineEvent.Type.STOP)){
						if(loop && !isInterrupted()){
							sound.setFramePosition(0);
							sound.start();
						}else{
							sound.drain();
							interrupt();
						}
						
					}
				}
				
			});
	}
	@Override
	public void interrupt(){
		super.interrupt();
		sound.stop();
		sound.close();
	}
	
	public Clip getSound(){
		return sound;
	}
	
}