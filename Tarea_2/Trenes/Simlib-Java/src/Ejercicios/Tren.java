package Ejercicios;
import static simlib.SimLib.*;
import static simlib.elements.Facility.*;

import simlib.elements.Facility;

public class Tren {
	public int id;
	public int cambio_trip;
	public boolean sin_Trip;
	public float tiempo_descarga;
	public float fin_cambio;
	public float ini_cambio;
	
	public Tren(int i) {
		id = i;
		cambio_trip = 0;
		sin_Trip = false;
		tiempo_descarga = -1;
		fin_cambio = -1; //momento especifico de la simulacion en que termina de cambiar la simulacion
		ini_cambio = unifrm(6, 11, 4)+simTime; //momento especifico de la simulacion en que debe haber un cambio de tripulacion
	}

	
}
