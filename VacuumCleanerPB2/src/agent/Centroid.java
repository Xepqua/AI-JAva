package agent;

class Centroid
{
    private double mX = 0.0;
    private double mY = 0.0;
    
    public Centroid()
    {
        return;
    }
    
    public Centroid(double newX, double newY)
    {
        this.mX = newX;
        this.mY = newY;
        return;
    }
    
    public void X(double newX)
    {
        this.mX = newX;
        return;
    }
    
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
		Centroid other = (Centroid) obj;
		if (Double.doubleToLongBits(mX) != Double.doubleToLongBits(other.mX))
			return false;
		if (Double.doubleToLongBits(mY) != Double.doubleToLongBits(other.mY))
			return false;
		return true;
	}

	public double X()
    {
        return this.mX;
    }
    
    public void Y(double newY)
    {
        this.mY = newY;
        return;
    }
    
    public double Y()
    {
        return this.mY;
    }
}
