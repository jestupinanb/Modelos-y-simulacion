package simlib.elements;

import Ejercicios.LosTrenes.Tren;
import simlib.exception.OutOfRangeException;

public class Event implements Comparable<Event>{
    private byte eventType;
    private float time;
    private Tren tren;

    public Event(byte type, float time, Tren tren) {
        this.eventType = type;
        this.time = time;
        this.tren = tren;
    }

    public Event(byte type, int time, Tren tren) {
        this(type, (double)time, tren);
    }

    public Event(byte type, double time, Tren tren) {
        this(type, (float)time, tren);
    }

    public Event(byte type, float time) {
        this.eventType = type;
        this.time = time;
    }
    
    public Event(byte type,double time) {
    	this(type,(float)time);
    }

    public byte getType() {
        return this.eventType;
    }

    public float getTime(){
        return this.time;
    }

    /*
    public float getAttribute( int index ){
        if( index >= attributes.length )
            throw new OutOfRangeException( "Event", attributes.length, index );
        return attributes[index];
    }

    public float[] getAttributes(){
        return attributes;
    }
    */
    
    public Tren getTren() {
    	return tren;
    }
    
    @Override
    public int compareTo(Event event) {
        return (this.time < event.getTime()) ? -1 : ((this.time == event.getTime()) ? 0 : 1);
    }

    public String toString(){
        return eventType+" "+time;
    }

}