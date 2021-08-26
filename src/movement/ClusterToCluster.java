package movement;

import movement.*;
import movement.cluster.*;
import core.*;
import java.util.*;


public class ClusterToCluster extends MovementModel {


	private static final int PATH_LENGTH =1;
	List<Coord> allClusters;

	private int nextCluster;
	private Coord lastCluster;

	public ClusterToCluster(Settings settings){
		super(settings);
	}

	public ClusterToCluster(ClusterToCluster ctc){
		super(ctc);
	}

	public Coord getInitialLocation(){
		assert rng != null : "MovementModel not initialized!";
		List<Coord> clusters = this.getClusters();
		Coord c = clusters.get(rng.nextInt(clusters.size())).clone();
		this.lastCluster = c;
		return c;
		
	}

	public Path getPath(){
		Path p = new Path(generateSpeed());
		p.addWaypoint(lastCluster.clone());
		Coord c = lastCluster;

		for (int i=0; i<PATH_LENGTH; i++){
			c = this.getClusters().get(rng.nextInt(this.getClusters().size())).clone();
			p.addWaypoint(c);
		}
		this.lastCluster = c;
		return p;
	}

	public ClusterToCluster replicate(){
		return new ClusterToCluster(this);
	}

	private List<Coord> getClusters(){

		if(this.allClusters == null){
			List<Coord> centros = ClustersConfig.getCenters().get("centros");
			List<Coord> all = new ArrayList<>();
			all.addAll(centros);

			this.allClusters = all;
			return all;
		} 
		else {
			return this.allClusters;
		}
	}

}
