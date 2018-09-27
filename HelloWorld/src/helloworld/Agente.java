/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package helloworld;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author marcos
 */
public class Agente extends SingleAgent {
    
    String contenidoMensaje;
    
    public Agente(AgentID aid) throws Exception{
        super(aid);
    }
    
    /*@Override
    public void init(){
        
    }*/
    @Override
    
    //Mientras se ejecuta el agente
    public void execute(){
        //while (!Salir)
        System.out.println("\nHola Mundo soy un agente");
        this.Ejercicio2();
    }
    
    public void Saluda(String mensaje, String receptor){
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID(receptor));
        outbox.setContent(mensaje);
        this.send(outbox);
        System.out.println("\nEnvio mensaje: "+mensaje);
    }
    
    public void Ejercicio1(){
        this.Saluda("Hola","Goku");
    }
    
    public void Ejercicio2(){
        this.Saluda("Hola","Songoanda");
        ACLMessage mensaje = new ACLMessage();
        try {
            mensaje=this.receiveACLMessage();
            System.out.println("\nRecibo mensaje: "+mensaje.getContent());
            contenidoMensaje=mensaje.getContent();
            String aux = "";
            for(int i=contenidoMensaje.length()-1; i>=0; i--){
                aux=aux+contenidoMensaje.charAt(i);
            }
            System.out.println("\n Envio mensaje: "+mensaje.getContent());
            this.Saluda(aux,"Songoanda");
            
        } catch (InterruptedException ex) {
            System.out.println("\nNo he recibido el mensaje");
        }
        
        
    }
    
    // Cuando terminar de ejecutar execute muere el agente y se ejecuta finalize
    // public void finalize();
}
