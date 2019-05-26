package Ejercicios;

import static simlib.SimLib.*;


import java.io.IOException;
import java.util.Comparator;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class LosBarcos {
	static final byte EVENT_ARRIVAL = 1, EVENT_REPAIR = 2, EVENT_ATTEND = 3;
	static final byte TYPE_TV = 1, TYPE_VCR = 2;
	static final byte STREAM_ARRIVAL = 1, STREAM_REPAIR_TV = 2, STREAM_REPAIR_VCR = 3, STREAM_ATTEND = 4;
	
	static final int EVENTS_NUMBER = 3;
	static  int  reparaciones_pendientes_tv, reparaciones_pendientes_vcr, reparaciones_TV_final,reparaciones_VCR_final;
	static float minarrival,maxarrival,  meanrepairtv, varrepairtv,meanrepairvcr,varrepairvcr,meanattend,lengthSimulation,tiempo_reparando;


	static SimReader reader;
	static SimWriter writer;

	static Queue<Float> queue;
	static Facility Server;

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
		eventSchedule(0, EVENT_REPAIR);
		
		eventSchedule(unifrm(minarrival, maxarrival, STREAM_ARRIVAL), EVENT_ARRIVAL);																			
		timing();
		
		while (simTime < lengthSimulation) {//Simulacion durante length simulation
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival();
				break;
			case EVENT_REPAIR:
				repair();
				break;
			case EVENT_ATTEND:
				attend();
				break;
			}
			timing();
		}
        report();
		
        writer.close();
        reader.close();
	}

	public static void arrival() {
		if( simTime< 480) {
		eventSchedule(simTime + unifrm(minarrival, maxarrival, STREAM_ARRIVAL), EVENT_ARRIVAL);
		}
		if (Server.isBussy() ) {
			queue.offer(simTime);
			
		} else {
			
			attend();
		}
	}
	public static void attend() {
		if(Server.isBussy())Server.setIdle();
		if(!queue.isEmpty()) {
		queue.poll();}
		byte ServerDesocupada;
		if( pro(STREAM_ARRIVAL) ) {//Recoger un elemento
			Server.setBussy();
			float tiempo = expon(meanattend, STREAM_ATTEND);
			tiempo_reparando+= tiempo;
			eventSchedule(simTime +tiempo, EVENT_ATTEND );
			
		}else {
			if(pro(STREAM_ARRIVAL) ) {//Reparar un tv
				
				ServerDesocupada = TYPE_TV;
			}else {//reparar un VCR
				ServerDesocupada = TYPE_VCR;
				
			}ocuparSever(ServerDesocupada);
		}
		
	} 
		
		
	
	public static void repair() {
		//System.out.print(simTime+"\n");
		
		if(reparaciones_pendientes_tv>0) {
			reparaciones_pendientes_tv--;
			ocuparSever (TYPE_TV);
	}else {
		if(reparaciones_pendientes_vcr>0) {
			reparaciones_pendientes_vcr--;
			ocuparSever (TYPE_VCR);
			
		}else {
			if (!queue.isEmpty()) {
				attend();
			} 
		}
	}
	}
	public static void ocuparSever (byte gruaDesocupada) {
		if(Server.isBussy())Server.setIdle();
		if (gruaDesocupada == TYPE_TV) {
			Server.setBussy();
			double tiempo = normal(meanrepairtv,varrepairtv,STREAM_REPAIR_TV);
			tiempo_reparando+= tiempo;
			reparaciones_TV_final++;
			eventSchedule(simTime +tiempo , EVENT_REPAIR);
		} else {
			Server.setBussy();
			double tiempo = normal(meanrepairvcr,varrepairvcr,STREAM_REPAIR_VCR);
			tiempo_reparando+= tiempo;
			reparaciones_VCR_final++;
			eventSchedule(simTime + tiempo, EVENT_REPAIR);
		}
		
	}

	
	public static void report() throws IOException {
		
		Server.report(writer);
		writer.write(completeLine(completeHalfLine("*  Numero de Tv  " + reparaciones_TV_final) + "  Numero de VCR  = " + reparaciones_VCR_final)+'\n');
		writer.write(completeLine(completeHalfLine("*  Tiempo de reparacion  " + tiempo_reparando) )+'\n');
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
		
	}
}
