
import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {

    public SoundManager() {
        
    }

    public void playBackgroundMusic(String track) {
        try {
            File audio_file = new File("./assets/music/" + track + ".wav");
            AudioInputStream audio_stream = AudioSystem.getAudioInputStream(audio_file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio_stream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-15.0f); // Reduce volume.
            clip.start();
        } catch (Exception e) {
            System.out.println("An error occurred while playing background music.");
        }
    }

    public void playSfx(String sfx) {
        try {
            File sound_file = new File("./assets/sound/" + sfx + ".wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(sound_file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-5.0f);
            clip.start();
        } catch (Exception e) {
            System.out.println("An error occurred while playing the bounce effect. No audio will be played.");
        }
    }

}
