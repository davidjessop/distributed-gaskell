package com.github.davidjessop.gaskell;

import net.jxta.discovery.DiscoveryEvent;
import net.jxta.discovery.DiscoveryListener;
import net.jxta.discovery.DiscoveryService;
import net.jxta.document.AdvertisementFactory;
import net.jxta.document.MimeMediaType;
import net.jxta.exception.PeerGroupException;
import net.jxta.id.IDFactory;
import net.jxta.platform.ModuleClassID;
import net.jxta.protocol.ModuleClassAdvertisement;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

@Component
public class PeerTracker implements DiscoveryListener {

    private final PeerIdUtil peerIdUtil;
    private final DiscoveryService discoveryService;

    private final Set<String> peers = new HashSet<>();

    @Autowired
    public PeerTracker(PeerIdUtil peerIdUtil, DiscoveryService discoveryService) throws IOException, PeerGroupException {
        this.peerIdUtil = peerIdUtil;
        this.discoveryService = discoveryService;
        publishModule();
        discoveryService.addDiscoveryListener(this);
    }

    @Scheduled(fixedDelay = 10000)
    public void checkPeers() throws InterruptedException {
        peers.clear();
        discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "STACK-OVERFLOW:HELLO", 1, null);
        Thread.sleep(5000);

        System.out.println("found " + peers.size() + " peers: " + String.join(",", peers));
    }

    public void publishModule() throws IOException {
        ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());
        mcadv.setName("STACK-OVERFLOW:HELLO");

        mcadv.setDescription(System.getProperty("user.name") + "@" + InetAddress.getLocalHost().getHostName());
        ModuleClassID mcID = IDFactory.newModuleClassID();
        mcadv.setModuleClassID(mcID);

        // Let the group know of this service "module" / collection
        discoveryService.publish(mcadv);
        discoveryService.remotePublish(mcadv);

    }

    @Override
    public void discoveryEvent(DiscoveryEvent event) {
        String peer = null;
        try {
            InputStream in = event.getResponse().getAdvertisements().nextElement().getDocument(MimeMediaType.XML_DEFAULTENCODING).getStream();
            Document doc = new Builder().build(in);
            Nodes descriptions = doc.query("//Desc");
            if (descriptions.size() > 0) {
                peer = descriptions.get(0).getValue();
            } else {
                Nodes mcids = doc.query("//MCID");
                if (mcids.size() > 0) {
                    peer = mcids.get(0).getValue();
                } else {
                    peer = "anonymous";
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ValidityException e) {
            e.printStackTrace();
        } catch (ParsingException e) {
            e.printStackTrace();
        }

        peers.add(peer);
    }

}
