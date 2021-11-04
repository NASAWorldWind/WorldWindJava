package gov.nasa.worldwind.terrain.tessellate.icosahedron;

import gov.nasa.worldwind.geom.Vec4;

public class CacheKey {

    private final Vec4 centroid;
    private int resolution;
    private final double verticalExaggeration;
    private int density;

    public CacheKey(IcosaTile tile, int resolution, double verticalExaggeration, int density) {
        // private class, no validation required.
        this.centroid = tile.pCentroid;
        this.resolution = resolution;
        this.verticalExaggeration = verticalExaggeration;
        this.density = density;
    }

    @Override
    public String toString() {
        return "density " + this.density + " ve " + this.verticalExaggeration + " resolution " + this.resolution;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheKey cacheKey = (CacheKey) o;

        if (density != cacheKey.density) {
            return false;
        }
        if (resolution != cacheKey.resolution) {
            return false;
        }
        if (Double.compare(cacheKey.verticalExaggeration, verticalExaggeration) != 0) {
            return false;
        }
        //noinspection RedundantIfStatement
        if (centroid != null ? !centroid.equals(cacheKey.centroid) : cacheKey.centroid != null) {
            return false;
        }

        return true;
    }

    public int hashCode() {
        int result;
        long temp;
        result = (centroid != null ? centroid.hashCode() : 0);
        result = 31 * result + resolution;
        temp = verticalExaggeration != +0.0d ? Double.doubleToLongBits(verticalExaggeration) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + density;
        return result;
    }
}
