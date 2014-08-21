package com.github.davidjessop.gaskell;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.peergroup.PeerGroupID;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeMsgEvent;
import net.jxta.pipe.PipeMsgListener;
import net.jxta.pipe.PipeService;
import net.jxta.platform.Module;
import net.jxta.platform.ModuleClassID;
import net.jxta.platform.NetworkManager;
import net.jxta.protocol.ModuleClassAdvertisement;
import net.jxta.protocol.ModuleImplAdvertisement;
import net.jxta.protocol.ModuleSpecAdvertisement;
import net.jxta.protocol.PipeAdvertisement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class PeerTracker implements DiscoveryListener {

    private final PeerIdUtil peerIdUtil;
    private final DiscoveryService discoveryService;

    private final Set<Peer> peers = new HashSet<>();

    @Autowired
    public PeerTracker(PeerIdUtil peerIdUtil, DiscoveryService discoveryService) throws IOException, PeerGroupException {
        this.peerIdUtil = peerIdUtil;
        this.discoveryService = discoveryService;
        publishModule();
        discoveryService.addDiscoveryListener(this);
    }

    @Scheduled(fixedDelay = 10000)
    public void checkPeers() {
        discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "STACK-OVERFLOW:HELLO", 1, null);
    }

    public void publishModule() throws IOException {
        ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
        mcadv.setName("STACK-OVERFLOW:HELLO");
        mcadv.setDescription("Tutorial example to use JXTA module advertisement Framework");
        ModuleClassID mcID = IDFactory.newModuleClassID();
        mcadv.setModuleClassID(mcID);

        // Let the group know of this service "module" / collection
        discoveryService.publish(mcadv);
        discoveryService.remotePublish(mcadv);

    }

    @Override
    public void discoveryEvent(DiscoveryEvent event) {
        Peer peer = peerIdUtil.fromSource(event.getSource());
        peers.add(peer);
        System.out.println("discovered peer: " + peer);
    }

}
