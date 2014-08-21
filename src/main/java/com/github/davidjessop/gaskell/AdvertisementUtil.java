package com.github.davidjessop.gaskell;

import net.jxta.document.AdvertisementFactory;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;

import org.springframework.stereotype.Component;

@Component
public class AdvertisementUtil {

    public PipeAdvertisement getAdvertisement(PipeID id, boolean multicast) {
        PipeAdvertisement adv = (PipeAdvertisement ) AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(id);
        if (multicast) {
            adv.setType(PipeService.PropagateType);
        } else {
            adv.setType(PipeService.UnicastType);
        }
        adv.setName("This however");
        adv.setDescription("does not really matter");
        return adv;
    }
}
