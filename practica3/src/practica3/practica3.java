/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


/**
 *
 * @author marcos
 */
public class practica3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Agente a,b,c,d;
        AgentsConnection.connect("isg2.ugr.es", 6000, "Denebola", "Orion", "Pitol", false);
        try {
            b = new Agente((new AgentID("vehiculo112")),"map10", false);
            b.start();
            c = new Agente((new AgentID("vehiculo113")),"map10", false);
            c.start();
            d = new Agente((new AgentID("vehiculo114")),"map10", false);
            d.start();
            a = new Agente((new AgentID("vehiculo111")),"map10", true);
            a.start();
            
        } catch (Exception ex) {
            System.out.println("Error al crear el agente");
        }
    }
    
}
//FALTA EL 6 7 8 9 10