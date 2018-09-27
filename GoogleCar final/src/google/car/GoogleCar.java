/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package google.car;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author marcos
 */
public class GoogleCar {
//HOLI
    public static void main(String[] args) {
        Agente a;
        AgentsConnection.connect("isg2.ugr.es", 6000, "Denebola", "Orion", "Pitol", false);
        try {
            a = new Agente(new AgentID("car2"));
            a.start();
        } catch (Exception ex) {
            System.out.println("Error al crear el agente: "+ex.toString());
        }
    }
    
}
