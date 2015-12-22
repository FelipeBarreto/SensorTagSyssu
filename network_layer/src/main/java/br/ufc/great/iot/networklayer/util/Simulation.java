package br.ufc.great.iot.networklayer.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import android.os.Environment;
import br.ufc.great.iot.networklayer.routing.message.RoutingMessage;

public class Simulation {

	private final String PATH = Environment.getExternalStorageDirectory().getPath() + "/simulation.txt";
	private static FileWriter logFile = null;
	
	private static Simulation singleton;
	public static HashMap<String, String> somcDevices = new HashMap<String, String>();
	
	private Simulation()
	{
		somcDevices.put("B8:F9:34:F4:4F:BD", "perninha");
		somcDevices.put("40:2B:A1:97:A5:54", "pluckduck");
		
		somcDevices.put("B4:62:93:3B:6A:BC", "presuntinho");
		somcDevices.put("B4:62:93:3B:6A:B2", "frajuto");
		somcDevices.put("B4:62:93:3B:6A:AC", "coiote_coio");
		somcDevices.put("04:FE:31:3E:83:00", "galaxy_tab2");
		
		somcDevices.put("84:00:D2:0B:8D:21", "xperia_sola");
		somcDevices.put("B8:F9:34:EF:D6:7B", "xperia_play");
		somcDevices.put("84:00:D2:66:F7:C2", "Lt26i");
		somcDevices.put("40:2B:A1:97:A5:62", "roycorroi");
		somcDevices.put("90:C1:15:66:D4:6D", "Lt29i");
		somcDevices.put("B4:62:93:3B:6A:AE", "estrangeiro");
		somcDevices.put("04:FE:31:3F:AF:A0", "galaxy_tab22");
		
		somcDevices.put("broadcast", "broadcast");
		somcDevices.put("broadcast_excluding_source", "broadcast_excluding_source");	
		try {
			logFile = new FileWriter(PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Simulation getInstance()
	{
		if(singleton == null)
		{
			singleton = new Simulation();
		}
		return singleton;
	}
	
	public void log(RoutingMessage message, boolean sending)
	{
		StringBuffer log = new StringBuffer();		
		if(sending) log.append("S "); 
		else log.append("R "); 
		log.append(" " +System.currentTimeMillis());
		
		log.append(message.toString());
		log.append("\n");
		try {
			logFile.append(log);
			logFile.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
