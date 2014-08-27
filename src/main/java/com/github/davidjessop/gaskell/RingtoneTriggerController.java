package com.github.davidjessop.gaskell;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.ByteArrayMessageElement;
import net.jxta.endpoint.Message;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class RingtoneTriggerController {

    public static final String PEER_NAME = "peerName";
    public static final String ENCODING = "ISO-8859-1";

    private final PipeService pipeService;
    private final PipeID multiCastId;
    private final String peerName;

    @Autowired
    public RingtoneTriggerController(PipeService pipeService, PipeID multiCastId, @Qualifier("peerName") String peerName) {
        this.pipeService = pipeService;
        this.multiCastId = multiCastId;
        this.peerName = peerName;
    }

    @RequestMapping("/trigger")
    public @ResponseBody String trigger() throws IOException {
        PipeAdvertisement adv = (PipeAdvertisement ) AdvertisementFactory.
            newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(multiCastId);
        adv.setType(PipeService.PropagateType);
        adv.setName("This however");
        adv.setDescription("does not really matter");

        OutputPipe out = pipeService.createOutputPipe(adv, 0);
        Message message = new Message();
        message.addMessageElement(new ByteArrayMessageElement(PEER_NAME, null, peerName.getBytes(ENCODING), null));
        out.send(message);

        return "Woo-ooo-ooo-ooo-ooo-ooo-ooo\n";
    }
}
