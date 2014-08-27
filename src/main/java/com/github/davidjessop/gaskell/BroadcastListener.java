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

    private final String execCommand;
    private final InputPipe inputPipe;
    private int messageCount;

    @Autowired
    public BroadcastListener(AdvertisementUtil advertisementUtil, PipeService pipeService, PipeID multicastId) throws IOException, InterruptedException {
        inputPipe = pipeService.createInputPipe(advertisementUtil.getAdvertisement(multicastId, true), this);

        InputStream in = ClassLoader.getSystemResourceAsStream("sarahringtone.mp3");
        File tmpFile = File.createTempFile("sarahringtone", "mp3");
        OutputStream out = new FileOutputStream(tmpFile);
        IOUtils.copy(in, out);
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("linux")) {
            execCommand = "mpg321 " + tmpFile.getAbsolutePath();
        } else if (osName.startsWith("mac")) {
            execCommand = "afplay " + tmpFile.getAbsolutePath();
        } else {
            throw new RuntimeException("unsupported OS: " + osName);
        }

    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent event) {
        System.out.println("received message " + ++messageCount);

        try {
            Runtime.getRuntime().exec(execCommand);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
