package simlib.elements;

import static simlib.SimLib.*;

import java.io.IOException;

import simlib.io.SimWriter;

public class Facility extends DiscreteStat {

	private boolean isBusy;
	private float lastSimTime;

	public Facility(String name, boolean isBusy) {
		super(name);
		this.isBusy = isBusy;
		lastSimTime = 0;
	}

	private boolean replace(boolean element) {
		update();
		boolean value = this.isBusy;
		this.isBusy = element;
		return value;
	}

	public boolean isIdle() {
		return this.isBusy == false;
	}

	public boolean isBusy() {
		return this.isBusy != false;
	}

	void update() {
		if (isBusy) {
			record((simTime - lastSimTime));
		}
		lastSimTime = simTime;
	}

	@Override
	public void report(SimWriter out) throws IOException {
		if (isBusy) {
			setIdle();
		}
		out.write("************************************************************\n");
		out.write(completeLine("*  FACILITY STADISTIC " + getName()));
		out.write("************************************************************\n");
		out.write(completeLine(completeHalfLine("*  Min = " + getMin()) + "  Max = " + getMax()));
//		out.write(completeLine("*  Records = " + getNumObs()));
		out.write(completeLine("*  Utilization average = " + getAverage()));
		out.write(completeLine("*  Porcentaje de tiempo ocupado  = " + getPercentage()));
		out.write(completeLine("*  Porcentaje de tiempo libre    = " + (1-getPercentage())));
		
	}
	
	public float getPercentage() {
		return getSum()/simTime;
	}
	
	public boolean setBusy() {
		try {
			if (isBusy) {
				Exception e = new Exception("No se puede ocupar el servidor: "+getName()+", ya que actualmente esta ocupado (Busy). Esto afectaria y dañaria las estadisticas.");
				throw e;
			}
		} catch (Exception excepcion) {
			excepcion.printStackTrace();
		}
		return replace(true);
	}

	public boolean setIdle() {
		try {
			if (!isBusy) {
				Exception e = new Exception("No se puede desocupar el servidor:"+getName()+",ya que actualmente esta desocupado (Idle). Esto afectaria y dañaria las estadisticas.");
				throw e;
			}
		} catch (Exception excepcion) {
			excepcion.printStackTrace();
		}
		return replace(false);
	}

}