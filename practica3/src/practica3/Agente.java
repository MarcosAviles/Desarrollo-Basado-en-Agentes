/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jettison.json.JSONString;




/**
 *
 * @author marcos
 */
public class Agente extends SingleAgent {
    
    static final String vehiculo1 = "vehiculo111";
    static final String vehiculo2 = "vehiculo112";
    static final String vehiculo3 = "vehiculo113";
    static final String vehiculo4 = "vehiculo114";  
    
    String reply;
    String conversation_id;
    String mapa;
    boolean jefe;
    String rol;
    ArrayList<Map<String,String>> vehiculos;
    ArrayList<Map<String,Integer>> posiciones;
    int posicionY, posicionYanterior;
    int posicionX, posicionXanterior;
    int bateria;
    int energia;
    int memoria[][];
    int sensor [];
    boolean objetivo, objetivo2;
    boolean seguir;
    boolean posible [];
    boolean izquierda;
    boolean detener_algoritmo=false;
    int pasos;
    boolean objetivo_visto;
    boolean fin, explorando, envioObjetivo;
    int vehiculosEnObjetivo;
    int mov_anterior;
    
    
    /**
 *
 * @author marcos
 */
    public Agente(AgentID aid, String map, boolean jefe_) throws Exception{
        super(aid);
        mapa=map;
        jefe=jefe_;
        vehiculos =new ArrayList <>();
        posiciones =new ArrayList <>();
        posible= new boolean[10];
        for(int i=0; i<posible.length; i++){
            posible[i]=false;
        }
        seguir=true;
        pasos=0;
        objetivo_visto=false;
        fin=false;
        objetivo2=explorando=envioObjetivo=false;
        vehiculosEnObjetivo=0;
        mov_anterior=0;
        memoria= new int [550][550];
        for(int i=0; i<550; i++){
            for(int x=0; x<550; x++){
               memoria[i][x]=0;
            }
        }
    }
    
    /*@Override
    public void init(){
        
    }*/
    @Override
    
    //Mientras se ejecuta el agente
    /**
     *
     * @author marcos
     */
    public void execute(){
        try {
            
            if(jefe){
                while(seguir){
                    this.enviarMensaje(1);
                    this.enviarMensaje(2);
                    this.enviarRoles();
                    if(this.comprobarRoles()){
                        seguir=false;
                    }
                    else{
                        this.restaurarVehiculos();
                        this.enviarMensaje(3);
                        
                    }
            }
            }
            else{
                while(seguir){
                    this.recibirMensaje();
                    this.enviarMensaje(2);
                    this.enviarRoles();
                }
            }
            this.inicializarCosas();
            this.comenzar();
            if(jefe){
                this.primeraOrden();
                //this.enviarMensaje(3);
            }
            
        } catch (InterruptedException ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(Agente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    /**
     *
     * @author marcos
     */
    private void enviarMensaje(int aux) throws InterruptedException, Exception{
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        JsonObject objeto= new JsonObject();
        
        switch(aux){
            case 1:
                outbox.setReceiver(new AgentID("Denebola"));
                outbox.setPerformative(ACLMessage.SUBSCRIBE);
                objeto.add("world", mapa);
                outbox.setContent(objeto.toString());
                this.send(outbox);
                this.recibirMensaje();

                outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(vehiculo2));
                outbox.addReceiver(new AgentID(vehiculo3));
                outbox.addReceiver(new AgentID(vehiculo4));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setConversationId(conversation_id);
                outbox.setContent("clave");
                this.send(outbox);
                break;
                    
            case 2:
                outbox.setReceiver(new AgentID("Denebola"));
                outbox.setPerformative(ACLMessage.REQUEST);
                objeto.add("command", "checkin");
                outbox.setContent(objeto.toString());
                outbox.setConversationId(conversation_id);
                this.send(outbox);
                this.recibirMensaje();
                break;
                
            case 3:
                outbox.setReceiver(new AgentID("Denebola"));
                outbox.setPerformative(ACLMessage.CANCEL);
                outbox.setContent(" ");
                outbox.setConversationId(conversation_id);
                this.send(outbox);
                this.recibirMensaje();
                this.recibirMensaje();
                break;
                
            case 4:
                outbox.setReceiver(new AgentID("Denebola"));
                outbox.setPerformative(ACLMessage.QUERY_REF);
                outbox.setInReplyTo(reply);
                outbox.setContent(" ");
                outbox.setConversationId(conversation_id);
                this.send(outbox);
                this.recibirMensaje();
                break;
            
            case 5:
                outbox.setReceiver(new AgentID("Denebola"));
                objeto.add("command", "refuel");
                outbox.setPerformative(ACLMessage.REQUEST);
                outbox.setInReplyTo(reply);
                outbox.setContent(objeto.toString());
                outbox.setConversationId(conversation_id);
                this.send(outbox);
                this.recibirMensaje();
                break;
        }
        
        
    }
    
    /**
     *
     * @author marcos
     */
    private void recibirMensaje() throws InterruptedException, Exception{
            ACLMessage inbox=new ACLMessage();
            inbox=receiveACLMessage();
            switch(inbox.getPerformative()){
                case "FAILURE": 
                    
                    if(inbox.getContent().contains("continuar")){
                        seguir=true;
                    }
                    break;
                case "NOT-UNDERSTOOD": 
                                        System.out.println(inbox.getContent());
                                        System.out.println(inbox.getReceiver());
                                        this.enviarMensaje(3);
                                        break;
                case "INFORM": 
                    if(inbox.getContent().contains("OK")){
                        reply=inbox.getReplyWith();
                        conversation_id=inbox.getConversationId();
                    }
                    if(inbox.getContent().contains("clave")){
                        reply=inbox.getReplyWith();
                        conversation_id=inbox.getConversationId();
                    }
                    if(inbox.getContent().contains("battery")){
                        JsonObject injson = Json.parse(inbox.getContent()).asObject();
                        injson=injson.get("result").asObject();
                        bateria=injson.get("battery").asInt();
                        posicionX=injson.get("x").asInt();
                        posicionY=injson.get("y").asInt();
                        memoria[posicionX][posicionY]++;
                        
                        JsonArray ja=injson.get("sensor").asArray();
                        for(int x=0; x<sensor.length; x++){
                            sensor[x]=(int) ja.get(x).asInt();
                        }
                        energia=injson.get("energy").asInt();
                        objetivo=injson.get("goal").asBoolean();
                        reply=inbox.getReplyWith();
                        conversation_id=inbox.getConversationId();
                    }
                    
                    if(inbox.getContent().contains("capabilities")){
                        
                        JsonObject injson = Json.parse(inbox.getContent()).asObject();
                        int fuel=injson.get("capabilities").asObject().get("fuelrate").asInt();
                        switch(fuel){
                            case 1:
                                rol="coche";
                                System.out.println("Soy coche "+this.getAid());
                                break;
                            case 2:
                                rol="dron";
                                System.out.println("Soy dron "+this.getAid());
                                break;
                            case 4:
                                rol="camion";
                                System.out.println("Soy camion "+this.getAid());
                                break;
                            }
                    }
                    if(inbox.getContent().contains("rol")){
                         JsonObject injson = Json.parse(inbox.getContent()).asObject();
                         String js = injson.get("rol").asString();
                         Map<String,String> aux = new HashMap<String,String>();
                         aux.put(inbox.getSender().toString(),js);
                         vehiculos.add(aux);
                    }
                    if(inbox.getContent().contains("trace")){
                        JsonObject injson = Json.parse(inbox.getContent()).asObject();
                        JsonArray ja=injson.get("trace").asArray();
                        try{
                            System.out.println("Recibiendo traza");
                            byte data[]=new byte[ja.size()];
                            for(int x=0; x<data.length; x++){
                                data[x]=(byte) ja.get(x).asInt();
                            }
                            try (FileOutputStream fos = new FileOutputStream(conversation_id+" "+mapa+".png")) {
                                fos.write(data);
                            }
                            System.out.println("Traza Guardada");
                        } 
                        catch (IOException ex){
                            System.err.println("Error procesando traza");
                        }
                    }
                                
                    if(inbox.getContent().contains("aqui estoy")){
                        String posicion=inbox.getContent();
                        String [] parts=posicion.split(",");
                        int aux=Integer.parseInt(parts[1]);
                        String agente=inbox.getSender().toString();
                        String [] parts2=agente.split("@");
                        parts2=parts2[0].split("//");
                        Map<String, Integer> pos= new HashMap<String, Integer>();
                        pos.put(parts2[1], aux);
                        posiciones.add(pos);
                    }
                    if(inbox.getContent().contains("vete a la izquierda")){
                        izquierda=true;
                        explorando=true;
                        System.out.println("Me llega la orden de tirar para la izquierda"+this.getAid());
                        estrategia();
                    }
                    
                    if(inbox.getContent().contains("vete a la derecha")){
                        izquierda=false;
                        explorando=true;
                        System.out.println("Me llega la orden de tirar para la derecha"+this.getAid());
                        estrategia();
                    }
                    
                    if(inbox.getContent().contains("detener_algoritmo")){
                        //this.recibirMensaje();
                        detener_algoritmo=true;
                        objetivo2=true;
                         JsonObject injson = Json.parse(inbox.getContent()).asObject();
                         int posx = injson.get("posicionX").asInt();
                         int posy = injson.get("posicionY").asInt();
                          System.out.println("Me llega la orden de Parar"+this.getAid());
                          Random r = new Random();
                          int extra = r.nextInt(10)+1;
                          Thread.sleep(1000*extra);
                        this.objetivoEncontrado2(posx, posy);
                       
                    }
                    if(inbox.getContent().contains("he llegado al objetivo")){
                        System.out.println("Ha llegado un agente al objetivo"+getAid());
                        vehiculosEnObjetivo++;
                        this.recibirMensaje();
                       
                    }
                        break;
                        
                        
                    
                case "CONFIRM": 
                    if(inbox.getContent().contains("continuar")){
                        seguir=false;
                    }break;
                    
                case "QUERY-REF":
                    if(inbox.getContent().contains("dame tu localizacion")){
                        ACLMessage outbox= new ACLMessage();
                        outbox.setSender(this.getAid());
                        outbox.addReceiver(new AgentID(vehiculo1));
                        outbox.setPerformative(ACLMessage.INFORM);
                        outbox.setContent("aqui estoy,"+posicionX);
                        this.send(outbox);
                        this.recibirMensaje();
                    }break;
        }
    }
    
    /**
     *
     * @author marcos
     */
    void enviarRoles() throws Exception{
        if(!jefe){
            ACLMessage outbox= new ACLMessage();
            outbox.setSender(this.getAid());
            outbox.addReceiver(new AgentID(vehiculo1));
            outbox.setPerformative(ACLMessage.INFORM);
            JsonObject objeto= new JsonObject();
            objeto.add("rol",rol);
            outbox.setContent(objeto.toString());
            this.send(outbox);
            this.recibirMensaje();
           
        }else{
            Map<String,String> aux = new HashMap<String,String>();
            aux.put(this.getAid().toString(),rol);
            vehiculos.add(aux);
            recibirMensaje();
            recibirMensaje();
            recibirMensaje();
            
        }
    }
    
    /**
     *
     * @author marcos
     */
    boolean comprobarRoles(){
        System.out.println("ConversationID: " +conversation_id);
        boolean retorno=false;
        int aux=0;
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.addReceiver(new AgentID(vehiculo2));
        outbox.addReceiver(new AgentID(vehiculo3));
        outbox.addReceiver(new AgentID(vehiculo4));
        outbox.setContent("continuar");
        
        for(int i=0; i<vehiculos.size(); i++){
            Map<String, String> vehiculo = new HashMap<String, String>();
            vehiculo=vehiculos.get(i);
            if(vehiculo.containsValue("coche")){
                aux++;
            }
        }
        if(aux>=2){
            retorno=true;
            outbox.setPerformative(ACLMessage.CONFIRM);
            this.send(outbox);
        }
        else{
            outbox.setPerformative(ACLMessage.FAILURE);
            this.send(outbox);
            retorno=false;
        }
        return retorno;
    }
    
    /**
     *
     * @author marcos
     */
    void inicializarCosas() throws Exception{
        if(rol=="coche"){
            sensor= new int[25];
        }
        if(rol=="dron"){
            sensor= new int[9];
        }
        if(rol=="camion"){
            sensor= new int[121];
        }
        actualizarSensores();
    }
    
    /**
     *
     * @author marcos
     */
    void actualizarSensores() throws Exception{
        this.enviarMensaje(4);
        if(bateria<5) 
            repostar();
    }
    
    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    void estrategia() throws Exception{
        objetivo_visto();
        if(!objetivo_visto){
        movimiento_inicial();
            while (!detener_algoritmo){
               algoritmo_busqueda();
            }
        }
    }
    
    /**
     *
     * @author marcos
     */
    void repostar() throws Exception{
        this.enviarMensaje(5);
        System.out.println("soy" + this.getAid() +" " + energia );
    }
    
    /**
     *
     * @author marcos
     */
    void mover(int aux) throws Exception{
        mov_anterior=aux;
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        JsonObject objeto= new JsonObject();
        outbox.setReceiver(new AgentID("Denebola"));
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setConversationId(conversation_id);
        outbox.setInReplyTo(reply);
        switch(aux){
            case 1: 
                objeto.add("command", "moveSW");
                  System.out.println("soy" + this.getAid() +"Me muevo al SurWest");
                break;
            case 2: 
                objeto.add("command", "moveS");
                 System.out.println("soy" + this.getAid() +"Me muevo al Sur");
                break;
            case 3: 
                objeto.add("command", "moveSE");
                 System.out.println("soy" + this.getAid() +"Me muevo al SurEst");
                break;
            case 4: 
                objeto.add("command", "moveW");
                 System.out.println("soy" + this.getAid() +"Me muevo al West");
                break;
            case 6: 
                objeto.add("command", "moveE");
                 System.out.println("soy" + this.getAid() +"Me muevo al Est");
                break;
            case 7: 
                objeto.add("command", "moveNW");
                 System.out.println("soy" + this.getAid() +"Me muevo al NortWest");
                break;
            case 8: 
                objeto.add("command", "moveN");
                 System.out.println("soy" + this.getAid() +"Me muevo al Norte");
                break;
            case 9: 
                objeto.add("command", "moveNE");
                 System.out.println("soy" + this.getAid() +"Me muevo al NorteEste");
                break;
            
        }
       pasos++;
        /*if(pasos>800){
            System.out.println("NUMERO DE PASOS LIMITE");
            this.enviarMensaje(3);
            
        }*/
        outbox.setContent(objeto.toString());
        this.send(outbox);
        //System.out.println("Sensor del agente "+getAid());
        //imprimirSensor();
        this.recibirMensaje();
        
}
    
    /**
     *
     * @author marcos
     */
    void imprimirSensor(){
        for (int i=0; i<sensor.length; i++){
            if(i%5==0){
                System.out.println("");
            }
            System.out.print(sensor[i]+" ");
        }
    }
    
    /**
     *
     * @author estrella
     */
    void objetivo_visto() throws Exception{
        
        for(int i=0; i<sensor.length; i++){
            if(sensor[i]==3){
                objetivo_visto=true;
                detener_algoritmo=true;
                ir_a_objetivo();
            }
        } 
    }
    
        
    int X_max=495; // la maxima coord x a la que va a ir
    int Y_max=495; // la maxima coord y a la que va a ir
    
    /**
     *
     * @author estrella
     * @author david
     * @author juanma
     */
    private void movimiento_inicial() throws Exception{
            if(posicionY<10){  // empieza arriba
               /* int i=0;
                while(i<5 && !objetivo_visto){
                    if(!evitarChocarZigzag(2)){
                        mover(2);  //se mueve abajo 5 casillas
                        i++;
                    }
                    else{
                        if(izquierda){
                            mover(6);
                        }
                        else{
                            mover(4);
                        }
                    }
                    actualizarSensores();
                    objetivo_visto();
                }*/
                mover(2);
                actualizarSensores();
            }
            else{    // empieza abajo
                /*int i=0;
                while(i<5 && !objetivo_visto){
                    if(!evitarChocarZigzag(8)){
                        mover(8);  //se mueve abajo 5 casillas
                        i++;
                    }
                    else{
                        if(izquierda){
                            mover(6);
                        }
                        else{
                            mover(4);
                        }
                    }
                    actualizarSensores();
                    objetivo_visto();
                }*/
                mover(8);
                actualizarSensores();
               // Y_max=posicionY;
                //X_max=Y_max;
            }
            //el agente actual está más a la derecha
            //if (posicionX >= agente2.coord_x){
            if(!izquierda){
                while(sensor[14]!=2 && !objetivo_visto){ //hasta el borde
                  /*  if(!evitarChocarZigzag(6)){
                         mover(6);
                    }
                    else{
                        if(posicionY<50){
                            if(!evitarChocarZigzag(2)){
                                 mover(2);
                            }
                        }
                        else{
                            if(!evitarChocarZigzag(8)){
                                mover(8);
                            }
                        }
                    }*/
                  mover(6);
                    actualizarSensores();
                    objetivo_visto();
                    
                }
                int j=0;
                while(j<4){
                   /* if(!evitarChocarZigzag(4)){
                         mover(4); //llevarlo 4 casillas más a la izq.  
                         j++;
                         System.out.println("WHIIIIIIILE J");
                    }
                    else{
                        if(posicionY<50){
                            mover(2);
                        }
                        else{
                            mover(8);
                        }
                    }*/
                   mover(4);
                   System.out.println("AQUI ESTOY");
                    actualizarSensores();
                    j++;
                    objetivo_visto();
                }
               // X_max=posicionX;
                //Y_max=X_max;
            }
            //el agente actual está más a la izquierda
            else{
                while(sensor[10]!=2 && !objetivo_visto){ //hasta el borde
                    /*if(!evitarChocarZigzag(4)){
                         mover(4);
                    }
                    else{
                        if(posicionY==5){
                            mover(2);
                        }
                        else{
                            mover(8);
                        }
                    }*/
                    mover(4);
                    System.out.println("AQUI ESTOY 2");
                    actualizarSensores();
                    objetivo_visto();
                }
                int j=0;
                while(j<4){
                    /*if(!evitarChocarZigzag(6)){
                         mover(6); //llevarlo 4 casillas más a la izq.  
                         j++;
                    }
                    else{
                        if(posicionY==5){
                            mover(2);
                        }
                        else{
                            mover(8);
                        }
                    }*/
                    mover(6);
                    actualizarSensores();
                    j++;
                    objetivo_visto();
                }
                
            }
            
            if(objetivo_visto){
                
                this.ir_a_objetivo();
                
               
            }
        
        
        
    }
    
    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    private void ir_a_objetivo() throws Exception{
        int aux=0;
        while(!objetivo2){
                    
                    if(sensor[12]==3){
                            if(sensor[0]==3 && sensor[0]!=4) mover(7);
                            else if(sensor[4]==3 && sensor[4]!=4) mover(9);
                            else if(sensor[20]==3 && sensor[20]!=4) mover(1);
                            else if(sensor[24]==3 && sensor[24]!=4) mover(3);
                            else if(sensor[2]==3 && sensor[2]!=4) mover(8);
                            else if(sensor[10]==3 && sensor[10]!=4) mover(4);
                            else if(sensor[14]==3 && sensor[14]!=4) mover(6);                            
                            else if(sensor[22]==3 && sensor[22]!=4) mover(2);
                            
                            aux++;
                            if(aux>1){
                                envioObjetivo=true;
                                System.out.println("Estoy en el while de ir a objetivo"+getAid());
                                this.informarObjetivo2(posicionX, posicionY);
                                objetivo2=true;
                            }
                            

                    }
                    else{
                        if ((sensor[0]==3 && sensor[0]!=4) || (sensor[1]==3 && sensor[1]!=4) 
                                || (sensor[5]==3 && sensor[5]!=4) || (sensor[6]==3 && sensor[6]!=4)){
                                mover(7);
                                actualizarSensores();
                            }
                            if ((sensor[2]==3 && sensor[2]!=4) || (sensor[7]==3 && sensor[7]!=4)){
                                mover(8);
                                actualizarSensores();
                            }
                            if ((sensor[3]==3 && sensor[3]!=4) || (sensor[4]==3 && sensor[4]!=4) || 
                                    (sensor[8]==3 && sensor[8]!=4) || (sensor[9]==3 && sensor[9]!=4)){
                                mover(9);
                                actualizarSensores();
                            }
                            if ((sensor[10]==3 && sensor[10]!=4) || (sensor[11]==3 && sensor[11]!=4)){
                                mover(4);
                                actualizarSensores();
                            }
                            if ((sensor[15]==3 && sensor[15]!=4) || (sensor[16]==3 && sensor[16]!=4) 
                                    || (sensor[20]==3 && sensor[20]!=4) || (sensor[21]==3 && sensor[21]!=4)){
                                mover(1);
                                actualizarSensores();
                            }
                            if ((sensor[17]==3 && sensor[17]!=4) || (sensor[22]==3 && sensor[22]!=4)){
                                mover(2);
                                actualizarSensores();
                            }
                            if ((sensor[18]==3 && sensor[18]!=4) || (sensor[19]==3 && sensor[19]!=4) 
                                    || (sensor[23]==3 && sensor[23]!=4) || (sensor[24]==3 && sensor[24]!=4)){
                                mover(3);
                                actualizarSensores();
                            }
                            if ((sensor[13]==3 && sensor[13]!=4) || (sensor[14]==3 && sensor[14]!=4)){
                                mover(6);
                                actualizarSensores();
                            
                            }             
                    }
                }
        
    }
    
    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    private void algoritmo_busqueda() throws Exception{
        System.out.println("ZIG ZAG");
        
        X_max=495; // la maxima coord x a la que va a ir
        Y_max=495; // la maxima coord y a la que va a ir
        int mov_dcha;
        int mov_izq;
        int mov_arriba;
        int mov_abajo;
        if(posicionY<50){  // EMPIEZA ARRIBA
            mov_dcha=0;
            mov_izq=0;
            mov_abajo=0;
            mov_arriba=0;
            while(posicionY<Y_max && !objetivo_visto){ //MOV ABAJO
                
                if(!evitarChocarZigzag(2)){
                        mover(2);
                        actualizarSensores();
                        if(mov_dcha>0 && !evitarChocarZigzag(4)){
                            mover(4);
                            actualizarSensores();
                            mov_dcha--;
                        }
                        if(mov_izq>0 && !evitarChocarZigzag(6)){
                            mover(6);
                            actualizarSensores();
                            mov_izq--;
                        }
                }
                else{
                    if(izquierda){
                        if(!evitarChocarZigzag(6)){
                            //System.out.print("Me muevo al 6...");
                            mover(6); 
                            actualizarSensores();
                            mov_dcha++;
                        }
                        else if(!evitarChocarZigzag(9)){
                                 mover(9);
                                 actualizarSensores();
                                 mov_arriba++;
                        }
                      
                        else if(!evitarChocarZigzag(1)){
                                 mover(1);
                                 actualizarSensores();
                                 mov_arriba++;
                        }
                    }
                    else{
                        if(!evitarChocarZigzag(4)){
                            mover(4); 
                           // System.out.print("Me muevo al 4...");
                            actualizarSensores();
                            mov_izq++;
                        }
                        else if(!evitarChocarZigzag(7)){
                                 mover(7);
                                 actualizarSensores();
                                 mov_arriba++;
                        }
                        else if(!evitarChocarZigzag(3)){
                                 mover(3);
                                 actualizarSensores();
                                 mov_arriba++;
                        }
                    }
                    
                    
                }
                
                
                
                objetivo_visto();
            } //------------------------------------------------- FIN MOV ABAJO
            
            
            
           // if(posicionX<50){
           if(izquierda){    // MOV DCHA 
               mov_arriba=0;
               mov_abajo=0;
                int destinoX=posicionX+8;
                while(posicionX<destinoX && !objetivo_visto){
                    
                    if(!evitarChocarZigzag(6)){
                        mover(6);
                        actualizarSensores();
                        if(mov_arriba>0 && !evitarChocarZigzag(2)){
                            mover(2);
                            actualizarSensores();
                            mov_arriba--;
                        }
                        if(mov_abajo>0 && !evitarChocarZigzag(8)){
                            mover(8);
                            actualizarSensores();
                            mov_abajo--;
                        }
                    }
                    else{
                        if(posicionY<50){
                            if(!evitarChocarZigzag(2)){
                                 mover(2);
                                 actualizarSensores();
                                 mov_abajo++;
                            }
                        }
                        else{
                            if(!evitarChocarZigzag(8)){
                                 mover(8);
                                 actualizarSensores();
                                 mov_arriba++;
                            }
                        }
                    }
                    
                    
                    
                    
                    objetivo_visto();
                }
            } //-------------------------- FIN MOV DCHA
            
            
            //else if(posicionX>50){
           else if (!izquierda){   // MOV IZQ
               mov_arriba=0;
               mov_abajo=0;
               int destinoX=posicionX-8;
                while(posicionX>destinoX && !objetivo_visto){
                    
                    if(!evitarChocarZigzag(4)){
                        mover(4);
                        actualizarSensores();
                        if(mov_arriba>0 && !evitarChocarZigzag(2)){
                            mover(2);
                            actualizarSensores();
                            mov_arriba--;
                        }   
                        if(mov_abajo>0 && !evitarChocarZigzag(8)){
                            mover(8);
                            actualizarSensores();
                            mov_abajo--;
                        }
                    }
                    else{
                        if(posicionY<50){
                            if(!evitarChocarZigzag(2)){
                                 mover(2);
                                 actualizarSensores();
                                 mov_abajo++;
                            }
                        }
                        else{
                            if(!evitarChocarZigzag(8)){
                                 mover(8);
                                 actualizarSensores();
                                 mov_arriba++;
                            }
                        }
                    }
                    
                    
                    objetivo_visto();
                }
            }  // ------------------ FIN MOV IZQ
        }
        
        else{   // EMPIEZA ABAJO
            mov_dcha=0;
            mov_izq=0;
            mov_arriba=0;
            mov_abajo=0;
            while(posicionY>5 && !objetivo_visto){   // MOV ARRIBA
                
                if(!evitarChocarZigzag(8)){
                    mover(8);  
                    actualizarSensores();
                    if(mov_dcha>0 && !evitarChocarZigzag(4)){
                        mover(4);
                        actualizarSensores();
                        mov_dcha--;
                    }
                    if(mov_izq>0 && !evitarChocarZigzag(6)){
                        mover(6);
                        actualizarSensores();
                        mov_izq--;
                    }
                  /*  if(mov_abajo>0 && !evitarChocarZigzag(8)){
                        mover(8);
                        actualizarSensores();
                        mov_abajo--;
                    }*/
                }
                else{
                    if(izquierda){
                        if(!evitarChocarZigzag(6)){
                                 mover(6);
                                 actualizarSensores();
                                 mov_dcha++;
                        }
                        else if(!evitarChocarZigzag(3)){
                                 mover(3);
                                 actualizarSensores();
                                 mov_abajo++;
                        }
                        else if(!evitarChocarZigzag(7)){
                                mover(7);
                                 actualizarSensores();
                                 mov_abajo++;
                        }
                    }
                    else{
                        if(!evitarChocarZigzag(4)){
                                 mover(4);
                                 actualizarSensores();
                                 mov_izq++;
                        }
                        else if(!evitarChocarZigzag(1)){
                                 mover(1);
                                 actualizarSensores();
                                 mov_abajo++;
                        }
                        else if(!evitarChocarZigzag(9)){
                                mover(9);
                                 actualizarSensores();
                                 mov_abajo++;
                        }
                    }
                }
                
                
                
                objetivo_visto();
            } // --------------------------- FIN MOV ARRIBA
           
            
            
            //if(posicionX<50){
            if(izquierda){    // MOV DCHA 
               mov_arriba=0;
               mov_abajo=0;
                int destinoX=posicionX+8;
                while(posicionX<destinoX && !objetivo_visto){
                    
                    if(!evitarChocarZigzag(6)){
                        mover(6);
                        actualizarSensores();
                        if(mov_arriba>0 && !evitarChocarZigzag(2)){
                            mover(2);
                            actualizarSensores();
                            mov_arriba--;
                        }
                        if(mov_abajo>0 && !evitarChocarZigzag(8)){
                            mover(8);
                            actualizarSensores();
                            mov_abajo--;
                        }
                    }  
                    else{
                        if(posicionY<50){
                            if(!evitarChocarZigzag(2)){
                                 mover(2);
                                 actualizarSensores();
                                 mov_abajo++;
                            }
                        }
                        else{
                            if(!evitarChocarZigzag(8)){
                                 mover(8);
                                 actualizarSensores();
                                 mov_arriba++;
                            }
                        }
                    }
                    
                 
                    objetivo_visto();
                }
            } // ---------------- FIN MOV DCHA
            
            
            //else if(posicionX>50){
            else if(!izquierda){ // MOV IZQ
               
               mov_arriba=0;
               mov_abajo=0;
               int destinoX=posicionX-8;
                while(posicionX>destinoX && !objetivo_visto){
                    
                    if(!evitarChocarZigzag(4)){
                        mover(4);
                        actualizarSensores();
                        if(mov_arriba>0 && !evitarChocarZigzag(2)){
                            mover(2);
                            actualizarSensores();
                            mov_arriba--;
                        }
                        if(mov_abajo>0 && !evitarChocarZigzag(8)){
                            mover(8);
                            actualizarSensores();
                            mov_abajo--;
                        }
                    }
                    else{
                        if(posicionY<50){
                            if(!evitarChocarZigzag(2)){
                                 mover(2);
                                 actualizarSensores();
                                 mov_abajo++;
                            }
                        }
                        else{
                            if(!evitarChocarZigzag(8)){
                                 mover(8);
                                 actualizarSensores();
                                 mov_arriba++;
                            }
                        }
                        
                    }
                    
                    
                    objetivo_visto();
                }
            } // ------------------- FIN MOV IZQ
            
        }
        
        if(objetivo_visto){
                this.ir_a_objetivo();
            }
        
        
    }
    
    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    void objetivoEncontrado(int x, int y) throws Exception{
        System.out.println("Se donde esta el objetivo"+x+" "+y+ " voy hacia alli  "+getAid()+"  ");
        if(explorando && !envioObjetivo){
            this.recibirMensaje(); 
        }
        //this.recibirMensaje(); // solo tienen que recibirlo los agentes coches que esten andando los otros no.
        
        if(rol=="dron"){
            while(!fin){ //NOSE SI ES ESA LA VARIABLE QUE DICE SI LO HA ENCONTRADO
             
            if(posicionX>x && posicionY>y){
                mover(7);
                actualizarSensores();
            }
            else if (posicionX>x && posicionY<y){
                mover(1);  
                actualizarSensores();
            }
            else if (posicionX<x && posicionY<y){
                mover(3);   
                actualizarSensores();
            }
            else if (posicionX<x && posicionY>y){
                mover(9);  
                actualizarSensores();
            }
            else if (posicionX>x){
                mover(4);
                actualizarSensores();
            }
            else if (posicionX<x){
                mover(6);
                actualizarSensores();
            }
            else if (posicionY>y){
                mover(8);
                actualizarSensores();
            }
            else if (posicionY<y){
                mover(2);
                actualizarSensores();
            }
            
           // for(int i=0; i<9 && objetivo_visto==false; i++){
            if(sensor[4]==3){
                objetivo_visto=true;
                fin=true;
                //this.ir_a_objetivo();
            }
            //}
            
            /*if(sensor[4]==3)
                fin=true;     */       
            
            }
        }
        
        if(rol=="camion" || rol=="coche"){ //segundo coche
            while(!fin){
            
            if(posicionX>x && posicionY>y){ //objetivo arriba izq
                System.out.println("Da la orde de arriba izq");
                if(!evitarChocarZigzag(7) && mov_anterior!=3)
                        mover(7);
                else if(!evitarChocarZigzag(4) && mov_anterior!=6)
                        mover(4);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                        mover(8);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                        mover(1);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                        mover(9);
                else if(!evitarChocarZigzag(6) && mov_anterior!=4)
                        mover(6);
                actualizarSensores();
            }
            else if (posicionX>x && posicionY<y){ //objetivo abajo izq
                System.out.println("Da la orde de abajo izq");
                if(!evitarChocarZigzag(1) && mov_anterior!=9)
                        mover(1); 
                else if(!evitarChocarZigzag(4) && mov_anterior!=6)
                        mover(4);
                else if(!evitarChocarZigzag(2) && mov_anterior!=8)
                        mover(2);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                        mover(3);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                        mover(7);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                        mover(8);
                actualizarSensores();
            }
            else if (posicionX<x && posicionY<y){ //abajo dcha
                System.out.println("Da la orde de abajo derecha");
                if(!evitarChocarZigzag(3) && mov_anterior!=7)
                        mover(3);   
                else if(!evitarChocarZigzag(6) && mov_anterior!=3)
                        mover(6);
                else if(!evitarChocarZigzag(2) && mov_anterior!=8)
                    mover(2);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                        mover(1);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                        mover(8);
                else if(!evitarChocarZigzag(4) && mov_anterior!=6)
                        mover(4);
                actualizarSensores();
            }
            else if (posicionX<x && posicionY>y){ //arriba dcha
                System.out.println("Da la orde de arriba derecha");
                if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                    mover(8);
                else if(!evitarChocarZigzag(6) && mov_anterior!=4)
                    mover(6);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                    mover(7);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                        mover(3);
                else if(!evitarChocarZigzag(2) && mov_anterior!=8)
                        mover(2);
                actualizarSensores();
            }
            else if (posicionX>x){
                System.out.println("Da la orde de izquierda justo");
                if(!evitarChocarZigzag(4) && mov_anterior!=6)
                    mover(4);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                    mover(7);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                    mover(1);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                    mover(8);
                else if(!evitarChocarZigzag(2) && mov_anterior!=8)
                    mover(2);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                    mover(3);
                actualizarSensores();
            }
            else if (posicionX<x){
                System.out.println("Da la orde de derecha justo");
                if(!evitarChocarZigzag(6) && mov_anterior!=4)
                    mover(6);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                    mover(3);
                else if(!evitarChocarZigzag(2) && mov_anterior!=8)
                    mover(2);
                else if(!evitarChocarZigzag(8) && mov_anterior!=2)
                    mover(8);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                    mover(9);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                    mover(3);
                actualizarSensores();
            }
            else if (posicionY>y){
                System.out.println("Da la orde de arriba justo");
                if(!evitarChocarZigzag(8) && mov_anterior!=2)
                    mover(8);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                    mover(7);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(4) && mov_anterior!=6)
                    mover(4);
                else if(!evitarChocarZigzag(6) && mov_anterior!=4)
                    mover(6);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                    mover(3);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                    mover(1);
                actualizarSensores();
            }
            else if (posicionY<y){
                System.out.println("Da la orde de abajo justo");
                if(!evitarChocarZigzag(2) && mov_anterior!=8)
                    mover(2);
                else if(!evitarChocarZigzag(1) && mov_anterior!=9)
                    mover(1);
                else if(!evitarChocarZigzag(3) && mov_anterior!=7)
                    mover(3);
                else if(!evitarChocarZigzag(4) && mov_anterior!=6)
                    mover(4);
                else if(!evitarChocarZigzag(6) && mov_anterior!=4)
                    mover(6);
                else if(!evitarChocarZigzag(9) && mov_anterior!=1)
                    mover(9);
                else if(!evitarChocarZigzag(7) && mov_anterior!=3)
                    mover(3);
                actualizarSensores();
            }
            
            if(rol=="coche"){
               // for(int i=0; i<25 && objetivo_visto==false; i++){
                    if(sensor[12]==3){
                        objetivo_visto=true;
                        fin=true;
                       // this.ir_a_objetivo();
                    }       
                //}
            }
            else{
                //for(int i=0; i<121 && objetivo_visto==false; i++){
                    if(sensor[60]==3){
                        objetivo_visto=true;
                        fin=true;
                    }
                //}
            }
            
            /*
            if(rol=="coche"){
                if(sensor[12]==3)
                fin=true;
                this.objetivoCumplido();
                
            }
            if(rol=="camion"){
                if(sensor[60]==3)
                fin=true;
            }
              */
            
        }
        }
        if(vehiculosEnObjetivo==4){
            System.out.println("Soy el jefe y se acaba la partida "+this.getAid());
            this.enviarMensaje(3);
        }
        
    }
    
    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    void objetivoEncontrado2(int x, int y) throws Exception{
        System.out.println("Se donde esta el objetivo"+x+" "+y+ " voy hacia alli  "+getAid()+"  ");
        if(explorando && !envioObjetivo){
            this.recibirMensaje(); 
        }
        //this.recibirMensaje(); // solo tienen que recibirlo los agentes coches que esten andando los otros no.
        
        if(rol=="dron"){
            while(!fin){ //NOSE SI ES ESA LA VARIABLE QUE DICE SI LO HA ENCONTRADO
             
            if(posicionX>x && posicionY>y){
                mover(7);
                actualizarSensores();
            }
            else if (posicionX>x && posicionY<y){
                mover(1);  
                actualizarSensores();
            }
            else if (posicionX<x && posicionY<y){
                mover(3);   
                actualizarSensores();
            }
            else if (posicionX<x && posicionY>y){
                mover(9);  
                actualizarSensores();
            }
            else if (posicionX>x){
                mover(4);
                actualizarSensores();
            }
            else if (posicionX<x){
                mover(6);
                actualizarSensores();
            }
            else if (posicionY>y){
                mover(8);
                actualizarSensores();
            }
            else if (posicionY<y){
                mover(2);
                actualizarSensores();
            }
            
           /*for(int i=0; i<9; i++){
            if(sensor[i]==3){
                objetivo_visto=true;
                fin=true;
                objetivoCumplido();
                //this.ir_a_objetivo();
            }
            }*/
            if(sensor[4]==3){
                objetivo_visto=true;
                fin=true;
                objetivoCumplido();
                //this.ir_a_objetivo();
            }
            /*if(sensor[4]==3)
                fin=true;     */       
            
            }
        }
        
        if(rol=="camion" || rol=="coche"){ //segundo coche
            while(!fin){
            
            if(posicionX>x && posicionY>y){ //objetivo arriba izq
                System.out.println("Da la orde de arriba izq");
                if(!evitarChocarZigzag(7) && esMenor(7))
                        mover(7);
                else if(!evitarChocarZigzag(4) && esMenor(4))
                        mover(4);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                        mover(8);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                        mover(1);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                        mover(9);
                else if(!evitarChocarZigzag(6) && esMenor(6))
                        mover(6);
                actualizarSensores();
            }
            else if (posicionX>x && posicionY<y){ //objetivo abajo izq
                System.out.println("Da la orde de abajo izq");
                if(!evitarChocarZigzag(1) && esMenor(1))
                        mover(1); 
                else if(!evitarChocarZigzag(4) && esMenor(4))
                        mover(4);
                else if(!evitarChocarZigzag(2) && esMenor(2))
                        mover(2);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                        mover(3);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                        mover(7);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                        mover(8);
                actualizarSensores();
            }
            else if (posicionX<x && posicionY<y){ //abajo dcha
                System.out.println("Da la orde de abajo derecha");
                if(!evitarChocarZigzag(3) && esMenor(3))
                        mover(3);   
                else if(!evitarChocarZigzag(6) && esMenor(6))
                        mover(6);
                else if(!evitarChocarZigzag(2) && esMenor(2))
                    mover(2);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                        mover(1);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                        mover(8);
                else if(!evitarChocarZigzag(4) && esMenor(4))
                        mover(4);
                actualizarSensores();
            }
            else if (posicionX<x && posicionY>y){ //arriba dcha
                System.out.println("Da la orde de arriba derecha");
                if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                    mover(8);
                else if(!evitarChocarZigzag(6) && esMenor(6))
                    mover(6);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                    mover(7);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                        mover(3);
                else if(!evitarChocarZigzag(2) && esMenor(2))
                        mover(2);
                else if(!evitarChocarZigzag(4) && esMenor(4))
                        mover(4);
                actualizarSensores();
            }
            else if (posicionX>x){
                System.out.println("Da la orde de izquierda justo");
                if(!evitarChocarZigzag(4) && esMenor(4))
                    mover(4);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                    mover(7);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                    mover(1);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                    mover(8);
                else if(!evitarChocarZigzag(2) && esMenor(2))
                    mover(2);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                    mover(3);
                actualizarSensores();
            }
            else if (posicionX<x){
                System.out.println("Da la orde de derecha justo");
                if(!evitarChocarZigzag(6) && esMenor(6))
                    mover(6);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                    mover(3);
                else if(!evitarChocarZigzag(2) && esMenor(2))
                    mover(2);
                else if(!evitarChocarZigzag(8) && esMenor(8))
                    mover(8);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                    mover(1);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                    mover(7);
                actualizarSensores();
            }
            else if (posicionY>y){
                System.out.println("Da la orde de arriba justo");
                if(!evitarChocarZigzag(8) && esMenor(8))
                    mover(8);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                    mover(7);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(4) && esMenor(4))
                    mover(4);
                else if(!evitarChocarZigzag(6) && esMenor(6))
                    mover(6);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                    mover(3);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                    mover(1);
                actualizarSensores();
            }
            else if (posicionY<y){
                System.out.println("Da la orde de abajo justo");
                if(!evitarChocarZigzag(2) && esMenor(2))
                    mover(2);
                else if(!evitarChocarZigzag(1) && esMenor(1))
                    mover(1);
                else if(!evitarChocarZigzag(3) && esMenor(3))
                    mover(3);
                else if(!evitarChocarZigzag(4) && esMenor(4))
                    mover(4);
                else if(!evitarChocarZigzag(6) && esMenor(6))
                    mover(6);
                else if(!evitarChocarZigzag(9) && esMenor(9))
                    mover(9);
                else if(!evitarChocarZigzag(7) && esMenor(7))
                    mover(7);
                actualizarSensores();
            }
            
            if(rol=="coche"){
               // for(int i=0; i<25 && objetivo_visto==false; i++){
                    if(sensor[12]==3){
                        objetivo_visto=true;
                        fin=true;
                        objetivoCumplido();
                       // this.ir_a_objetivo();
                    }       
                //}
            }
            else{
                //for(int i=0; i<121 && objetivo_visto==false; i++){
                    if(sensor[60]==3){
                        objetivo_visto=true;
                        fin=true;
                        objetivoCumplido();
                    }
                //}
            }
            
            /*
            if(rol=="coche"){
                if(sensor[12]==3)
                fin=true;
                this.objetivoCumplido();
                
            }
            if(rol=="camion"){
                if(sensor[60]==3)
                fin=true;
            }
              */
            
        }
        }
        if(vehiculosEnObjetivo==4){
            System.out.println("Soy el jefe y se acaba la partida "+this.getAid());
            this.enviarMensaje(3);
        }
        
    }
     
    
    /**
     *
     * @author marcos
     */
    private void restaurarVehiculos(){
        vehiculos.clear();
    }
    
    /**
     *
     * @author marcos
     */
    private void comenzar() throws Exception{
        int aux=0;
        if(jefe){
            for(int i=0; i<vehiculos.size() && aux<2; i++){
                Map<String, String> vehiculo = new HashMap<String, String>();
                vehiculo=vehiculos.get(i);
                String agente=vehiculo.toString();
                String [] parts=agente.split("@");
                parts=parts[0].split("//");
                if(parts[1]==vehiculo1 && vehiculo.containsValue("coche")){
                    Map<String, Integer> pos= new HashMap<String, Integer>();
                    pos.put(parts[1], posicionX);
                    posiciones.add(pos);
                    aux++;
                }
                else{
                    if(vehiculo.containsValue("coche")){
                        aux++;
                        ACLMessage outbox= new ACLMessage();
                        outbox.setSender(this.getAid());
                        outbox.addReceiver(new AgentID(parts[1]));
                        outbox.setPerformative(ACLMessage.QUERY_REF);
                        outbox.setContent("dame tu localizacion");
                        this.send(outbox);
                        this.recibirMensaje();
                    }
                }
            }
        }
        else{
            this.recibirMensaje();
        }
    }
    
    /**
     *
     * @author marcos
     */
    private void primeraOrden() throws Exception{
        int x1, x2;
        Map<String, Integer> pos= new HashMap<String, Integer>();
        pos=posiciones.get(0);
        String [] parts=pos.toString().split("=");
        x1=Integer.parseInt(parts[1].substring(0, parts[1].length()-1));
        Map<String, Integer> pos2= new HashMap<String, Integer>();
        pos2=posiciones.get(1);
        String [] parts2=pos2.toString().split("=");
        x2=Integer.parseInt(parts2[1].substring(0, parts2[1].length()-1));
        String agente1=parts[0].substring(1, parts[0].length());
        String agente2=parts2[0].substring(1, parts2[0].length());
        if(x1<x2){
            if(agente1.contains(vehiculo1)){
                izquierda=true;
                explorando=true;
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente2));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la derecha");
                this.send(outbox);
                estrategia();
            }
            if(agente2.contains(vehiculo1)){
                izquierda=false;
                explorando=true;
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente1));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la izquierda");
                this.send(outbox);
                estrategia();
            }
            if(!agente1.contains(vehiculo1) && !agente2.contains(vehiculo1)){
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente1));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la izquierda");
                this.send(outbox);
                outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente2));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la derecha");
                this.send(outbox);
                this.recibirMensaje();
            }
        }
        else{
            if(agente2.contains(vehiculo1)){
                izquierda=true;
                explorando=true;
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente1));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la derecha");
                this.send(outbox);
                estrategia();
            }
            if(agente1.contains(vehiculo1)){
                izquierda=false;
                explorando=true;
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente2));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la izquierda");
                this.send(outbox);
                estrategia();
            }
            if(!agente1.contains(vehiculo1) && !agente2.contains(vehiculo1)){
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente2));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la izquierda");
                this.send(outbox);
                outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                outbox.addReceiver(new AgentID(agente1));
                outbox.setPerformative(ACLMessage.INFORM);
                outbox.setContent("vete a la derecha");
                this.send(outbox);
                this.recibirMensaje();
            }
        }
    }
    
    /**
     *
     * @author marcos
     */
    void informarObjetivo(int posX, int posY) throws Exception{
        String primero=this.getAid().toString(); 
         
            
          ACLMessage outbox= new ACLMessage();
            outbox.setSender(this.getAid());
            JsonObject objeto= new JsonObject();
        
            outbox.setPerformative(ACLMessage.INFORM);
            objeto.add("detener_algoritmo", " ");
            objeto.add("posicionX", posX);
            objeto.add("posicionY", posY);
            outbox.setContent(objeto.toString());
            if(primero.contains(vehiculo1))
            {
                System.out.println("soy" + this.getAid() +"Envio objetivo");
                outbox.addReceiver(new AgentID(vehiculo2));
                outbox.addReceiver(new AgentID(vehiculo3));
                outbox.addReceiver(new AgentID(vehiculo4));
            }
               
            if(primero.contains(vehiculo2))
            {
                System.out.println("soy" + this.getAid() +"Envio objetivo");

                outbox.addReceiver(new AgentID(vehiculo1));
                outbox.addReceiver(new AgentID(vehiculo3));
                outbox.addReceiver(new AgentID(vehiculo4));
            }
            
                     
            if(primero.contains(vehiculo3))
            {
               System.out.println("soy" + this.getAid() +"Envio objetivo");

                outbox.addReceiver(new AgentID(vehiculo2));
                outbox.addReceiver(new AgentID(vehiculo1));
                outbox.addReceiver(new AgentID(vehiculo4));
            }
                           
            if(primero.contains(vehiculo4))
            {
               System.out.println("soy" + this.getAid() +"Envio objetivo");

                outbox.addReceiver(new AgentID(vehiculo2));
                outbox.addReceiver(new AgentID(vehiculo3));
                outbox.addReceiver(new AgentID(vehiculo1));
                  
            }
            this.send(outbox);
            objetivo2=true;
            if(jefe){
                System.out.println("Soy el jefe y estoy en el objetivo"+getAid());
                vehiculosEnObjetivo++;
            }
            else{
                this.objetivoCumplido();
            }
    }
    
    /**
     *
     * @author marcos
     */
    void informarObjetivo2(int posX, int posY) throws Exception{
        String primero=this.getAid().toString(); 
        String [] parts=primero.split("@");
        parts=parts[0].split("//");
        String aux="vehiculo11";
        for(int i=0; i<4; i++){
            if(parts[1]==vehiculo1 || parts[1]==vehiculo2 || parts[1]==vehiculo3 || parts[1]==vehiculo4){

            }
            else{
                ACLMessage outbox= new ACLMessage();
                outbox.setSender(this.getAid());
                JsonObject objeto= new JsonObject();
                outbox.setPerformative(ACLMessage.INFORM);
                objeto.add("detener_algoritmo", " ");
                objeto.add("posicionX", posX-i);
                objeto.add("posicionY", posY);
                outbox.addReceiver(new AgentID(aux+(i+1)));
                outbox.setContent(objeto.toString());
                this.send(outbox);
            }
        }    
        objetivo2=true;
        if(jefe){
            System.out.println("Soy el jefe y estoy en el objetivo"+getAid());
            vehiculosEnObjetivo++;
            this.recibirMensaje();
        }
        else{
            this.objetivoCumplido();
        }
    }
    
    /**
     *
     * @author marcos
     */
    void objetivoCumplido() throws Exception{
        ACLMessage outbox= new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.INFORM);
        outbox.setContent("he llegado al objetivo");
        outbox.addReceiver(new AgentID(vehiculo1));
        this.send(outbox);
        this.recibirMensaje();
    }

    /**
     *
     * @author marcos
     * @author estrella
     * @author david
     * @author juanma
     * @author alex
     */
    boolean evitarChocarZigzag(int aux){
        boolean retorno =false;
        if(rol=="coche"){
            switch(aux){
                case 1: if(sensor[16]==1 || sensor[16]==4 || sensor[16]==2){retorno=true;}break;
                case 2: if(sensor[17]==1 || sensor[17]==4 || sensor[17]==2){retorno=true;}break;
                case 3: if(sensor[18]==1 || sensor[18]==4 || sensor[18]==2){retorno=true;}break;
                case 4: if(sensor[11]==1 || sensor[11]==4 || sensor[11]==2){retorno=true;}break;
                case 6: if(sensor[13]==1 || sensor[13]==4 || sensor[13]==2){retorno=true;}break;
                case 7: if(sensor[6]==1 || sensor[6]==4 || sensor[6]==2){retorno=true;}break;
                case 8: if(sensor[7]==1 || sensor[7]==4 || sensor[7]==2){retorno=true;}break;
                case 9: if(sensor[8]==1 || sensor[8]==2 || sensor[7]==2){retorno=true;}break;
            }
        }
        if(rol=="camion"){
            switch(aux){
                case 1: if(sensor[70]==1 || sensor[70]==4 || sensor[70]==2){retorno=true;}break;
                case 2: if(sensor[71]==1 || sensor[71]==4 || sensor[71]==2){retorno=true;}break;
                case 3: if(sensor[72]==1 || sensor[72]==4 || sensor[72]==2){retorno=true;}break;
                case 4: if(sensor[59]==1 || sensor[59]==4 || sensor[59]==2){retorno=true;}break;
                case 6: if(sensor[61]==1 || sensor[61]==4 || sensor[61]==2){retorno=true;}break;
                case 7: if(sensor[48]==1 || sensor[48]==4 || sensor[48]==2){retorno=true;}break;
                case 8: if(sensor[49]==1 || sensor[49]==4 || sensor[49]==2){retorno=true;}break;
                case 9: if(sensor[50]==1 || sensor[50]==2 || sensor[50]==2){retorno=true;}break;
            }
        }
        
        return retorno;
    }
    // Cuando terminar de ejecutar execute muere el agente y se ejecuta finalize
    // public void finalize();
    /**
     *
     * @author marcos
     * @author alex
     * @author estrella
     * @author david
     */
    int [] menores(){
        int menor=100;
        int[] menores=new int [8];
        for(int i=0; i<8; i++){
            menores[i]=-1;
        }
        int b=0;
        for(int i=posicionY+1; i>=posicionY-1; i--){
            for(int j=posicionX-1; j<=posicionX+1; j++ ){
                b++;
                if(b==5){
                    b++;
                }
                if(evitarChocarZigzag(b)){
                    memoria[j][i]=999;
                }
            }
        }
        for(int i=posicionY+1; i>=posicionY-1; i--){
            for(int j=posicionX-1; j<=posicionX+1; j++ ){
                if(i>=0 && j>=0){
                    if(memoria[j][i]<menor){
                        menor=memoria[j][i];
                    }
                }
                
            }
        }
        int aux=0;
        for(int i=posicionY+1; i>=posicionY-1; i--){
            for(int j=posicionX-1; j<=posicionX+1; j++ ){
                if(memoria[j][i]==menor){
                    if(posicionX-1==j && posicionY+1==i){
                        menores[aux]=1;
                        aux++;
                    }
                    if(posicionX==j && posicionY+1==i){
                        menores[aux]=2;
                        aux++;
                    }
                    if(posicionX+1==j && posicionY+1==i){
                        menores[aux]=3;
                        aux++;
                    }
                    if(posicionX+1==j && posicionY==i){
                        menores[aux]=6;
                        aux++;
                    }
                    if(posicionX-1==j && posicionY==i){
                        menores[aux]=4;
                        aux++;
                    }
                    if(posicionX-1==j && posicionY-1==i){
                        menores[aux]=7;
                        aux++;
                    }
                    if(posicionX==j && posicionY-1==i){
                        menores[aux]=8;
                        aux++;
                    }
                    if(posicionX+1==j && posicionY-1==i){
                        menores[aux]=9;
                        aux++;
                    }
                }
                
            }
        }
        return menores;
    }
    /**
     *
     * @author marcos
     * @author alex
     * @author estrella
     * @author david
     */
    boolean esMenor(int a){
        int []menores=new int [8];
        menores=this.menores();
        for(int i=0; i<8; i++){
            if(a==menores[i]){
                return true;
            }
        }
        return false;
    }
}
