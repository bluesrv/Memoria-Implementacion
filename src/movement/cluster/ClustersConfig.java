/*
 *
 * Author: Erika Rosas
 * Modificacion de archivos de Felipe y Orlando para obtener los centros del movimiento
 *
 *
 */


package movement.cluster;

import core.*;
import java.util.*;
import java.io.*;



public class ClustersConfig{

	public static Map<String, List<Coord>> nodes;
	
	static boolean inited = false;

	public static Map<String, List<Coord>> getCenters(){
		if (!inited){
			init();
		}FEnergia
		return nodes;
	}


	static void init(){

		nodes = new Hashtable<>();
		nodes.put("centros", parseFile("./escenario1_4.txt"));
//		nodes.put("centros", parseFile("./escenario2_8.txt"));



	}

	public static List<Coord> parseFile(String file){
		List<Coord> all = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))){
			String line;
			while ((line = br.readLine()) != null){
				String[] coords = line.split(",");
				double x = new Double(coords[0]);
				double y = new Double(coords[1]);
				Coord m = new Coord(x,y);
				all.add(m);
			}
		} catch (Exception ex){
			System.out.println(ex);
			System.out.println("No se puede abrir " + file);
			System.exit(-1);
		}
		return all;
	}
}
