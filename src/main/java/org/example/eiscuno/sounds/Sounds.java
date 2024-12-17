package org.example.eiscuno.sounds;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class Sounds {
    /**
     * Clip object that holds the audio data for playing, stopping, and looping the sound.
     */
    private Clip clip;

    /**
     * Loads a sound file from the specified path.
     * The sound file is prepared for playback but not yet played.
     *
     * @param path The file path of the sound to be loaded.
     */
    public void loadSound(String path) {
        try {
            File soundFile = new File(path);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            clip = AudioSystem.getClip();
            clip.open(audioInputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Plays the loaded sound from the beginning.
     * If no sound has been loaded, this method does nothing.
     */
    public void playSound() {
        if (clip != null) {
            clip.setFramePosition(0); // Reset to the beginning of the sound
            clip.start();
        }
    }

    /**
     * Stops the currently playing sound.
     * If no sound is playing, this method does nothing.
     */
    public void stopSound() {
        if (clip != null) {
            clip.stop();
        }
    }

    /**
     * Loops the loaded sound continuously.
     * If no sound has been loaded, this method does nothing.
     */
    public void loopSound() {
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void lowerVolume(double v){
        if (clip != null) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float currentVolume = volumeControl.getValue();
            volumeControl.setValue(currentVolume - 15.0f);
        }
    }

    public void upperVolume(){
        if (clip != null) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float currentVolume = volumeControl.getValue();
            volumeControl.setValue(currentVolume + 6.0f);
        }
    }
}
