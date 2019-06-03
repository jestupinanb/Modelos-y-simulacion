package Ejercicios;

import static simlib.SimLib.*;
import static simlib.elements.Facility.*;
import static java.util.ArrayList.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class CAJAS {
	static final byte EVENT_ARRIVAL = 1, EVENT_DEPARTURE= 2;
	static final byte STREAM_INTERARRIVAL_A = 1, STREAM_INTERARRIVAL_B = 2, STREAM_INTERARRIVAL_C = 3, STREAM_DEPARTURE = 4;
	static final int EVENTS_NUMBER = 2;

	static float meanInterarrivalA,meanInterarrivalB, meanInterarrivalC, lengthSimulation ;
	static final float elevadorSube = 1, elevadorDescarga = 2, elevadorBaja =1, tiempoenServidor = 4;
	static float meanTransitoCajaA, meanEsperaCajaB, numeroCajasA, numeroCajasB, numeroCajasCen1Hora;


	static SimReader reader;
	static SimWriter writer;
	static SimWriter pruebas;


	static Queue<Paquete> queue;
	static ArrayList<Paquete> paquetes;
	static int i;
	static Facility sistema_descarga;

	public static void main(String[] args) throws IOException {
		float [] p = {0,0,(float) 0.33,(float)0.67};
		queue = new Queue<Paquete>("Cola de los paquetes");
		paquetes = new ArrayList<Paquete>();
		i = 0;
		
		meanTransitoCajaA = 0;
		meanEsperaCajaB = 0;
		numeroCajasA = 0;
		numeroCajasB = 0;
		numeroCajasCen1Hora = 0;
		
		

		reader = new SimReader("cajas.in");
		writer = new SimWriter("cajas.out");

		meanInterarrivalA = reader.readFloat();
		meanInterarrivalB = reader.readFloat();
		meanInterarrivalC = reader.readFloat();
		lengthSimulation = reader.readFloat();

		sistema_descarga = new Facility("Sistema de descarga", false);

        /* Write report heading and input parameters */
        writer.write("Modelo de cajas en ascensor \n\n" +
                //"Mean Arrival     " + meanInterarrivalA + "\n" +
             //   "Min Unload ship  " + minDeparture + "\n" +
             //   "Max Unload ship  " + maxDeparture + "\n" +
                "Number of minutes processed " + lengthSimulation + "\n\n"
        );
		
		initSimlib();

		/** Incio de la simulacion **/

		eventSchedule(unifrm(meanInterarrivalA-2, meanInterarrivalA+2, STREAM_INTERARRIVAL_A), EVENT_ARRIVAL, 1);// llega caja tipo A
		eventSchedule(meanInterarrivalB, EVENT_ARRIVAL, 2);// llega caja tipo B
		//eventSchedule(expon(meanInterarrivalC, STREAM_INTERARRIVAL_C), EVENT_ARRIVAL, 3);// llega caja tipo C
		eventSchedule(simTime+ irandi(3,p, STREAM_INTERARRIVAL_C), EVENT_ARRIVAL, 3);// llega caja tipo C
		timing();
		

		while (simTime < lengthSimulation) {//Simulacion durante length simulation			
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival((int)eventAttributes[0]);
				break;
			case EVENT_DEPARTURE:
				departure();
				break;
			}
			timing();
		}
        report();
		
        writer.close();
        reader.close();
	}

	
	public static void arrival(int tipoCaja) {
		//eventSchedule(simTime + expon(meanInterarrival, STREAM_INTERARRIVAL), EVENT_ARRIVAL);
		Caja caja = null; 
		switch(tipoCaja) {
		case 1: 
			eventSchedule(simTime + unifrm(meanInterarrivalA-2, meanInterarrivalA+2, STREAM_INTERARRIVAL_A), EVENT_ARRIVAL, 1);// llega caja tipo A
			caja = new Caja ("A",200,simTime);
			//System.out.println("LLEGA CAJA A");
			break;
		case 2:
			eventSchedule(simTime + meanInterarrivalB, EVENT_ARRIVAL, 2);// llega caja tipo B
			caja = new Caja ("B",100,simTime);
			//System.out.println("LLEGA CAJA B");
			break;
		case 3:
			//eventSchedule(simTime+ expon(meanInterarrivalC, STREAM_INTERARRIVAL_C), EVENT_ARRIVAL, 3);// llega caja tipo C
			float [] p = {0,0,(float) 0.33,(float)0.67};
			eventSchedule(simTime+ irandi(3,p, STREAM_INTERARRIVAL_C), EVENT_ARRIVAL, 3);// llega caja tipo C
			caja = new Caja ("C",50,simTime);
			//System.out.println("LLEGA CAJA C");
			break;
		}
		
		
		
		if(paquetes.isEmpty()) {
			///System.out.println("PAQUETES ESTA VACIO");
			Paquete paquete = new Paquete();
			paquete.agregarCaja(caja);
			paquetes.add(paquete);
		}else {
			int k = 0;
			boolean ac = false;
			while(paquetes.size()>k) {
				if (paquetes.get(k).peso+caja.peso==400) {
					///System.out.println("PASA A LA COLA");
					paquetes.get(k).agregarCaja(caja);
					i++;
					queue.offer(paquetes.remove(k));
					ac = true;
					
					
					////////////////////////////////////// SE VERIFICA SI EL SERVIDOR ESTA OCUPADO
					
					if(!sistema_descarga.isBussy()) {
						sistema_descarga.setBussy();
						queue.poll();
						eventSchedule(simTime+tiempoenServidor, EVENT_DEPARTURE);
					}
					break;
				}else if(paquetes.get(k).peso+caja.peso<400) {
					paquetes.get(k).agregarCaja(caja);
					ac = true;
					break;	
				}
				k++;
			}
			
			if(!ac) {
				///System.out.println("NO CABE EN LOS QUE HAY, CREA NUEVO PAQUETE");
				Paquete paquete = new Paquete();
				paquete.agregarCaja(caja);
				paquetes.add(paquete);
			}
		}
		//System.out.println("TAM: " + queue.size());
	}
	
	
	
	public static void departure(){
		sistema_descarga.setIdle();
		
		if(!queue.isEmpty()) {
			sistema_descarga.setBussy();
			Paquete p = queue.poll();
			eventSchedule(simTime+tiempoenServidor, EVENT_DEPARTURE);
			
			//////// PARA LAS ESTADISTICAS
			int k = 0;
			Caja c; 
			while(k<p.cantidadCajas()) {
				c=p.getCaja(k);
				switch(c.tipo) {
				case "A":
					meanTransitoCajaA += (simTime-c.llegada+elevadorSube+elevadorDescarga);
					numeroCajasA++;
					break;
				case "B":
					meanEsperaCajaB += (simTime-c.llegada+tiempoenServidor);
					numeroCajasB++;
//					break;
				case "C":
						numeroCajasCen1Hora++;
					break;
				
				}
				
				k++;
			}
		}
	}
	
	
	
	
	
		public static void report() throws IOException {
		
		sistema_descarga.report(writer);
		
		writer.write("************************************************************\n");
		writer.write(completeLine("*  Tiempo de los puertos en general"));
		writer.write("************************************************************\n");
		writer.write(completeLine(completeHalfLine("\n* Promedio transito caja A: " + meanTransitoCajaA/numeroCajasA )));
		writer.write(completeLine(completeHalfLine("\n* Promedio de espera caja B: " + meanEsperaCajaB/numeroCajasB )));
		writer.write(completeLine(completeHalfLine("\n* Numero de cajas C en una hora: " + (int)(numeroCajasCen1Hora )/8)));
		//writer.write(completeLine("*  Records = " + (sistema_descarga.getNumObs())));
		writer.write(completeLine("*  Utilization average = " + sistema_descarga.getSum()/sistema_descarga.getNumObs()));
		writer.write("************************************************************\n\n");
		
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
	
}

