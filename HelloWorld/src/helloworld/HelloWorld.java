/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package helloworld;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;


/**
 *
 * @author marcos
 */
public class HelloWorld {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        Agente a;
        AgentsConnection.connect("isg2.ugr.es", 6000, "test", "guest", "guest", false);
        try {
            a = new Agente(new AgentID("25342389"));
            a.start();
        } catch (Exception ex) {
            System.out.println("Error al crear el agente");
        }
    }
    
}
