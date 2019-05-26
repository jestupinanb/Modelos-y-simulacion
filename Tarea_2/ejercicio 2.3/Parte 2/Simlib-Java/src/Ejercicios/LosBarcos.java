package Ejercicios;

import static simlib.SimLib.*;


import java.io.IOException;
import java.util.Comparator;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class LosBarcos {
	static final byte EVENT_ARRIVAL = 1, EVENT_REPAIR_1 = 2, EVENT_REPAIR_2 = 3 ,EVENT_ATTEND_1 = 4, EVENT_ATTEND_2= 5;
	static final byte TYPE_TV = 1, TYPE_VCR = 2;
	static final byte STREAM_ARRIVAL = 1, STREAM_REPAIR_TV = 2, STREAM_REPAIR_VCR = 3, STREAM_ATTEND_1 = 4, STREAM_ATTEND_2 = 4;
	
	static final int EVENTS_NUMBER = 5;
	static  int  equipos_recogidos,reparaciones_pendientes_tv, reparaciones_pendientes_vcr, reparaciones_TV_final,reparaciones_VCR_final;
	static float minarrival,maxarrival,  meanrepairtv, varrepairtv,meanrepairvcr,varrepairvcr,meanattend,lengthSimulation,tiempo_reparando;


	static SimReader reader;
	static SimWriter writer;

	static Queue<Float> queue;
	static Facility Server, Server_2;

	public static void main(String[] args) throws IOException {

		queue = new Queue<Float>("Cola de los clientes");

		reader = new SimReader("barcos.in");
		writer = new SimWriter("barcos.out");

		minarrival = reader.readFloat();
		maxarrival = reader.readFloat();
		meanrepairtv = reader.readFloat();
		varrepairtv=reader.readFloat();
		meanrepairvcr = reader.readFloat();
		varrepairvcr=reader.readFloat();
		meanattend = reader.readFloat();
		lengthSimulation = reader.readFloat();

		Server = new Facility("Server", true);
		Server_2 = new Facility("Server_2", true);

        /* Write report heading and input parameters */
        writer.write("Modelo de Tienda de reparaciones de TV y VCR \n\n" +
        
                "Mean Arrival minimo     " + minarrival +"	maximo	 "+maxarrival +"\n" +
                "Mean repair TV  " + meanrepairtv + "	Var repair TV	"+varrepairtv+"\n" +
                "Mean repair VCR  " + meanrepairvcr + "	Var repair VCR	"+varrepairvcr+"\n" +
                "Max Attend  " + meanattend + "\n" +
                "Number of hours processed " + lengthSimulation + "\n\n"
        );
		
		initSimlib();
		initSimlib_b();

		/* Evitando que la lista de eventos tenga menos de 3 elementos */
		eventSchedule(Double.MAX_VALUE, (byte) 1);
		eventSchedule(Double.MAX_VALUE, (byte) 2);

		/** star of simulation **/
		eventSchedule(0, EVENT_REPAIR_1);
		eventSchedule(0, EVENT_REPAIR_2);
		eventSchedule(unifrm(minarrival, maxarrival, STREAM_ARRIVAL), EVENT_ARRIVAL);																			
		timing();
		
		while (simTime < lengthSimulation) {//Simulacion durante length simulation
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival();
				break;
			case EVENT_REPAIR_1:
				repair(EVENT_REPAIR_1);
			case EVENT_REPAIR_2:
				repair(EVENT_REPAIR_2);
				break;
			case EVENT_ATTEND_1:
				attend(EVENT_ATTEND_1);
				break;
			case EVENT_ATTEND_2:
				attend(EVENT_ATTEND_2);
				break;
			}
			timing();
		}
        report();
		
        writer.close();
        reader.close();
	}

	public static void arrival() {
		System.out.println(simTime+"\n");
		if( simTime< 480) {
		eventSchedule(simTime + unifrm(minarrival, maxarrival, STREAM_ARRIVAL), EVENT_ARRIVAL);
		}
		if (Server.isBussy() && Server_2.isBussy() ) {
			queue.offer(simTime);
			System.out.println("size "+ queue.size()+"\n");
		} else {
			if(Server.isBussy()) {
				System.out.println("atendio 2 \n");
			attend(EVENT_ATTEND_2);
		}else {
			System.out.println("atendio 1 \n");
			attend(EVENT_ATTEND_1);
		}
		}
	}
	public static void attend(byte tipo) {
		if(tipo==EVENT_ATTEND_1) {
			if(Server.isBussy())Server.setIdle();
			if(!queue.isEmpty()) {
			queue.poll();}
			byte ServerDesocupada;
				if( pro(STREAM_ARRIVAL) ) {//Recoger un elemento
					Server.setBussy();
					float tiempo = expon(meanattend, STREAM_ATTEND_1);
					//tiempo_reparando+= tiempo;
					//equipos_recogidos++;
					System.out.println("recogio \n");
					eventSchedule(simTime +tiempo, EVENT_ATTEND_1 );
				
				}else {
					System.out.println("reparo \n");
					if(pro(STREAM_ARRIVAL) ) {//Reparar un tv
					
						ServerDesocupada = TYPE_TV;
						}else {//reparar un VCR
						ServerDesocupada = TYPE_VCR;
					
					}ocuparSever_1(ServerDesocupada);
				
			}
		}else {
			if(Server_2.isBussy())Server_2.setIdle();
			if(!queue.isEmpty()) {
			queue.poll();}
			byte ServerDesocupada;
			if( pro(STREAM_ARRIVAL) ) {//Recoger un elemento
				Server_2.setBussy();
				float tiempo = expon(meanattend, STREAM_ATTEND_2);
				//tiempo_reparando+= tiempo;
				//equipos_recogidos++;
				eventSchedule(simTime +tiempo, EVENT_ATTEND_2 );
				
			}else {
				if(pro(STREAM_ARRIVAL) ) {//Reparar un tv
					
					ServerDesocupada = TYPE_TV;
				}else {//reparar un VCR
					ServerDesocupada = TYPE_VCR;
					
				}ocuparSever_2(ServerDesocupada);
			}
		
	}	
	} 
		
		
	
	public static void repair(byte tipo) {
		//System.out.print(simTime+"\n");
		if(tipo==EVENT_REPAIR_1) {
			if(reparaciones_pendientes_tv>0) {
				reparaciones_pendientes_tv--;
				ocuparSever_1 (TYPE_TV);
			}else {
			if(reparaciones_pendientes_vcr>0) {
				reparaciones_pendientes_vcr--;
				ocuparSever_1 (TYPE_VCR);
				
			}else {
				if (!queue.isEmpty()) {
					attend(EVENT_ATTEND_1);
				} else {if(Server.isBussy()) {Server.setIdle();}}
			}
		}
	}else {
		if(reparaciones_pendientes_tv>0) {
			reparaciones_pendientes_tv--;
			ocuparSever_2 (TYPE_TV);
		}else {
		if(reparaciones_pendientes_vcr>0) {
			reparaciones_pendientes_vcr--;
			ocuparSever_2 (TYPE_VCR);
			
		}else {
			if (!queue.isEmpty()) {
				attend(EVENT_ATTEND_2);
			} else {if(Server_2.isBussy()) {Server_2.setIdle();}}
		}
	}
	}
	}
	public static void ocuparSever_1 (byte gruaDesocupada) {
		if(Server.isBussy())Server.setIdle();
		if (gruaDesocupada == TYPE_TV) {
			Server.setBussy();
			double tiempo = normal(meanrepairtv,varrepairtv,STREAM_REPAIR_TV);
			tiempo_reparando+= tiempo;
			reparaciones_TV_final++;
			eventSchedule(simTime +tiempo , EVENT_REPAIR_1);
		} else {
			Server.setBussy();
			double tiempo = normal(meanrepairvcr,varrepairvcr,STREAM_REPAIR_VCR);
			tiempo_reparando+= tiempo;
			reparaciones_VCR_final++;
			eventSchedule(simTime + tiempo, EVENT_REPAIR_1);
		}
		
	}
	public static void ocuparSever_2 (byte gruaDesocupada) {
		if(Server_2.isBussy()) Server_2.setIdle();
		if (gruaDesocupada == TYPE_TV) {
			Server_2.setBussy();
			double tiempo = normal(meanrepairtv,varrepairtv,STREAM_REPAIR_TV);
			tiempo_reparando+= tiempo;
			reparaciones_TV_final++;
			eventSchedule(simTime +tiempo , EVENT_REPAIR_2);
		} else {
			Server_2.setBussy();
			double tiempo = normal(meanrepairvcr,varrepairvcr,STREAM_REPAIR_VCR);
			tiempo_reparando+= tiempo;
			reparaciones_VCR_final++;
			eventSchedule(simTime + tiempo, EVENT_REPAIR_2);
		}
		
	}

	
	public static void report() throws IOException {
		
		Server.report(writer);
		Server_2.report(writer);
		writer.write(completeLine(completeHalfLine("*  Numero de Tv  " + reparaciones_TV_final) + "  Numero de VCR  = " + reparaciones_VCR_final)+'\n');
		writer.write(completeLine(completeHalfLine("*  Tiempo de reparacion  " + tiempo_reparando) )+'\n');
		writer.write(completeLine(completeHalfLine("*  Equipos recogidos    " + equipos_recogidos) )+'\n');
		queue.report(writer); 
		
		
	}
	
	public static String completeHalfLine(String line){
		while (line.length()<30){
			line += " ";
		}
		return line;
	}
	
	public static String completeLine(String line){
		while (line.length()<59){
			line += " ";
		}
		return line + "*\n";
	}
	
	public static void initSimlib_b(){
		reparaciones_pendientes_tv= 2;
		reparaciones_pendientes_vcr= 2;
		reparaciones_TV_final=0;
		reparaciones_VCR_final=0;
		tiempo_reparando = 0 ;
		equipos_recogidos=0;
	}
}
