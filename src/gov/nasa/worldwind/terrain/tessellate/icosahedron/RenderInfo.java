package gov.nasa.worldwind.terrain.tessellate.icosahedron;

import gov.nasa.worldwind.globes.Globe;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;
import gov.nasa.worldwind.terrain.*;

public class RenderInfo {

    protected final int density;
    protected DoubleBuffer vertices;
    protected final IntBuffer indices;
    protected final FloatBuffer textureCoords;
    protected Object vboCacheKey = new Object();
    protected boolean isVboBound = false;
    private IcoSphereTessellator tessellator;
    protected IcosaTile tile;

    protected RenderInfo(IcoSphereTessellator tessellator, IcosaTile tile, int density,
            DoubleBuffer vertices, Globe globe, SectorGeometryList currentTiles, int currentLevel,
            FloatBuffer textureCoords) {
        this.tessellator = tessellator;
        IcoSphereTessellator.createIndices(density);
        this.density = density;
        this.vertices = vertices;
        this.textureCoords = textureCoords;
        this.indices = this.tessellator.indexLists.get(this.density);
        this.tile = tile;
    }

    protected long getSizeInBytes() {
        return 12;// + this.vertices.limit() * 5 * Float.SIZE;
    }
}
