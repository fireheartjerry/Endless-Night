/*
* Authors: Jerry Li & Victor Jiang
* Date: June 13, 2025
* Description: This class plays background music and sound effects
*/

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {

    // Constructor for the SoundManager class.
    public SoundManager() {

    }

    // Method to play background music in a loop.
    public void playBackgroundMusic(String track) {
        try {
            // Load the audio file from the specified path.
            File audio_file = new File("./assets/music/" + track + ".wav");
            AudioInputStream audio_stream = AudioSystem.getAudioInputStream(audio_file);

            // Create a Clip object to play the audio.
            Clip clip = AudioSystem.getClip();
            //clip.open(audio_stream);

            // Set the clip to loop continuously.
            clip.loop(Clip.LOOP_CONTINUOUSLY);

            // Adjust the volume of the audio using a FloatControl.
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-15.0f); // Reduce the volume by 15 decibels.

            // Start playing the audio.
            clip.start();
        } catch (Exception e) {
            // Print an error message if an exception occurs.
            System.out.println("An error occurred while playing background music.");
        }
    }

    // Method to play a sound effect (SFX) once.
    public void playSfx(String sfx) {
        try {
            // Load the sound effect file from the specified path.
            File sound_file = new File("./assets/sound/" + sfx + ".wav");
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(sound_file);

            // Create a Clip object to play the sound effect.
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            // Adjust the volume of the sound effect using a FloatControl.
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            volumeControl.setValue(-5.0f); // Reduce the volume by 5 decibels.

            // Start playing the sound effect.
            clip.start();
        } catch (Exception e) {
            // Print an error message if an exception occurs.
            System.out.println("An error occurred while playing the bounce effect. No audio will be played.");
        }
    }

}
