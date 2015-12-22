package br.ufc.great.iot.networklayer.routing.message;

public abstract class AbstractMessage {

	public String getType()
	{
		return this.getClass().getCanonicalName();
	}
}
