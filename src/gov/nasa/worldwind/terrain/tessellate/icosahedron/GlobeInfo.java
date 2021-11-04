package gov.nasa.worldwind.terrain.tessellate.icosahedron;

import gov.nasa.worldwind.globes.Globe;

// Stores properties for constructing the globe
public class GlobeInfo {

    protected final Globe globe; // TODO: remove the dependency on this
    protected final double level0EdgeLength;
    protected final double invAsq;
    protected final double invCsq;

    static final double EDGE_FACTOR = Math.sqrt(10d + 2d * Math.sqrt(5d)) / 4d;

    protected GlobeInfo(Globe globe) {
        this.globe = globe;
        double equatorialRadius = globe.getEquatorialRadius();
        double polarRadius = globe.getPolarRadius();

        this.invAsq = 1 / (equatorialRadius * equatorialRadius);
        this.invCsq = 1 / (polarRadius * polarRadius);

        this.level0EdgeLength = equatorialRadius / EDGE_FACTOR;
    }
}
