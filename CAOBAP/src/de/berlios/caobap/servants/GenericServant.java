/*
 * Created on 13 mars 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.berlios.caobap.servants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.PortableServer.POA;

import slb.iop.SmartCard;
import slb.iop.slbException;

/**
 * @author Administrateur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GenericServant extends CAOBAPServant {

	private static final int P1=0x00;
	private static final int P2=0x00;
	
	//TODO Taille dynamique
	private static final int LE=0x02;
	private final Map<String,Integer> inss = new HashMap<String,Integer>();
	private final int cla;
	private final short[] aid;
	private final SmartCard smartCard;
	public GenericServant(int cla,short[]aid,SmartCard smartCard){
		this.cla=cla;
		this.aid=aid;
		this.smartCard=smartCard;
	}
	public void loadConfig(File idlFile, File insFile) throws IOException{
		//TODO charger le fichier idl pour extraire les type de retour et le compliler pour utilisation static
		
		BufferedReader reader = new BufferedReader(new FileReader(insFile));
		while(reader.ready()){
			String line =reader.readLine();
			String lines[]= line.split(":");
			if(lines.length<2)throw new RuntimeException("Ins file not valid");
			try{
				inss.put(lines[0],Integer.parseInt(lines[1],16));
			}catch (NumberFormatException e){
				throw new RuntimeException("Ins file not valid",e);
			}
		}
		
	}
	
	//TODO mettre un nom unique..
	private static String[] __ids = {"IDL:test/SimpleSring:1.0"};
	public String[] _all_interfaces(POA poa, byte[] objectId) {
		return (String[])__ids.clone ();
	}
	
	public OutputStream _invoke(String method, InputStream input, ResponseHandler handler) throws SystemException {
		Integer ins = inss.get(method);
		
		OutputStream out = handler.createReply ();
		if(ins==null)
			return out;
		
		
		
		//TODO fonction avec arguments
		//TODO les autres types de retour possible
		try
		{
			smartCard.BeginTransaction();
			smartCard.SelectAID(aid);
			short []res = smartCard.SendCardAPDU(cla,ins,P1,P2,new int[]{},LE);
			smartCard.EndTransaction();
			String str = "";
			for(int i=0;i<res.length;i++){
				str+=res[i];
			}
			out.write_string(str);
		}
		catch (slbException e)
		{
			e.printStackTrace();
		}
		
		
		
		return out;
	}

}
