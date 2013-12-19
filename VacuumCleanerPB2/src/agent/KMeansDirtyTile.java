package agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;



public class KMeansDirtyTile {
	
	public static void main(String[] args) {
		ArrayList<Point> points=new ArrayList<Point>();
		
		Random r=new Random();
		while(points.size()<10){
			Point p=new Point(r.nextInt(20),r.nextInt(20));
			if(!points.contains(p))
				points.add(p);
		}
		
		
		for(Cluster c:getBestClusters(points)){
			System.out.println("CLUSTER C:"+c.getCentroid().X()+" "+c.getCentroid().Y());
			System.out.println("DISTANCE: "+c.getDistance());
			for(Point p:c.getPoints())
				System.out.println(p);
			System.out.println("");
		}
		
		
	}
	
	public static ArrayList<Cluster> getBestClusters(ArrayList<Point> points){
		
		int K=points.size()/2;

		
		HashMap<Double, ArrayList<Cluster>> clustersFind=new HashMap<Double, ArrayList<Cluster>>();

		while(K>2){
			KMeansDirtyTile km=new KMeansDirtyTile(K);
			km.addPoints(points);
			ArrayList<Cluster> clusters=km.getClusters();
			
			double nums[]=new double[clusters.size()];
			
			int i=0;
			for(Cluster c:clusters){
				nums[i]=c.getDistance();
				i++;
			}
			
			double media=average(nums);
			clustersFind.put(media, clusters);
			
			K--;

			
			
		}
		
		Double bestDouble=null;
		for(Double d:clustersFind.keySet()){
			if(bestDouble==null)
				bestDouble=d;
			else if(bestDouble>d)
				bestDouble=d;
		}
		
		return clustersFind.get(bestDouble);
	}
	
    private  ArrayList<Data> dataSet = new ArrayList<Data>();
    private  ArrayList<Centroid> centroids = new ArrayList<Centroid>();
    
    private int numberCluster;
    private int numberData;
    
    public KMeansDirtyTile(int numberCluster) {
    	this.numberCluster=numberCluster;
	}

    public void addPoints(List<Point> points){
    	numberData=points.size();
    	for(Point p:points){
    		Data d=new Data(p.x, p.y);
    		dataSet.add(d);
    	}
    }
    
    private void initialize()
    {
    	Random r=new Random();
    	
    	while(centroids.size()<numberCluster){
    		Data d=dataSet.get(r.nextInt(dataSet.size()));
    		
    		Centroid c=new Centroid(d.X(),d.Y());
    		if(!centroids.contains(c))
    			centroids.add(c);

    	}

    }
    
    private void kMeanCluster()
    {
        final double bigNumber = Integer.MAX_VALUE;    // some big number that's sure to be larger than our data range.
        double minimum = bigNumber;                   // The minimum value to beat. 
        double distance = 0.0;                        // The current minimum value.
        int sampleNumber = 0;
        int cluster = 0;
        boolean isStillMoving = true;
        Data newData = null;
        
        // Add in new data, one at a time, recalculating centroids with each new one. 
        while(sampleNumber < numberData)
        {
        	newData=dataSet.get(sampleNumber);
            minimum = bigNumber;
            for(int i = 0; i < numberCluster; i++)
            {
                distance = dist(newData, centroids.get(i));
                if(distance < minimum){
                    minimum = distance;
                    cluster = i;
                }
            }
            newData.cluster(cluster);
            
            // calculate new centroids.
            for(int i = 0; i < numberCluster; i++)
            {
                int totalX = 0;
                int totalY = 0;
                int totalInCluster = 0;
                for(int j = 0; j < dataSet.size(); j++)
                {
                    if(dataSet.get(j).cluster() == i){
                        totalX += dataSet.get(j).X();
                        totalY += dataSet.get(j).Y();
                        totalInCluster++;
                    }
                }
                if(totalInCluster > 0){
                    centroids.get(i).X(totalX / totalInCluster);
                    centroids.get(i).Y(totalY / totalInCluster);
                }
            }
            sampleNumber++;
        }

        // Now, keep shifting centroids until equilibrium occurs.
        while(isStillMoving)
        {
            // calculate new centroids.
            for(int i = 0; i < numberCluster; i++)
            {
                int totalX = 0;
                int totalY = 0;
                int totalInCluster = 0;
                for(int j = 0; j < dataSet.size(); j++)
                {
                    if(dataSet.get(j).cluster() == i){
                        totalX += dataSet.get(j).X();
                        totalY += dataSet.get(j).Y();
                        totalInCluster++;
                    }
                }
                if(totalInCluster > 0){
                    centroids.get(i).X(totalX / totalInCluster);
                    centroids.get(i).Y(totalY / totalInCluster);
                }
            }
            
            // Assign all data to the new centroids
            isStillMoving = false;
            
            for(int i = 0; i < dataSet.size(); i++)
            {
                Data tempData = dataSet.get(i);
                minimum = bigNumber;
                for(int j = 0; j < numberCluster; j++)
                {
                    distance = dist(tempData, centroids.get(j));
                    if(distance < minimum){
                        minimum = distance;
                        cluster = j;
                    }
                }
                tempData.cluster(cluster);
                if(tempData.cluster() != cluster){
                    tempData.cluster(cluster);
                    isStillMoving = true;
                }
            }
        }
    }
    
    /**
     * // Calculate Euclidean distance.
     * @param d - Data object.
     * @param c - Centroid object.
     * @return - double value.
     */
    public static  double dist(Data d, Centroid c)
    {
        return Math.sqrt(Math.pow((c.Y() - d.Y()), 2) + Math.pow((c.X() - d.X()), 2));
    }
   
    public static  double dist(int x1, int y1, int x2, int y2)
    {
        return Math.sqrt(Math.pow((x1 - x2), 2) + Math.pow((y1 - y2), 2));
    }
    
    public static double average(double nums[]){
        double ms = 0;
        for (int i = 0; i < nums.length; i++)
            ms += nums[i] * nums[i];
        ms /= nums.length;
        return Math.sqrt(ms);
    }
    
    
    public ArrayList<Cluster> getClusters(){
    	
    	initialize();
    	kMeanCluster();
    	
    	ArrayList<Cluster> clusters=new ArrayList<Cluster>();
    	
    	for(int i=0;i<numberCluster;i++)
    		clusters.add(new Cluster());
		
    	//Add data
    	for(Data d:dataSet){
			clusters.get(d.cluster()).putData(d);
		}
    	
    	//Add centroids
    	for(int i=0;i<centroids.size();i++){
    		clusters.get(i).setCentroid(new Data(centroids.get(i).X(),centroids.get(i).Y()));
    	}
    	
    	return clusters;
    }
    



}
