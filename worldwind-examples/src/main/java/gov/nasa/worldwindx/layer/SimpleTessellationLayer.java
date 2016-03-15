/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx.layer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwindx.ExampleUtil;

public class SimpleTessellationLayer extends AbstractLayer {

    protected FloatBuffer vertices;

    protected ShortBuffer triStrip;

    protected ShortBuffer lines;

    public SimpleTessellationLayer() {
        super("Tessellation Layer");
    }

    @Override
    protected void doRender(DrawContext dc) {
        if (this.vertices == null) {
            this.tessellate(dc);
        }

        dc.putUserProperty("tessellatorVertices", this.vertices);
        dc.putUserProperty("tessellatorTriStrip", this.triStrip);
        dc.putUserProperty("tessellatorLines", this.lines);
    }

    protected void tessellate(DrawContext dc) {
        int numLat = 50;
        int numLon = 100;
        int stride = 5;
        int count = numLat * numLon * stride;

        Sector sector = new Sector().setFullSphere();

        this.vertices = ByteBuffer.allocateDirect(count * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        dc.getGlobe().geographicToCartesianGrid(sector, numLat, numLon, null, null, this.vertices, stride).rewind();
        ExampleUtil.assembleTexCoords(numLat, numLon, (FloatBuffer) this.vertices.position(3), stride).rewind();

        this.triStrip = ExampleUtil.assembleTriStripIndices(numLat, numLon);

        this.lines = ExampleUtil.assembleLineIndices(numLat, numLon);
    }
}
