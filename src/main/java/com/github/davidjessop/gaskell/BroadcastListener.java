package com.github.davidjessop.gaskell;

import net.jxta.endpoint.MessageElement;
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

@Component
public class BroadcastListener implements PipeMsgListener {

    private static final int RINGTONE_DURATION = 32000;

    private final String execCommand;
    private final InputPipe inputPipe;

    private long lockTimeout;

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
        String peerName = "anonymous";
        MessageElement messageElement = event.getMessage().getMessageElement(RingtoneTriggerController.PEER_NAME);
        if (messageElement != null) {
            try {
                peerName = new String(messageElement.getBytes(false), RingtoneTriggerController.ENCODING);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime > lockTimeout) {
            System.out.println("received trigger from " + peerName);
            lockTimeout = currentTime + RINGTONE_DURATION;
            try {
                Runtime.getRuntime().exec(execCommand);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            System.out.println("ignored trigger from " + peerName);
        }
    }

}
