package agent;

import java.awt.Point;

import core.VacuumEnvironment.LocationState;

public class TileNode {
	
	public Point position;
	public LocationState TileType;

	public TileNode() {
		position = new Point();
		TileType = LocationState.Clean;
	}

	public TileNode(Point p, boolean isObstacle) {
		this.position = p;
		if (isObstacle) {
			TileType = LocationState.Obstacle;
		} else
			TileType = LocationState.Clean;
	}
	
	@Override
	protected Object clone()  {
		TileNode t=new TileNode();
		t.position=(Point) this.position.clone();
		t.TileType=this.TileType;
		return t;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((TileType == null) ? 0 : TileType.hashCode());
		result = prime * result
				+ ((position == null) ? 0 : position.hashCode());
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
		TileNode other = (TileNode) obj;
		if (TileType != other.TileType)
			return false;
		if (position == null) {
			if (other.position != null)
				return false;
		} else if (!position.equals(other.position))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "x= " + position.x + " y= " + position.y + " type= "
				+ TileType.name();
	}


}
