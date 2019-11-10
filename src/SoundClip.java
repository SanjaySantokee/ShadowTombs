import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import java.io.File;

public class SoundClip {

    private boolean playCompleted;
    private boolean opened;
    private Clip audioClip;
    private FloatControl gainControl;

    public void open(String soundFileName) {
        try {
            File file = new File(soundFileName);
            AudioInputStream sound = AudioSystem.getAudioInputStream(file);
            audioClip = AudioSystem.getClip();
            audioClip.open(sound);
        } catch (Exception e) {
            System.out.println("Error loading " + soundFileName + " file");
        }
    }

    public void play(int i) {
        audioClip.setFramePosition(i);
        audioClip.start();
    }

    public void loop() {
        audioClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop() {
        audioClip.stop();
        audioClip.close();
    }

    public void close() {
        audioClip.close();
    }


    public void adjustVolume(float volume) {
        gainControl = (FloatControl) audioClip.getControl(FloatControl.Type.MASTER_GAIN);
        float range = gainControl.getMaximum() - gainControl.getMinimum();
        float gain = (range * volume) + gainControl.getMinimum();
        gainControl.setValue(gain);
    }
}
