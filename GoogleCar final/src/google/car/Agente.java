/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package google.car;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.eclipsesource.json.JsonObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;


/**
 *
 * @author Marcos
 */
public class Agente extends SingleAgent {
    
    private String key;
    private int [] radar;
    private float[] scanner;
    private float[] distancias_posibles;
    private int mapa;
    private int sensores;
    private int bateria;
    private boolean conectado;
    private boolean objetivoEncontrado;
    private int [][] memoria;
    private int coord_x;
    private int coord_y;
    private boolean [] posible;
    private int numeroPasos;
    private int noSePuede;

    
 /**
 *
 * @author Marcos
 */
    public Agente(AgentID aid) throws Exception{
        super(aid);
        key="";
        radar = new int [25];
        scanner = new float [25];
        distancias_posibles = new float [10];
        conectado=false;
        objetivoEncontrado=false;
        memoria=new int [500][500];
        for(int i=0; i<500; i++){
            for(int x=0; x<500; x++){
                memoria[i][x]=1;
            }
        }
        coord_x=0;
        coord_y=0;
       
        posible = new boolean [10]; // se usan 8 elementos porque usamos el teclado numérico para las coordenadas
        for (int i=0; i<posible.length; i++){
            posible[i]=true;
            
        }
        numeroPasos=0;
        noSePuede=0;
    }
    
    /*@Override
    public void init(){
        
    }*/
    @Override
    
 /**
 *
 * @author Marcos
 */
    //Mientras se ejecuta el agente
    public void execute(){
        try {
            this.Login(11);
            this.repostar();
            this.estrategia();
        } catch (InterruptedException | FileNotFoundException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /*
        Se realiza la sesión en un mapa con los distintos sensores acoplados
        @Param mundo, indica el número del mundo que nos logeamos
    */
    /**
    *
    * @author Marcos
    */
    private void Login(int mundo) throws InterruptedException, FileNotFoundException{
        mapa=mundo;
        sensores=3;
        JsonObject objeto= new JsonObject();
        objeto.add("command", "login");
        objeto.add("world", "map"+mundo);
        objeto.add("radar", "car2");
        objeto.add("scanner", "car2");
        objeto.add("gps", "car2");
        this.enviarMensaje(objeto);
        this.actualizarSensores(0);
    }
    
    /*
        Finalizar sesión en el servidor, para poder obtener la traza del mapa que hemos
    conseguido recorrer.
    */
 /**
 *
 * @author Marcos
 */
    private void finalizarSesion() throws InterruptedException, FileNotFoundException{
        JsonObject objeto= new JsonObject();
        objeto.add("command","logout");
        objeto.add("key", key);
        this.enviarMensaje(objeto);
        System.out.println("Finalizo sesión \n");
        this.actualizarSensores(1);
    }
    
    /*
        Enviar un mensaje con el movimiento o accion al servidor (moverse, repostar...)
    */
    
 /**
 *
 * @author Marcos
 */
    private void enviarMensaje(JsonObject mensaje){
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Denebola"));
        outbox.setContent(mensaje.toString());
        this.send(outbox);
    }
   
    /*
        Repostar el vehiculo
    */
 /**
 *
 * @author Marcos
 */
    private void repostar() throws InterruptedException, FileNotFoundException{
        bateria=100;
        JsonObject objeto= new JsonObject();
        objeto.add("command","refuel");
        objeto.add("key", key);
        System.out.println("Recargo batería \n");
        this.enviarMensaje(objeto);
        this.actualizarSensores(0);
    }
    
    /*
        Cada vez que realizamos una acción, el agente recibe un mensaje por cada sensor que tengamos
    equipado nuestro agente, por ello sensores+1, significa que vamos a recibir un mensaje por cada


sensor +1 de respuesta (si ha sido "OK", "CRASHED"...)
    En el caso de aumento se utiliza sólo para finalizar sension, ya que recibimos un mensaje por cada 
    senson, el mensaje de respuesto, y un nuevo mensaje de traza, por ello se hace el aumento.
    */
    
 /**
 *
 * @author Marcos
 * @author Estrella
 */
    private void actualizarSensores(int aumento) throws InterruptedException, FileNotFoundException{
        ACLMessage mensaje= new ACLMessage();
        for(int i=0; i<sensores+1+aumento; i++){
            mensaje= this.receiveACLMessage();
            if(mensaje.getContent().contains("radar")){
                JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                JsonArray ja=injson.get("radar").asArray();
                for(int x=0; x<radar.length; x++){
                    radar[x]=(int) ja.get(x).asInt();
                }
            }
            if(mensaje.getContent().contains("scanner")){
                JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                JsonArray ja=injson.get("scanner").asArray();
                for(int x=0; x<scanner.length; x++){
                    scanner[x]=(float) ja.get(x).asFloat();
                }
            }
            if(mensaje.getContent().contains("gps")){
                JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                coord_x=injson.get("gps").asObject().get("x").asInt();
                coord_y=injson.get("gps").asObject().get("y").asInt();
                memoria[coord_x][coord_y]++;
            }
            if(mensaje.getContent().contains("result")){
                if(!conectado){
                    JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                    key = injson.get("result").asString();
                    conectado=true;
                    System.out.println("Me logeo en el mapa"+mapa+" correctamente y guardo la clave\n");
                }
                JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                String aux=injson.get("result").asString();
                if("CRASHED".equals(aux)){
                    System.out.println(aux);
                    this.finalizarSesion();
                }
                
            }
            if(mensaje.getContent().contains("trace")){
                JsonObject injson = Json.parse(mensaje.getContent()).asObject();
                JsonArray ja=injson.get("trace").asArray();
                try{
                    System.out.println("Recibiendo traza");
                    byte data[]=new byte[ja.size()];
                    for(int x=0; x<data.length; x++){
                        data[x]=(byte) ja.get(x).asInt();
                }
                try (FileOutputStream fos = new FileOutputStream("mitraza"+mapa+".png")) {
                        fos.write(data);
                }
                System.out.println("Traza Guardada");
                } 
                catch (IOException ex){
                    System.err.println("Error procesando traza");
                }
            }
        }
    }
    
    /*
        Realiza la orden de hacer un movimiento en una dirección concreta
    */
 /**
 * @author Marcos
 * @author David
 */
    private void mover(int movimiento) throws InterruptedException, FileNotFoundException{
        JsonObject mensaje= new JsonObject();
        switch(movimiento){
            case 8: mensaje.add("command","moveN"); 
                    System.out.println("Me muevo al Norte");break;
            case 9: mensaje.add("command", "moveNE");
                    System.out.println("Me muevo al Noreste");break;
            case 6: mensaje.add("command", "moveE"); 
                    System.out.println("Me muevo al Este");break;
            case 3: mensaje.add("command", "moveSE");
                    System.out.println("Me muevo al Sureste");break;
            case 2: mensaje.add("command", "moveS");
                    System.out.println("Me muevo al Sur");break;
            case 1: mensaje.add("command", "moveSW");
                    System.out.println("Me muevo al Suroeste");break;
            case 4: mensaje.add("command", "moveW");
                    System.out.println("Me muevo al Oeste");break;
            case 7: mensaje.add("command", "moveNW");
                    System.out.println("Me muevo al Noroeste");break;
        }
        mensaje.add("key", key);
        this.enviarMensaje(mensaje);
        this.actualizarSensores(0);
        bateria--;
        numeroPasos++;
    }
    
    /*
        Dependiendo donde se encuentre la posición de la distancia mas pequeña
    se decide que movimiento hacer.
    */
 /**
 * @author Marcos
 * @author Estrella
 * @author Alejandro
 * @author Juanma
 * @author David
 */
    private int decide(){
        float aux=1000000;
        float aux2=1000000;
        int retorno=0;
        int menor=99999;
        int [] pasadas = new int [10];
        
        for(int i=0; i<10; i++)
        {
            pasadas[i]=-1;
        }
        int b=0;
        for(int i=coord_y+1; i>=coord_y-1; i--){
            for(int j=coord_x-1; j<=coord_x+1; j++ ){
                b++;
                if(i>=0 && j>=0){
                    if(memoria[j][i]<menor && posible[b]){
                        menor=memoria[j][i];
                    }
                }
                
            }
        }
         
        int a=0;
        for(int i=coord_y+1; i>=coord_y-1; i--){
            for(int j=coord_x-1; j<=coord_x+1; j++ ){
                a++;
                if(i>=0 && j>=0){     
                    if(memoria[j][i]==menor){
                        pasadas[a]=1;
                    }
                    else{
                        pasadas[a]=0;
                    }
                }
                else{
                    pasadas[a]=0;
                }
                   
            }
        }

       for(int i=1; i<posible.length; i++){
           if(posible[i]){
                if(pasadas[i]==1){
                    if(distancias_posibles[i]<aux){
                        aux=distancias_posibles[i];
                        retorno=i;
                    }
                }
            }
        }

        return retorno;
    }
    
    /*

    
    /*
        Evita una vez decido que movimiento se realiza, mirar si tiene algún obstaculo,
        por ejemplo si nos movemos al norte no haya obstaculo justo en esa dirección
    */
    
 /**
 * @author Marcos
 */
    private void evitarChocar(){
        if(radar[7]==1){
            posible[8]=false;
        } 
        if(radar[8]==1){
            posible[9]=false;
        }
        if(radar[13]==1){
            posible[6]=false;
        }
        if(radar[18]==1){
            posible[3]=false;
        }
        if(radar[17]==1){
            posible[2]=false;
        } 
        if(radar[16]==1){
            posible[1]=false;
        }
        if(radar[11]==1){
            posible[4]=false;
        }
        if(radar[6]==1){
            posible[7]=false;
        } 
        posible[5]=false;
        posible[0]=false;
    }

 /**
 * @author Marcos
 */
    private void restaurarMovimiento(){
        for (int i=0; i<posible.length; i++){
            posible[i]=true;
            distancias_posibles[i]=0;
        }
    }
    
 /**
 * @author Marcos
 */
    private void calcularDistancias(){
        distancias_posibles[1]=scanner[16];
        distancias_posibles[2]=scanner[17];
        distancias_posibles[3]=scanner[18];
        distancias_posibles[4]=scanner[11];
        distancias_posibles[6]=scanner[13];
        distancias_posibles[7]=scanner[6];
        distancias_posibles[8]=scanner[7];
        distancias_posibles[9]=scanner[8];
    }
    

    /*
        Primera heuristica, busca la distancia mas pequeñan, decide que movimiento hacer, 
    comprueba que no haya obstaculos, si no hay obstaculos se mueve hacia esa dirección 
    mientras no se haya encontrado el objetivo.
    */
    
 /**
 * @author Marcos
 */
    private void estrategia() throws InterruptedException, FileNotFoundException{
        while(!objetivoEncontrado && numeroPasos<2500){
            if(bateria<2){
                this.repostar();
            }
            else{
                this.calcularDistancias();
                this.evitarChocar();
                int movimiento=this.decide();
                this.mover(movimiento);
                this.objetivo();
                this.restaurarMovimiento();
            }
            
            System.out.println("Numero de pasos: "+numeroPasos);
        }
        this.finalizarSesion();
    }
    
    /*
        Comprueba si ha llegado al objetivo.
    */
 /**
 * @author Marcos
 */
    private void objetivo(){
        if(radar[0]==2 || radar[1]==2 || radar[2]==2 || radar[3]==2 || radar[4]==2 ||
            radar[5]==2 || radar[10]==2 || radar[15]==2 || radar[20]==2 ||
                radar[9]==2 || radar[14]==2 || radar[19]==2 || radar[24]==2){
            noSePuede++;
        }
        if(radar[12]==2 || noSePuede>=20){
           objetivoEncontrado=true;
        }
    }
    
}