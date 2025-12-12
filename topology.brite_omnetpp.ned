package inet.examples.ethernet.cloudBio;

import inet.examples.ethernet.lans.LargeLAN;
import inet.nodes.inet.StandardHostWithDLThruputMeter;
import inet.nodes.ethernet.EtherSwitch;
import inet.linklayer.ethernet.EtherBus;
import inet.networklayer.autorouting.FlatNetworkConfigurator;
import inet.nodes.inet.Router;
import inet.nodes.inet.StandardHost;
import ned.DatarateChannel;


@display("bgb=1022,590");
//
// OMNeT++ network description -- exported from BRITE topology
// Generator Model Used:
//   Model (7 - Imported From File): BRITE topology.brite 0 0.0 0.0
//    Model 3.0 ASWaxman 10.0 1000.0 100.0 1.0 2.0 0.15 0.2 1.0 1.0 10.0 1024.0
//


//
// Prototype for modules that can be used as node in a BRITENetwork
//

//
// Topology exported from BRITE. Actual module type to be used as nodes
// in the network should be supplied in the 'nodetype' parameter.
//
network realcloud {
  submodules:
    node0: StandardHost {
      @display("p=212,470;i=device/server_l");
 } 
    node1: StandardHost {
      @display("p=33,715;i=device/server_l");
 } 
    node2: StandardHost {
      @display("p=50,924;i=device/server_l");
 } 
    node3: StandardHost {
      @display("p=687,392;i=device/server_l");
 } 
    node4: StandardHost {
      @display("p=672,869;i=device/server_l");
 } 
    node5: StandardHost {
      @display("p=821,151;i=device/server_l");
 } 
    node6: StandardHost {
      @display("p=550,413;i=device/server_l");
 } 
    node7: StandardHost {
      @display("p=795,138;i=device/server_l");
 } 
    node8: StandardHost {
      @display("p=712,528;i=device/server_l");
 } 
    node9: StandardHost {
      @display("p=494,209;i=device/server_l");
 } 

configurator: FlatNetworkConfigurator {}

  connections:
    node2.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node1.pppg++;
    node2.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node0.pppg++;
    node3.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node0.pppg++;
    node3.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node1.pppg++;
    node4.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node2.pppg++;
    node4.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node3.pppg++;
    node5.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node4.pppg++;
    node5.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node3.pppg++;
    node6.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node3.pppg++;
    node6.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node4.pppg++;
    node7.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node3.pppg++;
    node7.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node5.pppg++;
    node8.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node3.pppg++;
    node8.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node7.pppg++;
    node9.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node6.pppg++;
    node9.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node5.pppg++;
    node0.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node4.pppg++;
    node0.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node6.pppg++;
    node1.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node0.pppg++;
    node1.pppg++ <--> { delay=200.0us; datarate=10.0Mbps; } <--> node4.pppg++;
}

