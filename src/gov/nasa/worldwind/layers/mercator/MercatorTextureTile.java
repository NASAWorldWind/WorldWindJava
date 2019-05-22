/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.*;

/**
 * @version $Id: MercatorTextureTile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MercatorTextureTile extends TextureTile
{

	public MercatorTextureTile(MercatorSector mercatorSector, Level level,
			int row, int col)
	{
		super(mercatorSector, level, row, col);
	}

	@Override
	public MercatorSector getSector()
	{
		return (MercatorSector) super.getSector();
	}

	@Override
	public TextureTile[] createSubTiles(Level nextLevel)
	{
		if (nextLevel == null)
		{
			String msg = Logging.getMessage("nullValue.LevelIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		double d0 = this.getSector().getMinLatPercent();
		double d2 = this.getSector().getMaxLatPercent();
		double d1 = d0 + (d2 - d0) / 2.0;

		Angle t0 = this.getSector().getMinLongitude();
		Angle t2 = this.getSector().getMaxLongitude();
		Angle t1 = Angle.midAngle(t0, t2);

		String nextLevelCacheName = nextLevel.getCacheName();
		int nextLevelNum = nextLevel.getLevelNumber();

		int northRow = 2 * this.getRow();
		int southRow = northRow + 1;
		int westCol = 2 * this.getColumn();
		int eastCol = westCol + 1;

		TextureTile[] subTiles = new TextureTile[4];

		TextureTile subTile = this.getTileFromMemoryCache(new TileKey(nextLevelNum, northRow, westCol, nextLevelCacheName));
		subTiles[0] = subTile != null ? subTile : new MercatorTextureTile(new MercatorSector(d0, d1, t0, t1), nextLevel, northRow, westCol);

		subTile = this.getTileFromMemoryCache(new TileKey(nextLevelNum, northRow, eastCol, nextLevelCacheName));
		subTiles[1] = subTile != null ? subTile : new MercatorTextureTile(new MercatorSector(d0, d1, t1, t2), nextLevel, northRow, eastCol);

		subTile = this.getTileFromMemoryCache(new TileKey(nextLevelNum, southRow, westCol, nextLevelCacheName));
		subTiles[2] = subTile != null ? subTile : new MercatorTextureTile(new MercatorSector(d1, d2, t0, t1), nextLevel, southRow, westCol);

		subTile = this.getTileFromMemoryCache(new TileKey(nextLevelNum, southRow, eastCol, nextLevelCacheName));
		subTiles[3] = subTile != null ? subTile : new MercatorTextureTile(new MercatorSector(d1, d2, t1, t2), nextLevel, southRow, eastCol);

		return subTiles;
	}

}