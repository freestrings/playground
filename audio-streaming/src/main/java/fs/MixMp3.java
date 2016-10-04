package fs;

import javazoom.spi.mpeg.sampled.file.MpegAudioFileReader;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MixMp3 implements Runnable {

    public static void main(String... args) throws Exception {
        new MixMp3();
    }

    private final SourceDataLine auline;

    private AudioInputStream audioInputStream;

    public MixMp3() throws Exception {
        MpegAudioFileReader mpReader = new MpegAudioFileReader();
        AudioInputStream ais1 = mpReader.getAudioInputStream(new File("./src/main/resources/04 - Over The Mountain.mp3"));
        AudioInputStream ais2 = mpReader.getAudioInputStream(new File("./src/main/resources/05 - Bark At The Moon.mp3"));
        List<Object> list = Arrays.asList(ais1, ais2);

        this.audioInputStream = new MixingAudioInputStream(getAudioFormat(), list);
        AudioFormat format = this.audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        if (AudioSystem.isLineSupported(info) == false) {
            throw new Exception("Soundcard doesn't support the format of the audio-file");
        }
        auline = (SourceDataLine) AudioSystem.getLine(info);
        auline.open(format);
        auline.start();

        new Thread(this).start();
    }

    static AudioFormat getAudioFormat() {
        float sampleRate = 8000.0F;
        //8000,11025,16000,22050,44100
        int sampleSizeInBits = 16;
        //8,16
        int channels = 1;
        //1,2
        boolean signed = true;
        //true,false
        boolean bigEndian = false;
        //true,false
        return new AudioFormat(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    @Override
    public void run() {
        int nBytesRead = 0;
        byte[] abData = new byte[128 * 1024];

        try {
            do {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0)
                    auline.write(abData, 0, nBytesRead);
            }
            while (nBytesRead != -1);
        } catch (IOException ie) {
            ie.printStackTrace();
        }

        auline.drain();
        auline.close();
    }
}
