package agent;

class Data
{
    @Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(mX);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(mY);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Data other = (Data) obj;
		if (Double.doubleToLongBits(mX) != Double.doubleToLongBits(other.mX))
			return false;
		if (Double.doubleToLongBits(mY) != Double.doubleToLongBits(other.mY))
			return false;
		return true;
	}

	private double mX = 0;
    private double mY = 0;
    private int mCluster = 0;
    
    public Data()
    {
        return;
    }
    
    public Data(double x, double y)
    {
        this.X(x);
        this.Y(y);
        return;
    }
    
    public void X(double x)
    {
        this.mX = x;
        return;
    }
    
    public double X()
    {
        return this.mX;
    }
    
    public void Y(double y)
    {
        this.mY = y;
        return;
    }
    
    public double Y()
    {
        return this.mY;
    }
    
    public void cluster(int clusterNumber)
    {
        this.mCluster = clusterNumber;
        return;
    }
    
    public int cluster()
    {
        return this.mCluster;
    }
}
