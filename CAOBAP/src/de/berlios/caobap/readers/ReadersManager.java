/*
 * Created on 28 févr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package de.berlios.caobap.readers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
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
import de.berlios.caobap.COA;

/**
 * @author Administrateur
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReadersManager {
	private final static Map <String,COA> coas = new HashMap<String,COA>();
	
	
	
	private final static IOP sIOP=new IOP();
	
	public static void main(String[] args) throws AdapterInactive, AdapterAlreadyExists, InvalidPolicy, ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy, InvalidName, ObjectNotActive, IOException {
		final ORB orb  = ORB.init (args, null);
		final POA rootPOA = POAHelper.narrow (orb.resolve_initial_references ("RootPOA"));
		
			
		String[] listReaders=sIOP.ListReaders();
		System.out.println(listReaders.length);			
		for (int i=0;i<listReaders.length;i++){		
			System.out.println(listReaders[i]);
			COA coa = new COA(sIOP,listReaders[i],rootPOA,orb);
			sIOP.addIOPListener(coa);
			coas.put(listReaders[i],coa);
		}
		sIOP.addIOPListener(new IOPListener()
		{
		
			public void CardRemoved(IOPEvent e) {}

			public void CardInserted(IOPEvent arg0) {}

			public void ReaderRemoved(IOPEvent e) {
				String readerName = e.getReaderName();
				sIOP.removeIOPListener(coas.get(readerName));
				coas.remove(readerName);
			}

			public void ReaderInserted(IOPEvent e) {
				try{
					String readerName = e.getReaderName();
					System.out.println(readerName);
					COA coa = new COA(sIOP,readerName,rootPOA,orb);
					sIOP.addIOPListener(coa);
					coas.put(readerName,coa);
				}catch (Exception ex){
					ex.printStackTrace();
				}
			}
		
		});
		
		rootPOA.the_POAManager ().activate ();
		orb.run ();
	}
	

	

}
