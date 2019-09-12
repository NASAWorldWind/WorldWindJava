/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.layers.mercator;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.layers.TextureTile;
import gov.nasa.worldwind.util.*;

/**
 * @version $Id: MercatorTextureTile.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class MercatorTextureTile extends TextureTile
{
	private MercatorSector mercatorSector;

	public MercatorTextureTile(MercatorSector mercatorSector, Level level,
			int row, int col)
	{
		super(mercatorSector, level, row, col);
		this.mercatorSector = mercatorSector;
	}

	@Override
	public MercatorTextureTile[] createSubTiles(Level nextLevel)
	{
		if (nextLevel == null)
		{
			String msg = Logging.getMessage("nullValue.LevelIsNull");
			Logging.logger().severe(msg);
			throw new IllegalArgumentException(msg);
		}
		double d0 = this.getMercatorSector().getMinLatPercent();
		double d2 = this.getMercatorSector().getMaxLatPercent();
		double d1 = d0 + (d2 - d0) / 2.0;

		Angle t0 = this.getSector().getMinLongitude();
		Angle t2 = this.getSector().getMaxLongitude();
		Angle t1 = Angle.midAngle(t0, t2);

		String nextLevelCacheName = nextLevel.getCacheName();
		int nextLevelNum = nextLevel.getLevelNumber();
		int row = this.getRow();
		int col = this.getColumn();

		MercatorTextureTile[] subTiles = new MercatorTextureTile[4];

		TileKey key = new TileKey(nextLevelNum, 2 * row, 2 * col,
				nextLevelCacheName);
		MercatorTextureTile subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[0] = subTile;
		else
			subTiles[0] = new MercatorTextureTile(new MercatorSector(d0, d1,
					t0, t1), nextLevel, 2 * row, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row, 2 * col + 1,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[1] = subTile;
		else
			subTiles[1] = new MercatorTextureTile(new MercatorSector(d0, d1,
					t1, t2), nextLevel, 2 * row, 2 * col + 1);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[2] = subTile;
		else
			subTiles[2] = new MercatorTextureTile(new MercatorSector(d1, d2,
					t0, t1), nextLevel, 2 * row + 1, 2 * col);

		key = new TileKey(nextLevelNum, 2 * row + 1, 2 * col + 1,
				nextLevelCacheName);
		subTile = this.getTileFromMemoryCache(key);
		if (subTile != null)
			subTiles[3] = subTile;
		else
			subTiles[3] = new MercatorTextureTile(new MercatorSector(d1, d2,
					t1, t2), nextLevel, 2 * row + 1, 2 * col + 1);

		return subTiles;
	}

	protected MercatorTextureTile getTileFromMemoryCache(TileKey tileKey)
	{
		return (MercatorTextureTile) WorldWind.getMemoryCache(
				MercatorTextureTile.class.getName()).getObject(tileKey);
	}

	public MercatorSector getMercatorSector()
	{
		return mercatorSector;
	}
}
