

package test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.omg.CORBA.ORB;

public class SimpleStringClient
{
	public static void main (String [] args) throws IOException
	{
		// Initialisation de l'ORB
	    ORB orb = ORB.init(args, null);

	    // Récupération de la référence du servant
	    BufferedReader fileReader = new BufferedReader(new FileReader("ObjectRef"));
	    String stringIOR = fileReader.readLine();
	    fileReader.close();

	    // Création, à partir de la référence du servant, d'un proxy local
	    SimpleSring str = SimpleSringHelper.narrow(orb.string_to_object(stringIOR));
		System.out.println (str.getString());
	}
}
