/*
 * Created on 28 févr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.berlios.caobap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.InvalidName;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManagerPackage.AdapterInactive;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.WrongPolicy;

import slb.iop.IOP;
import slb.iop.IOPEvent;
import slb.iop.IOPListener;
import slb.iop.SmartCard;
import slb.iop.slbException;
import de.berlios.caobap.servants.CAOBAPServant;
import de.berlios.caobap.servants.GenericServant;

/**
 * @author Administrateur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class COA implements IOPListener{
	private SmartCard smartCard=new SmartCard();
	private final IOP sIOP;
	private final String readerName;
	private boolean isConnected;
	private final List<CAOBAPServant> servants = new ArrayList<CAOBAPServant>();
	
	private POA delegatePOA ;
	public COA(IOP sIOP,String readerName,POA rootPOA,ORB orb) throws AdapterAlreadyExists, InvalidPolicy, AdapterInactive, ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy, ObjectNotActive, IOException{
		this.sIOP=sIOP;
		this.readerName=readerName;
		cardConnected();
		//TODO lire les applets sur la carte et creer un servant pour chacune
		
		delegatePOA= rootPOA;
		
		short[]aid = new short[]{0x01,0x02,0x03,0x04,0x05,0x06};
		GenericServant genSrv =new GenericServant(0x85,aid,smartCard);
		
		genSrv.loadConfig(null,new File("simpleString.ins"));
		servants.add(genSrv);
		
		byte [] oid	=delegatePOA.activate_object( genSrv);
		saveIOR("objectRef",orb.object_to_string(delegatePOA.id_to_reference(oid)));
	}
	
	public void saveIOR(String fileName,String ior) throws FileNotFoundException{
		PrintWriter file = new PrintWriter(fileName);
	    file.println(ior);
	    file.close();	
	}
	public void saveIOR(NamingContextExt naming,String nameStr,org.omg.CORBA.Object servantRef) throws InvalidName, NotFound, CannotProceed{
		 NameComponent[] name = naming.to_name(nameStr); 
		try {
		 
		 naming.bind(name,servantRef);
	    } catch (AlreadyBound e) {
		  naming.rebind(name,servantRef);
	    }
	}
	
	public void CardRemoved(IOPEvent e) {
		if(readerName.equals(e.getReaderName())){
			System.out.println("C R");
		
			isConnected=false;
		}
	}
	
	public void CardInserted(IOPEvent e) {
		if(readerName.equals(e.getReaderName())){
			System.out.println("C I");
			cardConnected();
		}
		
		
	}
	public void cardConnected(){
		if (sIOP.Connect(smartCard, readerName, false)) {
			System.out.println ("Carte connectee sur"+readerName);
			isConnected=true;
			establishSecureChanel();
			System.out.println("ok");
			
			
		}else{
			System.out.println("Carte non connctée sur"+readerName);
			isConnected=false;
		}
	}
	public void establishSecureChanel(){
		
		//TODO mettre les cle ds le fichier de conf...
		try {
			System.out.println ("Transaction EstablishSecureChannel");				
			smartCard.BeginTransaction();
			short[] MACKey;
			short[] autKey;
			short[] KEKKey;
			MACKey = stringToShortArray("404142434445464748494A4B4C4D4E4F");
			autKey = stringToShortArray("404142434445464748494A4B4C4D4E4F");
			KEKKey = stringToShortArray("404142434445464748494A4B4C4D4E4F");
			boolean connected = smartCard.EstablishSecureChannel(MACKey, autKey, KEKKey);
			if (connected) {
				System.out.println ("Canal securise cree");
			}
			smartCard.EndTransaction();
		}
		catch(slb.iop.slbException e){	System.out.println ("Pb d ouverture de canal");}
	}
	public void exemple(){
		if(!isConnected)return ;
		
		try {
			smartCard.BeginTransaction();
			short[]id = new short[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07};
			short[]aid = new short[]{0x01,0x02,0x03,0x04,0x05,0x06};
			String aidStr = "01020304050607";
			try{
				boolean res =smartCard.CreateInstance(id,aid,500,new short[0]);
				System.out.println (res);
			} catch (slbException e) {}
			
			boolean res =smartCard.SelectAID(aid);
			System.out.println (res);
			
			int CLA,INS,P1,P2,LE;
			int body[]=null;
			short out[]=null;
			CLA = (int) Integer.parseInt("85", 16);
			INS = (int) Integer.parseInt("20", 16);
			P1 = (int) Integer.parseInt("00", 16);
			P2 = (int) Integer.parseInt("00", 16);
			body=new int[0];
			LE = 49;
					
			
			out = smartCard.SendCardAPDU(CLA,INS,P1,P2,body,LE);
			for(int i=0;i<out.length;i++){
				System.out.print ((char)out[i]);
			}
			System.out.println ();
			
			CLA = (int) Integer.parseInt("85", 16);
			INS = (int) Integer.parseInt("10", 16);
			P1 = (int) Integer.parseInt("00", 16);
			P2 = (int) Integer.parseInt("00", 16);
			byte [] tmp ="toto".getBytes();
			body=new int[tmp.length];
			for(int i=0;i<tmp.length;i++){
				body[i]=tmp[i];
			}
			LE = (int) Integer.parseInt("00", 16);
					

			out = smartCard.SendCardAPDU(CLA,INS,P1,P2,body,LE);
			
			for(int i=0;i<out.length;i++){
				System.out.println (Integer.toHexString(out[i]));
			}
				
			
			smartCard.EndTransaction();
			


		} catch (slbException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static short[] stringToShortArray(String value) {
		short shortKey[] = new short[(value.length() / 2)];
		int y = 0;
		String strShort;

		for (int x = 0; x < shortKey.length; x++) {
			strShort = value.substring(y, (y + 2));
			if (strShort.equals("FF")) {
				shortKey[x] = (short) 0xFF;
			} else {
				try {
					shortKey[x] = (short) Short.parseShort(strShort, 16);
				} catch (NumberFormatException e) {
					System.out.println("HexStringToShortArray Failed ");
				}
			}
			y = y + 2;
		}
		return shortKey;
	}
//outil stringToIntArray
	public static int[] stringToIntArray(String value) {
		int intKey[] = new int[(value.length() / 2)];
		int y = 0;
		String strInt;

		for (int x = 0; x < intKey.length; x++) {
			strInt = value.substring(y, (y + 2));
			if (strInt.equals("FF")) {
				intKey[x] = (int) 0xFF;
			} else {
				try {
					intKey[x] = (int) Integer.parseInt(strInt, 16);
				} catch (NumberFormatException e) {
					System.out.println("stringToShortArray Failed ");
				}
			}

			y = y + 2;
		}
		return intKey;
	}
	
	public void ReaderRemoved(IOPEvent e) {}
	
	public void ReaderInserted(IOPEvent e) {}
}
