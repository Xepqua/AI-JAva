package agent;

import java.awt.Point;
import java.util.ArrayList;

public class Cluster implements Comparable<Cluster>{
	
	private ArrayList<Data> dataSet=new ArrayList<Data>();
	private Data centroid;
	
	public Cluster() {
	}
	
	public void putData(Data d){
		dataSet.add(d);
	}
	
	public ArrayList<Point> getPoints(){
		ArrayList<Point> points=new ArrayList<Point>();
		for(Data d:dataSet)
			points.add(new Point((int) d.X(),(int)d.Y()));
		
		return points;
	}
	
	public Double getDistance(){
		double nums[]=new double[dataSet.size()];
		
		int i=0;
		for(Data d:dataSet){
			nums[i]=KMeansDirtyTile.dist((int) centroid.X(),(int) centroid.X(),(int) d.X(),(int) d.Y());
			i++;
		}
		
		return KMeansDirtyTile.average(nums);
	}

	public Data getCentroid() {
		return centroid;
	}

	public void setCentroid(Data centroid) {
		this.centroid = centroid;
	}

	@Override
	public int compareTo(Cluster o) {
		if(getDistance()>o.getDistance())
			return 1;
		if(getDistance()<o.getDistance())
			return -1;
		return 0;
	}
	

}
