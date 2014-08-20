package com.github.davidjessop.gaskell;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Component
public class RingtoneTriggerController {

    private final PipeService pipeService;
    private final PeerTracker peerTracker;

    @Autowired
    public RingtoneTriggerController(PeerTracker peerTracker) {
        this.peerTracker = peerTracker;
        this.pipeService = peerTracker.getPipe_service();
    }

    @RequestMapping("/trigger")
    @Scheduled(fixedDelay = 5000)
    public void trigger() throws IOException {
        PipeAdvertisement adv = (PipeAdvertisement ) AdvertisementFactory.
            newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(peerTracker.getMulticast_id());
        adv.setType(PipeService.PropagateType);
        adv.setName("This however");
        adv.setDescription("does not really matter");

        OutputPipe out = pipeService.createOutputPipe(adv, 0);
        out.send(new Message());

//        return "woo-ooo-ooo-ooo-ooo-ooo-ooo";
    }
}
