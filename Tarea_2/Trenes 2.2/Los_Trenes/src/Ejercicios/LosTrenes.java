package Ejercicios;

import static simlib.SimLib.*;

import java.io.IOException;
import simlib.io.*;
import simlib.elements.*;
import simlib.collection.*;

public class LosTrenes {
	static final byte EVENT_ARRIVAL = 1, EVENT_DESCARGUE = 2, EVENT_SALIDA_DE_TRIPULACION = 3,
			EVENT_LLEGADA_TRIPULACION = 4;

	static final byte STREAM_INTERARRIVAL = 1, STREAM_DESCARGUE = 2, STREAM_ARRIVAL_SALIDA_TRIPULACION = 3,
			STREAM_LLEGADA = 4;

	static float meanInterarrival, minDescargue, maxDescargue, minArrivalSalidaTripulacion, maxArrivalSalidaTripulacion,
			minLlegadaTripulacion, maxLlegadaTripulacion, lengthSimulation;

	static float timeWorking;

	static SimReader reader;
	static SimWriter writer;

	static QueueStats<Tren> queue;
	static Servidor server;

	public static void main(String[] args) throws IOException {

		queue = new QueueStats<Tren>("Cola de los trenes");

		reader = new SimReader("trenes.in");
		writer = new SimWriter("trenes.out");

		meanInterarrival = reader.readFloat();
		minDescargue = reader.readFloat();
		maxDescargue = reader.readFloat();
		timeWorking = reader.readFloat();
		minArrivalSalidaTripulacion = reader.readFloat();
		maxArrivalSalidaTripulacion = reader.readFloat();
		minLlegadaTripulacion = reader.readFloat();
		maxLlegadaTripulacion = reader.readFloat();

		lengthSimulation = reader.readFloat();

		server = new Servidor("Lugar de descarga.", false);

		/* Write report heading and input parameters */
		writer.write("Modelo de trenes carboneros con un lugar de descarga y \"hogging out\" \n\n" + "Mean Arrival   "
				+ meanInterarrival + "\n" + "Min Descargue  " + minDescargue + "\n" + "Max Descargue  " + maxDescargue
				+ "\n" + "Tiempo de trabajo de la triuplacion  " + timeWorking + "\n"
				+ "Min tiempo de trabajo en llegada del tren al sistema  " + minArrivalSalidaTripulacion + "\n"
				+ "Max tiempo de trabajo en llegada del tren al sistema  " + maxArrivalSalidaTripulacion + "\n"
				+ "Min tiempo de llegada de una nueva tripulacion  " + minLlegadaTripulacion + "\n"
				+ "Max tiempo de llegada de una nueva tripulacion  " + maxLlegadaTripulacion + "\n"
				+ "Number of hours processed " + lengthSimulation + "\n\n");

		initSimlib();

		eventSchedule(distributionArrival(), EVENT_ARRIVAL);

		timing();

		while (simTime < lengthSimulation) {
			switch (eventType) {
			case EVENT_ARRIVAL:
				arrival();
				break;
			case EVENT_DESCARGUE:
				descargue();
				break;
			case EVENT_SALIDA_DE_TRIPULACION:
				salidaDeTripulacion();
				break;
			case EVENT_LLEGADA_TRIPULACION:
				llegadaDeTripulacion();
				break;
			}
			timing();
			if (simTren != null && simTren.getId() == 39229) {
				System.out.println("");
			}
		}

		report();

		writer.close();
		reader.close();

	}

	public static void report() throws IOException {
		server.report(writer);
		Tren.report(writer);
		queue.report(writer, simTime);

	}

	public static void arrival() {
		eventSchedule(distributionArrival() + simTime, EVENT_ARRIVAL);
		Tren tren = new Tren(distributionArrivalSalidaTripulacion(), simTime);
		eventSchedule(tren.getSalidaTripulacion(), EVENT_SALIDA_DE_TRIPULACION, tren);
		if (!queue.isEmpty() || server.isBusy()) {
			queue.offer(tren, simTime);
		} else {
			server.setBusy(distributionDescargue(), tren);
			puedeDescargueTren();
		}
	}

	public static void descargue() {
		if (simTren.getTieneTripulacion()) {
			server.setIdle(simTime);
			if (!queue.isEmpty() && queue.peek().getTieneTripulacion()) {
				server.setBusy(distributionDescargue(), queue.poll(simTime));
				puedeDescargueTren();
			}
		}
	}

	public static void salidaDeTripulacion() {
		if (server.isBusy() && simTren.getId() == server.lastTrenId) {
			server.unmanned(simTime);
		}
		simTren.setSinTripulacion();
		if (trenEnSistema(simTren)) {
			eventSchedule(distributionLlegadaTripulacion() + simTime, EVENT_LLEGADA_TRIPULACION, simTren);
		}
	}

	public static void llegadaDeTripulacion() {

		if (server.isBusy() && simTren.getId() == server.lastTrenId) {
			server.withCrew(simTime);
		}

		simTren.salidaTripulacion(timeWorking, simTime);
		eventSchedule(simTren.getSalidaTripulacion(), EVENT_SALIDA_DE_TRIPULACION, simTren);
		try {
			if (server.isIdle() && simTren.getId() == queue.peek().getId()) {
				server.setBusy(distributionDescargue(), queue.poll(simTime));
			}
		} catch (Exception e) {
			System.out.println("Hay un error jejeje");
		}
		if (server.isBusy() && simTren.getId() == server.getlastTrenIdInServer()) {
			puedeDescargueTren();
		}
	}

	public static void puedeDescargueTren() {
		Tren tren = server.getTren();
		float valSalidaTripulacion = tren.getSalidaTripulacion() - simTime;
		if (valSalidaTripulacion < server.getValDescargue()) {
			server.setValDescargue(server.getValDescargue() - valSalidaTripulacion);
		} else {
			float descargueTren = server.getValDescargue() + simTime;
			eventSchedule(descargueTren, EVENT_DESCARGUE, tren);
			server.setValDescargue(0);
		}
	}

	public static boolean trenEnSistema(Tren tren) {
		boolean enSistema;
		if (tren.getId() > server.getlastTrenIdInServer()
				|| (tren.getId() == server.getlastTrenIdInServer() && server.isBusy())) {
			enSistema = true;
		} else {
			enSistema = false;
		}
		return enSistema;
	}

	public static float distributionArrival() {
		return expon(meanInterarrival, STREAM_INTERARRIVAL);
	}

	public static float distributionDescargue() {
		return unifrm(minDescargue, maxDescargue, STREAM_DESCARGUE);
	}

	public static float distributionArrivalSalidaTripulacion() {
		return unifrm(minArrivalSalidaTripulacion, maxArrivalSalidaTripulacion, STREAM_ARRIVAL_SALIDA_TRIPULACION);
	}

	public static float distributionLlegadaTripulacion() {
		return unifrm(minLlegadaTripulacion, maxLlegadaTripulacion, STREAM_LLEGADA);
	}

	public static String completeHalfLine(String line) {
		while (line.length() < 30) {
			line += " ";
		}
		return line;
	}

	public static String completeLine(String line) {
		while (line.length() < 59) {
			line += " ";
		}
		return line + "*\n";
	}

	public static class Servidor {
		private int lastTrenId;
		private Facility serverStatus;
		private float valDescargue;
		private Tren tren;

		private float unmannedTime;
		private float lastUpdate;

		public Servidor(String name, boolean isBusy) {
			this.lastTrenId = -1;
			this.serverStatus = new Facility(name, isBusy);
			this.unmannedTime = 0;
			this.lastUpdate = 0;
		}

		public int getlastTrenIdInServer() {
			return this.lastTrenId;
		}

		public boolean isBusy() {
			return this.serverStatus.isBusy();
		}

		public boolean isIdle() {
			return this.serverStatus.isIdle();
		}

		public boolean setBusy(float valDescargue, Tren tren) {
			this.lastTrenId++;
			this.valDescargue = valDescargue;
			this.tren = tren;
			try {
				if (tren.id != lastTrenId) {
					Exception e = new Exception("Ha desaparecido un tren. Los trenes deben entrar en orden.");
					throw e;
				}
			} catch (Exception excepcion) {
				excepcion.printStackTrace();
			}
			return this.serverStatus.setBusy();
		}

		public boolean setIdle(float simTime) {
			try {
				if (valDescargue != 0) {
					Exception e = new Exception("Solo deberian salir los trenes cuando su tiempo de salida es 0.");
					throw e;
				}
			} catch (Exception excepcion) {
				excepcion.printStackTrace();
			}
			this.tren.deathIsComing(simTime);
			this.tren = null;
			return this.serverStatus.setIdle();
		}

		public float getValDescargue() {
			return valDescargue;
		}

		public void setValDescargue(float valDescargue) {
			this.valDescargue = valDescargue;
		}

		public Tren getTren() {
			return tren;
		}

		public void report(SimWriter out) throws IOException {
			serverStatus.report(out);
			out.write(serverStatus
					.completeLine("*  Porcentaje de tiempo sin tripulacion    = " + unmannedTime / simTime));
			out.write("************************************************************\n\n");
		}

		public void unmanned(float simTime) {
			if (lastUpdate > 0) {
				Exception e = new Exception("LastUpdate deberia ser menor que 0");
				e.printStackTrace();
			}
			lastUpdate = simTime;
		}

		public void withCrew(float simTime) {
			if (lastUpdate <= 0 || simTime - lastUpdate < 0) {
				Exception e = new Exception("lastUpdate >=0 o simTime-lastUpdate < 0");
				e.printStackTrace();
			}
			unmannedTime += simTime - lastUpdate;
			lastUpdate = 0;
		}

	}

	public static class Tren {
		private static int idGenerator;
		private int id;
		private float valSalidaTripulacion;
		private float salidaTripulacion;
		private boolean tieneTripulacion;

		private float birthdate;
		private int numUnmanned;

		private static DiscreteStat stats = new DiscreteStat("De los Trenes en horas.");
		private static int unmannedCero = 0;
		private static int unmannedOne = 0;
		private static int unmannedTwo = 0;
		private static int numDead = 0;

		public Tren(float valSalidaTripulacion, float simTime) {
			this.id = getIdGenerator();
			Tren.updateIdGenerator();
			this.valSalidaTripulacion = valSalidaTripulacion;
			setConTripulacion();
			this.salidaTripulacion = this.valSalidaTripulacion + simTime;
			this.birthdate = simTime;
			this.numUnmanned = 0;
		}

		public boolean getTieneTripulacion() {
			return tieneTripulacion;
		}

		public void setSinTripulacion() {
			numUnmanned++;
			this.valSalidaTripulacion = 0;
			this.salidaTripulacion = 0;
			this.tieneTripulacion = false;
		}

		private void setConTripulacion() {
			this.tieneTripulacion = true;
		}

		public int getId() {
			return id;
		}

		public float getSalidaTripulacion() {
			return salidaTripulacion;
		}

		public void salidaTripulacion(float valSalidaTripulacion, float simTime) {
			setConTripulacion();
			this.valSalidaTripulacion = valSalidaTripulacion;
			this.salidaTripulacion = this.valSalidaTripulacion + simTime;
		}

		public static int getIdGenerator() {
			return idGenerator;
		}

		public static void updateIdGenerator() {
			Tren.idGenerator++;
		}

		public void deathIsComing(float simTime) {
			stats.record(simTime - birthdate);
			numDead++;
			switch (numUnmanned) {
			case 0:
				unmannedCero++;
				break;
			case 1:
				unmannedOne++;
				break;
			case 2:
				unmannedTwo++;
				break;
			default:
				break;
			}
		}

		public static void report(SimWriter out) throws IOException {
			stats.report(out);
			out.write("************************************************************\n");
			out.write(completeLine("*  Proporcion trenes sin tripulacion"));
			out.write("************************************************************\n");
			out.write(completeLine("*  Sin tripulacion 0 veces: " + unmannedCero));
			out.write(completeLine("*  Sin tripulacion 1 vez:   " + unmannedOne));
			out.write(completeLine("*  Sin tripulacion 2 veces: " + unmannedTwo));
			out.write(completeLine("*  Cantidad de trenes que han salido: " + numDead));
			out.write("************************************************************\n\n");
		}
	}

	public static class QueueStats<E> {
		private Queue<E> queue;
		private DiscreteStat stats;
		private float lastSimTime;
		private int maxInQueue;

		public QueueStats(String text) {
			this.queue = new Queue<E>(text);
			this.stats = new DiscreteStat("De la " + text.toLowerCase() + ".");
		}

		public void offer(E object, float simTime) {
			updateStats(simTime);
			queue.offer(object);
		}

		public E poll(float simTime) {
			updateStats(simTime);
			return queue.poll();
		}

		public void updateStats(float simTime) {
			if (maxInQueue < queue.size()) {
				maxInQueue = queue.size();
			}
			stats.record((simTime - lastSimTime) * queue.size());
			lastSimTime = simTime;
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}

		public E peek() {
			return queue.peek();
		}

		public void report(SimWriter out, float simTime) throws IOException {
			updateStats(simTime);

			out.write("************************************************************\n");
			out.write(this.stats.completeLine("*  DISCRETE STATISTIC " + stats.getName()));
			out.write("************************************************************\n");
			out.write(this.stats.completeLine(completeHalfLine("*  Min = " + 0) + "  Max = " + maxInQueue));
			out.write(this.stats.completeLine("*  Average = " + this.stats.getAverage()));
			out.write("************************************************************\n\n");
		}

	}
}
