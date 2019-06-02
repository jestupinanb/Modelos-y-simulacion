package Ejercicios;

import simlib.collection.ArrayList;

public class Paquete {
	public int peso;
	public ArrayList<Caja> cajas;
	
	public Paquete() {
		this.peso = 0;
		this.cajas = new ArrayList<Caja>("ArrayList de cajas");
	}
	
	public void agregarCaja(Caja caja) {
		cajas.add(caja);
		peso += caja.peso;
	}

	public int cantidadCajas() {
		return cajas.size();
	}
	
	public Caja getCaja(int index) {
		return cajas.get(index);
	}
	

}
