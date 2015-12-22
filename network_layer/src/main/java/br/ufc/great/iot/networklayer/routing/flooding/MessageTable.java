package br.ufc.great.iot.networklayer.routing.flooding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import android.os.Handler;
import br.ufc.great.iot.networklayer.routing.exception.DuplicatedMessageException;
import br.ufc.great.iot.networklayer.routing.flooding.message.FloodingMessage;

public class MessageTable implements Runnable{

	public static final int DEFAULT_TABLE_SIZE = 20;
	public static final int DEFAULT_PURGE_TIME = 10000;
	private final int MESSAGE_TABLE_LIMIT;
	private final int PURGE_TIME; 
	private long lastAddedEntry;
	private Queue<MessageEntry> messages;
	private Handler handler;
	
	public MessageTable(int maxTableSize, int purgeTime)
	{
		MESSAGE_TABLE_LIMIT = maxTableSize;
		PURGE_TIME = purgeTime;
		messages = new LinkedList<MessageEntry>();
		handler = new Handler();
		handler.postDelayed(this, PURGE_TIME);
	}
	
	public boolean addMessage(FloodingMessage message) throws DuplicatedMessageException
	{

		// ALTERADO = message / messages
		synchronized (messages) {
			MessageEntry newEntry = new MessageEntry(message.getSourceAddress(),
					message.getSequenceNumber());
			
			lastAddedEntry = newEntry.getTimeStamp();
			
			if(messages.contains(newEntry))
			{
				throw new DuplicatedMessageException(message.getSourceAddress(),
						message.getSequenceNumber());
			}
			
			if(messages.size() > MESSAGE_TABLE_LIMIT)
			{
				messages.remove();
				
			}
			
			return messages.add(newEntry);
		}
					
	}

	@Override
	public void run() {
		purge();
		handler.postDelayed(this, PURGE_TIME);
	}

	private void purge() {
		Iterator<MessageEntry> it = messages.iterator();
		ArrayList<MessageEntry> olderEntries = new ArrayList<MessageEntry>(messages.size());
		
		while(it.hasNext())
		{
			MessageEntry entry = it.next();
			if(entry.getTimeStamp() < (lastAddedEntry - PURGE_TIME))
			{
				olderEntries.add(entry);
			}
			else
			{
				break;
			}
				
		}
		
		synchronized (messages) {
			messages.removeAll(olderEntries);
		}
		
/*		if (BuildConfig.DEBUG) {
			Log.d(this.getClass().getName(), "Purging message table ");
			Log.d(this.getClass().getName(), olderEntries.size() + " entries removed at"
			+ System.currentTimeMillis());
		}*/
	}
}
