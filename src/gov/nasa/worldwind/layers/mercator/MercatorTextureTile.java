/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
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