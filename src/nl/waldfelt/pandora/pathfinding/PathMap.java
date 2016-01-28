package nl.waldfelt.pandora.pathfinding;

import java.awt.Rectangle;
import java.io.File;
import java.util.Iterator;

import tiled.core.Map;
import tiled.core.MapObject;
import tiled.core.ObjectGroup;
import tiled.io.TMXMapReader;

public class PathMap {
	
	private TMXMapReader mReader;
	private Map mMap;

	private int mMapWidth;
	private int mMapHeight;
	
	private int mTileWidth;
	private int mTileHeight;
	
	private Node[][] mPathMap;
	
	
	public PathMap() {
		// Initialize TMX map reader
		mReader = new TMXMapReader();
		
		// Try to retrieve map (NEEDS WORK!!!)
		try {
			File file = new File("../PandoraClient/assets/tmx/terrain.tmx");
			
			mMap = mReader.readMap(file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		
		// Set tile dimensions in map
		mMapWidth = mMap.getWidth();
		mMapHeight = mMap.getHeight();
		mTileWidth = mMap.getTileWidth();
		mTileHeight = mMap.getTileHeight();
		
		// Set layers (NEEDS WORK!!!) doesn't automatically sort proper layers & object groups
		ObjectGroup objectLayer = (ObjectGroup) mMap.getLayer(1);
		
		// Initialize the bitmap
		mPathMap = new Node[mMapWidth][mMapHeight];
		
		for(int x = 0; x < mMapWidth; x++) {
			for(int y = 0; y < mMapHeight; y++) {
				mPathMap[x][y] = new Node(x, y);
			}
		}
		
		// Get collision objects from the map and store them in the bitmap
		if(objectLayer.getProperties().getProperty("collide").equals("true")) {
		
			Iterator<MapObject> objects = objectLayer.getObjects();
			
			while( objects.hasNext() ) {
				MapObject object = objects.next();
				Rectangle bounds = object.getBounds();
				
				for(int x = (int) bounds.getMinX() / mTileWidth; x < (int) bounds.getMaxX() / mTileWidth; x++) {
					for(int y = (int) bounds.getMinY() / mTileHeight; y < (int) bounds.getMaxY() / mTileHeight; y++) {
						mPathMap[x][y].setAsObstacle(true);
					}
				}
			}
		}
		
	}
	
	
	// Return node specified at (x,y)
	public Node getNode(int pX, int pY) {
		return mPathMap[pX][pY];
	}
	
	
	// Return the width of the map (and thus bitmap)
	public int getWidth() {
		return mMapWidth;
	}
	
	// Return the height of the map (and thus bitmap)
	public int getHeight() {
		return mMapHeight;
	}
	
	// Print PathMap
	public void printPathMap() {
		for(int y = 0; y < mMapHeight; y++) {
			String padding = "";
			if(y < 10) {
				padding = " ";
			}
			System.out.print(padding + y + " |");
			for(int x = 0; x < mMapWidth; x++) {
				System.out.print( mPathMap[x][y].isObstacle() ? "# " : ". " );
			}
			System.out.println();
		}
	}
	
	
	
}
