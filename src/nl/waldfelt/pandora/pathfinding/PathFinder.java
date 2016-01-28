package nl.waldfelt.pandora.pathfinding;

import java.util.ArrayList;


public class PathFinder {

	// ----- Pathfinder Variables
	private PathMap mPathMap;	
	private Node mCurrentPoint;
	private Node mTargetPoint;	
	private ArrayList<Node> mPath;
	
	public PathFinder(PathMap pPathMap, int mCurrentX, int mCurrentY, int mTargetX, int mTargetY) {
		mPathMap = pPathMap;
		mCurrentPoint = mPathMap.getNode(mCurrentX, mCurrentY);
		mPath = new ArrayList<Node>();
		
		mTargetPoint = getNonObstacleNode(mPathMap.getNode(mTargetX, mTargetY));
		
		findPath();
	}
	
	
	public void findPath() {
		
		// Localized A* Pathfinding Algorithm
		ArrayList<Node> openList = new ArrayList<Node>();
		
		mCurrentPoint.setDistanceFromStart(0); 
		openList.add( mCurrentPoint );
		
		ArrayList<Node> closedList = new ArrayList<Node>();
		
		Node lFinger;
		
		while(openList.size() > 0) {
			
			lFinger = openList.remove(0);
			
			if(lFinger.equals(mTargetPoint)) {
				reconstructPath(lFinger);
				return;
			}
			closedList.add(lFinger);
			
			for(Node pPoint : getNeighbours(lFinger)) {
				boolean neighbourIsBetter;
				
				if(closedList.contains(pPoint)) {
					continue;
				}
				
				if(!pPoint.isObstacle()) {
					float neighbourDistanceFromStart = lFinger.getDistanceFromStart() + getDistance(lFinger, pPoint);
					
					if(!openList.contains(pPoint)) {
						openList.add(pPoint);
						neighbourIsBetter = true;
					} else if(neighbourDistanceFromStart < lFinger.getDistanceFromStart()) {
						neighbourIsBetter = true;
					} else {
						neighbourIsBetter = false;
					}
					
					if(neighbourIsBetter) {
						pPoint.setParent(lFinger);
						pPoint.setDistanceFromStart( neighbourDistanceFromStart );

						float hX = (mTargetPoint.x - pPoint.x);
						float hY = (mTargetPoint.y - pPoint.y);
						
						pPoint.setHeuristicDistanceToEnd( hX * hX + hY * hY );
					}
				}
			}
		}
	}
	
	//Get path by searching parents of the nodes, starting at the final node
	private void reconstructPath(Node pNode) {
		mPath = new ArrayList<Node>();
		
		mPath.add(pNode);
		while(pNode.getParent() != null) {
			mPath.add(0, pNode.getParent());
			pNode = pNode.getParent();
		}
		
		// Post Smoothpath Processing
		
		ArrayList<Node> smoothPath = new ArrayList<Node>();
		smoothPath.add(mPath.get(0));
		
		int k = 0;
		
		for(int i = 1; i < mPath.size() - 1; i++) {
			// Check whether a waypoint is necessary; if so, add it to the set
			if(!lineOfSight(smoothPath.get( k ), mPath.get(i + 1))) {
				smoothPath.add(mPath.get(i));
				k++;
			}
		}
		smoothPath.add(mPath.get( mPath.size() - 1));
		mPath = smoothPath;		
	}
	
	public Integer[] getPath() {
		
		Integer[] lPath = new Integer[mPath.size() * 2];
		for(int i = 0; i < mPath.size() * 2; i = i + 2) {
			lPath[i] = 	mPath.get(i / 2).x;
			lPath[i + 1] = mPath.get(i / 2).y;
		}
		
		return lPath;
	}
	
	//Create the actual path to modifiy the entity
	/*public Path getPath() {
		
		Path lPath = new Path(path.size());
		
		boolean isCurrentPath = true;
		
		for(Node pNode : path) {

			int lPathOffsetX = mTiledMap.getTileWidth() / 2;
			int lPathOffsetY = mTiledMap.getTileHeight() / 2;
			
			// If it is the first tile in the path, adopt current offset on the tile
			if(isCurrentPath) {
				lPathOffsetX = mCurrentTileOffsetX;
				lPathOffsetY = mCurrentTileOffsetY;
				isCurrentPath = false;
			}
			lPath.to((pNode.x + mFrustrumOffsetX)* 16 - SceneManager.mWorldScene.getPlayerSprite().getWidth() / 2 + lPathOffsetX, (pNode.y  + mFrustrumOffsetY) * 16 - SceneManager.mWorldScene.getPlayerSprite().getHeight() + lPathOffsetY);
		}
		
		return lPath; 
	}*/
	
	// Determine whether two nodes are in line of sight of eachother
	private boolean lineOfSight(Node pNode1, Node pNode2) {
		
		
		int pX0 = pNode1.x;
		int pY0 = pNode1.y;
		
		int pX1 = pNode2.x;
		int pY1 = pNode2.y;
		
		int dX = Math.abs(pX1 - pX0);
		int dY = Math.abs(pY1 - pY0);
		
		int x = pX0;
		int y = pY0;
		
		int n = 1 + dX + dY;

		int x_inc = (pX1 > pX0) ? 1 : -1;
		int y_inc = (pY1 > pY0) ? 1 : -1;
		
		int error = dX - dY;
		
		dX = dX * 2;
		dY = dY * 2;
		
		for(; n > 0; n--) {
			if(x < 0 || y < 0 || x > mPathMap.getWidth() || y > mPathMap.getHeight()) {
				return false;
			}
			
			if(mPathMap.getNode(x, y).isObstacle()) {
				return false;
			}
			
			if(error > 0) {
				x = x + x_inc;
				error = error - dY;
			} else if(error < 0) {
				y = y + y_inc;
				error = error + dX;
			} else {
				x = x + x_inc;
				y = y + y_inc;
				error = error - dY;
				error = error + dX;
			}
		}
		
		return true; 
	}

	// ----- AStar helper functions
	private ArrayList<Node> getNeighbours(Node pPoint) {
		ArrayList<Node> neighbours = new ArrayList<Node>();
		
		int pX = pPoint.x;
		int pY = pPoint.y;
		
		// check top, right, bottom, left
		if(isValidCoordinate(pX - 1, pY)) {
			neighbours.add( mPathMap.getNode(pX - 1, pY) );
		}

		if(isValidCoordinate(pX, pY + 1)) {
			neighbours.add( mPathMap.getNode(pX, pY + 1) );
		}

		if(isValidCoordinate(pX + 1, pY)) {
			neighbours.add( mPathMap.getNode(pX + 1, pY) );
		}

		if(isValidCoordinate(pX, pY - 1)) {
			neighbours.add( mPathMap.getNode(pX, pY - 1) );
		}
		
		// check topleft, topright, bottomright, bottomleft
		if(isValidCoordinate(pX - 1, pY - 1)) {
			neighbours.add( mPathMap.getNode(pX - 1, pY - 1) );
		}

		if(isValidCoordinate(pX - 1, pY + 1)) {
			neighbours.add( mPathMap.getNode(pX - 1, pY + 1) );
		}

		if(isValidCoordinate(pX + 1, pY + 1)) {
			neighbours.add( mPathMap.getNode(pX + 1, pY + 1) );
		}

		if(isValidCoordinate(pX + 1, pY - 1)) {
			neighbours.add( mPathMap.getNode(pX + 1, pY - 1) );
		}
		
		return neighbours;
		
	}
	
	//Check whether coordinates do exist on the localized pathmap
	private boolean isValidCoordinate(int pX, int pY) {
		
		// Breach map width
		if(pX < 0 || pX > mPathMap.getWidth()) {
			return false;
		}
		
		// Breach map height
		if(pY < 0 || pY > mPathMap.getHeight()) {
			return false;
		}
		
		return true;
	}
	
	// Find closest nonObstacleNode
	private Node getNonObstacleNode(Node pNode) {
		
		// Initialize smallest distance
		float validDistance = Float.MAX_VALUE;
		Node validNode = null;
		
		ArrayList<Node> needles = new ArrayList<Node>();
		ArrayList<Node> haystack = new ArrayList<Node>();
		ArrayList<Node> visited = new ArrayList<Node>();
		
		haystack.add(pNode);
		Node lFinger;
		
		// Continue while the searchArea is populated
		while(haystack.size() > 0) {
			
			lFinger = haystack.remove(0);
			
			// If we already visited the node, skip routines
			if(visited.contains(lFinger)) {
				continue;
			}
			
			// Add to visited nodes
			visited.add(lFinger);
			
			
			if(!lFinger.isObstacle()) {
				// If Node is not an obstacle, it may be a candidate for closest valid nodes
				if(!needles.contains(lFinger))
					needles.add(lFinger);
				
			} else {
				// If the this node also is an obstacle, find it's neighbours and add them to the searcharea
				for(Node neighbour : getNeighbours(lFinger)) {
					if(!visited.contains(neighbour)) {
						haystack.add(neighbour); 
					}
				}
			}
		}
		
		
		// We got all the possible candidates
		for(Node lNode : needles) {
			float dist = getDistance(pNode, lNode);
			
			// If this node is a closer fit, replace current closest fit
			if(dist < validDistance) {
				validDistance = dist;
				validNode = lNode;
			}
			
		}		
		
		return validNode;
	}
	
	
	// Return single line distance
	private float getDistance(Node pStart, Node pEnd) {
		int dX = Math.max(pStart.x, pEnd.x) - Math.min(pStart.x, pEnd.x);
		int dY = Math.max(pStart.y, pEnd.y) - Math.min(pStart.y, pEnd.y);
		
		return (float) Math.sqrt( dX * dX + dY * dY);
	}
}
