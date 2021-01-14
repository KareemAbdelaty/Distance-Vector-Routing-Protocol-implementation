/*
 * Complete this class.
 * Student Name: Kareem Abdelaty
 * Student ID No.: 30075331
 * resource : Computer Networking: Top-Down Approach chapter 5.2.2
 */
import javax.swing.*;        

public class RouterNode {
    private int myID;
    private GuiTextArea myGUI;
    private RouterSimulator sim;
	private int INFINITY =  RouterSimulator.INFINITY;
    private int[] costs = new int[RouterSimulator.NUM_NODES];//holds the costs to all nodes
    private int[] path = new int[RouterSimulator.NUM_NODES];//holds the node to forward the message to for a destination of index
    private int[][] table = new int[RouterSimulator.NUM_NODES][RouterSimulator.NUM_NODES];//holds the distance routing table for the full network
    private boolean poisoned = false;//Reversed Poison on?
//	private boolean linkchange = false; // LinkChange on?

  //--------------------------------------------------
  public RouterNode(int ID, RouterSimulator sim, int[] costs) {
	    myID = ID;
        this.sim = sim;
        myGUI =new GuiTextArea("  Output window for Router #"+ ID + "  ");
        System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
//		if(linkchange){
//		RouterSimulator.LINKCHANGES = true;
//	}
		/*
		Intiallise costs for other nodes in the network into a 2d array for easy access
		Set the cost for all my neighbors. all other costs are infinity	
		*/
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
          path[i] = i; //
          for(int x = 0; x < RouterSimulator.NUM_NODES; x++) {
            if(i == myID)
                table[i][x] = costs[x];
            else
                table[i][x] = INFINITY;
          }
        }
		//send cost table to neighbours
        upDateNeighbours();

  }

  //--------------------------------------------------
  		/*
		*if you recieve an update from a router, see if you need to update your distance tables. if you update your table
		* UpDate all neighbors
		*/
  
  public void recvUpdate(RouterPacket pkt) {
	    System.arraycopy(pkt.mincost, 0, table[pkt.sourceid], 0, RouterSimulator.NUM_NODES);
        if(recalc()) {
            upDateNeighbours();
        }
	  

  }

  //--------------------------------------------------
  //Send a update packet
  private void sendUpdate(RouterPacket pkt) {
    sim.toLayer2(pkt);

  }
  

  //--------------------------------------------------
  //format reults and print table;
  public void printDistanceTable() {
	  myGUI.println("Current table for " + myID +
			"  at time " + sim.getClocktime());			

		String s = "dest |";
		for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
			s += "  " + i;           
        }
		//similar indesign to the one in the book
		myGUI.println(s);
		String str = "Cost |";
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
			if(costs[i] != 999){
				str += "  " + costs[i];
			}else{
				str += "  " + "infinity";
			}   
        }
		myGUI.println(str);
		String str2 = "route |";
		for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
			str2 += "  " + path[i];
        }
		myGUI.println(str2);
 
            
        
  }

  //--------------------------------------------------
  //
  public void updateLinkCost(int dest, int newcost) {
	    table[myID][dest] = newcost;
        if(recalc()) {
            upDateNeighbours();
        }
  }
	/**
	send your current costs to all neighbors and if Reverese poison is on apply ReversePoison
	*/
    private void upDateNeighbours() {
        for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
            if(i != myID && costs[i] != INFINITY) {
				int newCost[] = new int[RouterSimulator.NUM_NODES];
				System.arraycopy(costs, 0, newCost, 0, RouterSimulator.NUM_NODES);
				if(poisoned) {
					for(int j = 0; j < RouterSimulator.NUM_NODES; j++) {
						if(path[j] == i) {
							newCost[j] = INFINITY;
						}
					}
				}
				RouterPacket pkt = new RouterPacket(myID, i, newCost);
				sendUpdate(pkt);
            }
        }
    }
  //implment Bellman-Ford equation
  private boolean recalc() {  
	boolean needUpdate = false;
	for(int i = 0; i < RouterSimulator.NUM_NODES; i++) {
		for(int x = 0; x < RouterSimulator.NUM_NODES; x++) {
			int c;
			if(i != myID){ 
				c = table[i][x] + table[myID][i]; //c(x, v) + dv( y)
			}else{
				c = table[i][x]; // dx( y) 
			}
			//if min is lower than current cost take min and update
			if(c < costs[x]) {
				costs[x] = c; 
				path[x] = i;
				needUpdate = true;
			}
		}
	}

	return needUpdate; 
}

}
