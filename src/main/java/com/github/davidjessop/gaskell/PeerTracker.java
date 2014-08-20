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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
public class PeerTracker implements DiscoveryListener, PipeMsgListener {

    private static final String subgroup_name = "Make sure this is spelled the same everywhere";
    private static final String subgroup_desc = "...";
    private static final PeerGroupID subgroup_id = IDFactory.newPeerGroupID(PeerGroupID.defaultNetPeerGroupID, subgroup_name.getBytes());

    private static final String unicast_name = "This must be spelled the same too";
    private static final String multicast_name = "Or else you will get the wrong PipeID";
    private static final String service_name = "And dont forget it like i did a million times";

    private final PeerIdUtil peerIdUtil;
    private final NetworkManager manager;

    private PeerGroup subgroup;
    private PipeService pipe_service;
    private PipeID unicast_id;
    private PipeID multicast_id;
    private PipeID service_id;
    private DiscoveryService discoveryService;
    private ModuleSpecAdvertisement mdadv;

    private int messageCount;

    private final Set<Peer> peers = new HashSet<>();

    @Autowired
    public PeerTracker(PeerIdUtil peerIdUtil, NetworkManager manager) throws IOException, PeerGroupException {
        this.peerIdUtil = peerIdUtil;
        this.manager = manager;
        start();
        spawnDiscoveryThread();
    }

    private void spawnDiscoveryThread() {
        new Thread("fetch advertisements thread") {
            public void run() {
                while(true) {
                    discoveryService.getRemoteAdvertisements(null, DiscoveryService.ADV, "Name", "STACK-OVERFLOW:HELLO", 1, null);
                    try {
                        sleep(10000);
                    } catch (InterruptedException e) {
                        //squish
                    }
                }
            }
        }.start();
    }

    public void start() throws PeerGroupException, IOException {
        // Launch the missiles, if you have logging on and see no exceptions
        // after this is ran, then you probably have at least the jars setup correctly.
        PeerGroup net_group = manager.startNetwork();

        // Connect to our subgroup (all groups are subgroups of Netgroup)
        // If the group does not exist, it will be automatically created
        // Note this is suggested deprecated, not sure what the better way is
        ModuleImplAdvertisement mAdv = null;
        try {
            mAdv = net_group.getAllPurposePeerGroupImplAdvertisement();
        } catch (Exception ex) {
            System.err.println(ex.toString());
        }
        subgroup = net_group.newGroup(subgroup_id, mAdv, subgroup_name, subgroup_desc);

        // A simple check to see if connecting to the group worked
        if (Module.START_OK != subgroup.startApp(new String[0]))
            System.err.println("Cannot start child peergroup");

        // We will spice things up to a more interesting level by sending unicast and multicast messages
        // In order to be able to do that we will create to listeners that will listen for
        // unicast and multicast advertisements respectively. All messages will be handled by Hello in the
        // pipeMsgEvent method.

        unicast_id = IDFactory.newPipeID(subgroup.getPeerGroupID(), unicast_name.getBytes());
        multicast_id = IDFactory.newPipeID(subgroup.getPeerGroupID(), multicast_name.getBytes());

        pipe_service = subgroup.getPipeService();
        pipe_service.createInputPipe(get_advertisement(unicast_id, false), this);
        pipe_service.createInputPipe(get_advertisement(multicast_id, true), this);

        // In order to for other peers to find this one (and say hello) we will
        // advertise a Hello Service.
        discoveryService = subgroup.getDiscoveryService();
        discoveryService.addDiscoveryListener(this);

        ModuleClassAdvertisement mcadv = (ModuleClassAdvertisement) AdvertisementFactory.newAdvertisement(ModuleClassAdvertisement.getAdvertisementType());

        mcadv.setName("STACK-OVERFLOW:HELLO");
        mcadv.setDescription("Tutorial example to use JXTA module advertisement Framework");

        ModuleClassID mcID = IDFactory.newModuleClassID();

        mcadv.setModuleClassID(mcID);

        // Let the group know of this service "module" / collection
        discoveryService.publish(mcadv);
        discoveryService.remotePublish(mcadv);

        mdadv = (ModuleSpecAdvertisement) AdvertisementFactory.newAdvertisement(ModuleSpecAdvertisement.getAdvertisementType());
        mdadv.setName("STACK-OVERFLOW:HELLO");
        mdadv.setVersion("Version 1.0");
        mdadv.setCreator("sun.com");
        mdadv.setModuleSpecID(IDFactory.newModuleSpecID(mcID));
        mdadv.setSpecURI("http://www.jxta.org/Ex1");

        service_id = IDFactory.newPipeID(subgroup.getPeerGroupID(), service_name.getBytes());
        PipeAdvertisement pipeadv = get_advertisement(service_id, false);
        mdadv.setPipeAdvertisement(pipeadv);

        // Let the group know of the service
        discoveryService.publish(mdadv);
        discoveryService.remotePublish(mdadv);

        // Start listening for discovery events, received by the discoveryEvent method
        pipe_service.createInputPipe(pipeadv, this);
    }


    @Override
    public void discoveryEvent(DiscoveryEvent event) {
        Peer peer = peerIdUtil.fromSource(event.getSource());
        peers.add(peer);
        System.out.println("discovered peer: " + peer);
    }

    private static PipeAdvertisement get_advertisement(PipeID id, boolean is_multicast) {
        PipeAdvertisement adv = (PipeAdvertisement )AdvertisementFactory.newAdvertisement(PipeAdvertisement.getAdvertisementType());
        adv.setPipeID(id);
        if (is_multicast) {
            adv.setType(PipeService.PropagateType);
        } else {
            adv.setType(PipeService.UnicastType);
        }
        adv.setName("This however");
        adv.setDescription("does not really matter");
        return adv;
    }

    @Override
    public void pipeMsgEvent(PipeMsgEvent event) {
        System.out.println("received message " + ++messageCount);
    }

    public PipeID getMulticast_id() {
        return multicast_id;
    }

    public PipeService getPipe_service() {
        return pipe_service;
    }
}
