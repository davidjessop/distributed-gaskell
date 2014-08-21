package com.github.davidjessop.gaskell;

import net.jxta.document.AdvertisementFactory;
import net.jxta.endpoint.Message;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.OutputPipe;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

@Controller
public class RingtoneTriggerController {

    private final PipeService pipeService;
    private final PipeID multiCastId;

    @Autowired
    public RingtoneTriggerController(PipeService pipeService, PipeID multiCastId) {
        this.pipeService = pipeService;
        this.multiCastId = multiCastId;
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
        out.send(new Message());

        return "Woo-ooo-ooo-ooo-ooo-ooo-ooo\n";
    }
}
