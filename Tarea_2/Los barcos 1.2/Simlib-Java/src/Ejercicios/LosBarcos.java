package Ejercicios;

import static simlib.SimLib.*;

import java.io.IOException;
import java.util.Comparator;

import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class LosBarcos {
	static final byte EVENT_ARRIVAL = 1, EVENT_DEPARTURE_1 = 2, EVENT_DEPARTURE_2 = 3;
	static final byte STREAM_INTERARRIVAL = 1, STREAM_DEPARTURE = 2;
	static final boolean MULTIPLICAR = true, DIVIDIR = false;
	static final int EVENTS_NUMBER = 3;

	static float meanInterarrival, minDeparture, maxDeparture, lengthSimulation;


	static SimReader reader;
	static SimWriter writer;

	static Queue<Float> queue;
	static Facility grua_1;
	static Facility grua_2;

	public static void main(String[] args) throws IOException {

		queue = new Queue<Float>("Cola de los barcos");

		reader = new SimReader("barcos.in");
		writer = new SimWriter("barcos.out");

		meanInterarrival = reader.readFloat();
		minDeparture = reader.readFloat();
		maxDeparture = reader.readFloat();
		lengthSimulation = reader.readFloat();

		grua_1 = new Facility("Grua 1", false);
		grua_2 = new Facility("Grua 2", false);

        /* Write report heading and input parameters */
        writer.write("Modelo de barcos en un puerto con un muelles 2 amarres y 2 gruas \n\n" +
                "Mean Arrival     " + meanInterarrival + "\n" +
                "Min Unload ship  " + minDeparture + "\n" +
                "Max Unload ship  " + maxDeparture + "\n" +
                "Number of days processed " + lengthSimulation + "\n\n"
        );
		
		initSimlib();

		/* Evitando que la lista de eventos tenga menos de 3 elementos */
		eventSchedule(Double.MAX_VALUE, (byte) 1);
		eventSchedule(Double.MAX_VALUE, (byte) 2);
		eventSchedule(Double.MAX_VALUE, (byte) 3);

		/** Incio de la simulacion **/

		eventSchedule(unifrm(minDeparture, maxDeparture, STREAM_INTERARRIVAL), EVENT_ARRIVAL);// Primer elemento de
																							// llegada
		timing();
		
		
		while (simTime < lengthSimulation) {//Simulacion durante length simulation
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival();
				break;
			case EVENT_DEPARTURE_1:
				descarge_barco(EVENT_DEPARTURE_1);
				break;
			case EVENT_DEPARTURE_2:
				descarge_barco(EVENT_DEPARTURE_2);
				break;
			}
			timing();
		}
        report();
		
        writer.close();
        reader.close();
	}

	public static void arrival() {
		eventSchedule(simTime + expon(meanInterarrival, STREAM_INTERARRIVAL), EVENT_ARRIVAL);
		byte gruaDesocupada;
		if (grua_1.isBussy() && grua_2.isBussy()) {
			queue.offer(simTime);
		} else {
			gruaDesocupada = EVENT_DEPARTURE_1;// Si ninguna grua esta ocupada por defecto esta dosocupada la primera
			if (grua_1.isBussy() || grua_2.isBussy()) {
				if (grua_1.isBussy()) {
					gruaDesocupada = EVENT_DEPARTURE_2;
					nuevoTiempo(EVENT_DEPARTURE_1, MULTIPLICAR, simTime);
				} else {
					gruaDesocupada = EVENT_DEPARTURE_1;
					nuevoTiempo(EVENT_DEPARTURE_2, MULTIPLICAR, simTime);
				}
			}
			ocuparGruaDesocupada(gruaDesocupada);
		}

	}

	public static void descarge_barco(byte event_departure) {
		Facility grua = event_departure == EVENT_DEPARTURE_1 ? grua_1 : grua_2;
		grua.setIdle();
		if (!queue.isEmpty()) {
			queue.poll();
			ocuparGruaDesocupada(event_departure);
		} else {
			if (grua_1.isBussy() || grua_2.isBussy()) {
				if (grua_1.isBussy()) {
					nuevoTiempo(EVENT_DEPARTURE_1, DIVIDIR, simTime);
				} else {
					nuevoTiempo(EVENT_DEPARTURE_2, DIVIDIR, simTime);
				}
			}
		}
	}

	public static void ocuparGruaDesocupada(byte gruaDesocupada) {
		if (gruaDesocupada == EVENT_DEPARTURE_1) {
			grua_1.setBussy();
		} else {
			grua_2.setBussy();
		}
		eventSchedule(simTime + unifrm(minDeparture, maxDeparture, STREAM_DEPARTURE), gruaDesocupada);
	}

	public static void nuevoTiempo(byte type, boolean operacion, float simTimeActual) {
		int actualEventType = eventType;
		float simActual = simTime;
		int[] eventsTypeTemp = new int[EVENTS_NUMBER];
		float[] simTimeTemp = new float[EVENTS_NUMBER];
		for (int i = 0; i < EVENTS_NUMBER; i++) {
			timing();
			eventsTypeTemp[i] = eventType;
			simTimeTemp[i] = simTime;
		}
		for (int i = 0; i < EVENTS_NUMBER; i++) {
			if (eventsTypeTemp[i] == type) {
				simTimeTemp[i] = operacion ? (simTimeTemp[i] - simTimeActual) + simTimeTemp[i]
						: (simTimeTemp[i] - simTimeActual) / 2 + simTimeActual;
			}
		}
		for (int i = 0; i < EVENTS_NUMBER; i++) {
			eventSchedule(simTimeTemp[i], (byte) eventsTypeTemp[i]);
		}
		eventSchedule(simActual, (byte)actualEventType);
		timing();
	}

	public static void report() throws IOException {
		
		grua_1.report(writer);
		grua_2.report(writer);
		
		writer.write("************************************************************\n");
		writer.write(completeLine("*  Tiempo de los puertos en general"));
		writer.write("************************************************************\n");
		writer.write(completeLine(completeHalfLine("*  Min = " + Float.min(grua_1.getMin(), grua_2.getMin()) ) + "  Max = " + Float.max(grua_1.getMax(), grua_2.getMax())));
		writer.write(completeLine("*  Records = " + (grua_1.getNumObs()+grua_2.getNumObs())));
		writer.write(completeLine("*  Utilization average = " + (grua_1.getSum()+grua_2.getSum())/(grua_1.getNumObs()+grua_2.getNumObs())));
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
