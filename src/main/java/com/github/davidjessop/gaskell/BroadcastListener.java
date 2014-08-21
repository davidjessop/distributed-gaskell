package com.github.davidjessop.gaskell;

import net.jxta.pipe.InputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.CountDownLatch;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

@Component
public class BroadcastListener implements PipeMsgListener {

    private int messageCount;
    private InputPipe inputPipe;

    @Autowired
    public BroadcastListener(AdvertisementUtil advertisementUtil, PipeService pipeService, PipeID multicastId) throws IOException, InterruptedException {
        inputPipe = pipeService.createInputPipe(advertisementUtil.getAdvertisement(multicastId, true), this);

        // initialise JFX toolkit
        final CountDownLatch latch = new CountDownLatch(1);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new JFXPanel(); // initializes JavaFX environment
                latch.countDown();
            }
        });
        latch.await();

        InputStream in = ClassLoader.getSystemResourceAsStream("sarahringtone.mp3");
        OutputStream out = new FileOutputStream(new File("/tmp/sarahringtone.mp3"));
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent event) {
        System.out.println("received message " + ++messageCount);

        String bip = "file:/tmp/sarahringtone.mp3";
        Media hit = new Media(bip);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }

}
