package Ejercicios;

import static simlib.SimLib.*;
import static simlib.elements.Facility.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.sun.xml.internal.ws.api.pipe.NextAction;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class Trenes {
	static final byte EVENT_ARRIVAL = 1, EVENT_INI_CAMBIO_TRIPULACION = 2, EVENT_FIN_CAMBIO_TRIPULACION = 3, EVENT_DESCARGA = 4;
	static final byte STREAM_INTERARRIVAL = 1, STREAM_CAMBIO_TRIPULACION = 2,STREAM_DESCARGA = 3;
	static final boolean MULTIPLICAR = true, DIVIDIR = false;
	static final int EVENTS_NUMBER = 4;

	static float meanInterarrival, minDescarga, maxDescarga, minCambio, maxCambio, minTiempoTrip, maxTiempoTrip, lengthSimulation ;


	static SimReader reader;
	static SimWriter writer;
	static SimWriter pruebas;

	static Queue<Tren> queue;
	static int i;
	static Facility sistema_descarga;

	public static void main(String[] args) throws IOException {
		
//		queue = new Queue<Tren>("Cola de los trenes");
//		
//		Tren t1 = new Tren (1);
//		Tren t2 = new Tren (2);
//		
//		queue.offer(t1);
//		queue.offer(t2);
//		
//		Tren temp = queue.get(1);
//		
//		temp.id = 3;
//		
//		while(!queue.isEmpty()) {
//			System.out.println(queue.poll().id);
//		}
		
		
		queue = new Queue<Tren>("Cola de los trenes");
		i = 0;
		reader = new SimReader("barcos.in");
		writer = new SimWriter("barcos.out");
		pruebas = new SimWriter("pruebas.out");

		meanInterarrival = reader.readFloat();
		minDescarga = reader.readFloat();
		maxDescarga = reader.readFloat();
		minCambio = reader.readFloat();
		maxCambio = reader.readFloat();
		minTiempoTrip = reader.readFloat();
		maxTiempoTrip = reader.readFloat();
		lengthSimulation = reader.readFloat();

		sistema_descarga = new Facility("Sistema de descarga", false);

        /* Write report heading and input parameters */
        writer.write("Modelo de trenes carboneros en sistema de descarga con un servidor \n\n" +
                "Mean Arrival     " + meanInterarrival + "\n" +
             //   "Min Unload ship  " + minDeparture + "\n" +
             //   "Max Unload ship  " + maxDeparture + "\n" +
                "Number of days processed " + lengthSimulation + "\n\n"
        );
		
		initSimlib();
		
		eventSchedule(Double.MAX_VALUE, EVENT_ARRIVAL);// Primer elemento de
		/** Incio de la simulacion **/

		eventSchedule(expon(meanInterarrival, STREAM_INTERARRIVAL), EVENT_ARRIVAL);// Primer elemento de
																							// llegada
		timing();
		

		while (simTime < 20) {//Simulacion durante length simulation
	///System.out.print("\nnext type   " + eventType + " simtime " + simTime);
			System.out.println("SIMTIME: " + simTime);
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival();
				break;
			case EVENT_INI_CAMBIO_TRIPULACION:
				ini_cambio_tripulacion((int)eventAttributes[0]);
				break;
			case EVENT_FIN_CAMBIO_TRIPULACION:
				fin_cambio_tripulacion((int)eventAttributes[0]);
				break;
			case EVENT_DESCARGA:
				descarga();
				break;
			}
			System.out.println("    TAM COLA: " + queue.size());
			timing();
		}
        report();
		
        writer.close();
        reader.close();
	}

	public static void arrival() {
		eventSchedule(simTime + expon(meanInterarrival, STREAM_INTERARRIVAL), EVENT_ARRIVAL);
		Tren tren = new Tren(i);
		System.out.println("ARRIVAL ID: " + tren.id);
		
		/*
		if(sistema_descarga.isBussy()) {
			eventSchedule(tren.ini_cambio, EVENT_INI_CAMBIO_TRIPULACION, i);
			queue.offer(tren);
		}else {
			float D = unifrm(minDescarga, maxDescarga, STREAM_DESCARGA);     //D = TIEMPO DE DESCARGA
			sistema_descarga.setBussy(); 
			if(tren.ini_cambio-simTime>=D) { 
				eventSchedule(simTime+D, EVENT_DESCARGA);
				tren.tiempo_descarga = D; ////////////////////////////////
			}else {
				tren.tiempo_descarga= D-(tren.ini_cambio-simTime);
				eventSchedule(tren.ini_cambio,EVENT_DESCARGA);
			}
		}
		*/
		queue.offer(tren);
		eventSchedule(tren.ini_cambio, EVENT_INI_CAMBIO_TRIPULACION, i);
		System.out.println("INICIO CAMBIO: " + tren.ini_cambio);
		if(!sistema_descarga.isBussy()) {
			eventSchedule(simTime,EVENT_DESCARGA);
		}
		i++;
	}
	
	public static void ini_cambio_tripulacion(int tren) {
		
		
		Tren t = buscarTren(tren);
		System.out.println("INI_CAMBIO ID: " + t.id);
		t.sin_Trip=true;
		t.cambio_trip++;
		
		float fin_cambio = unifrm(minCambio, maxCambio, STREAM_CAMBIO_TRIPULACION);
		t.fin_cambio=simTime+fin_cambio;
		eventSchedule(t.fin_cambio, EVENT_FIN_CAMBIO_TRIPULACION, tren);
		
		
	}
	
	public static void fin_cambio_tripulacion(int tren) {
		Tren t = buscarTren(tren);
		System.out.println("FIN_CAMBIO ID: " + t.id);
		t.sin_Trip=false;
		t.ini_cambio=12+simTime;
		
		eventSchedule(t.ini_cambio, EVENT_INI_CAMBIO_TRIPULACION, tren);
	}
	
	public static void descarga() {
		
		if(!queue.isEmpty()){
			Tren tren = queue.peek();
			
			if(tren.sin_Trip) {
				eventSchedule(tren.fin_cambio, EVENT_DESCARGA);
				System.out.println("DESCARGA SIN TRIP ID: " + tren.id);
				if(tren.tiempo_descarga>0) {
					sistema_descarga.setBussy();
				}
			}else {
				if(tren.tiempo_descarga>0) {
					System.out.println("DESCARGA SALE ID: " + tren.id);
					eventSchedule(simTime,EVENT_DESCARGA);
					removeEventType(tren.id);
					queue.poll();
					sistema_descarga.setIdle();
				}else {
					float D = unifrm(minDescarga, maxDescarga, STREAM_DESCARGA);     //D = TIEMPO DE DESCARGA
					if(tren.ini_cambio >= simTime+D) {
						System.out.println("DESCARGA ALCANZA (SE AGENDA PARA SALIR) ID: " + tren.id);
						sistema_descarga.setBussy();
						eventSchedule(simTime+D,EVENT_DESCARGA);
						tren.tiempo_descarga = D;
					}else {
						System.out.println("DESCARGA NO ALCANZA (DEBE ENTRAR A DESCARGA SIN TRIP ID: " + tren.id);
						tren.tiempo_descarga= D-(tren.ini_cambio-simTime);
						eventSchedule(tren.ini_cambio,EVENT_DESCARGA);
						System.out.println("SE AGENDO DESCARGA EN: " + tren.ini_cambio);
					}
				}
				
			}
		}
	}
	
	
	public static Tren buscarTren(int tren) {
		int id;
		for (int ac =0; ac<queue.size();ac++) {
			id = queue.get(ac).id;
			id = (int)id;
			if(id==tren)
				return queue.get(ac);
		}
		return null;
	}
	
	
	
	
		public static void report() throws IOException {
		
		sistema_descarga.report(writer);
		
		writer.write("************************************************************\n");
		writer.write(completeLine("*  Tiempo de los puertos en general"));
		writer.write("************************************************************\n");
		writer.write(completeLine(completeHalfLine("\n*  Min = " + sistema_descarga.getMin()  + "  Max = " + sistema_descarga.getMax())));
		writer.write(completeLine("*  Records = " + (sistema_descarga.getNumObs())));
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

