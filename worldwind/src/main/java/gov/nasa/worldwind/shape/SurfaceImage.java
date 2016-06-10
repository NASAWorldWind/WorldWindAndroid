/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.shape;

import java.util.Arrays;

import gov.nasa.worldwind.PickedObject;
import gov.nasa.worldwind.draw.DrawableSurfaceTexture;
import gov.nasa.worldwind.geom.Location;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globe.Globe;
import gov.nasa.worldwind.render.AbstractRenderable;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.SurfaceTextureProgram;
import gov.nasa.worldwind.render.Texture;
import gov.nasa.worldwind.util.Logger;
import gov.nasa.worldwind.util.Pool;

public class SurfaceImage extends AbstractRenderable implements Movable {

    protected final Sector sector = new Sector();

    protected ImageSource imageSource;

    public SurfaceImage() {
        super("Surface Image");
    }

    public SurfaceImage(Sector sector, ImageSource imageSource) {
        super("Surface Image");

        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "constructor", "missingSector"));
        }

        this.sector.set(sector);
        this.imageSource = imageSource;
    }

    public Sector getSector() {
        return this.sector;
    }

    public void setSector(Sector sector) {
        if (sector == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "SurfaceImage", "setSector", "missingSector"));
        }

        this.sector.set(sector);
    }

    public ImageSource getImageSource() {
        return imageSource;
    }

    public void setImageSource(ImageSource imageSource) {
        this.imageSource = imageSource;
    }

    @Override
    protected void doRender(RenderContext rc) {
        if (this.sector.isEmpty()) {
            return; // nothing to render
        }

        if (!rc.terrain.getSector().intersects(this.sector)) {
            return; // no terrain surface to render on
        }

        Texture texture = rc.getTexture(this.imageSource); // try to get the texture from the cache
        if (texture == null) {
            texture = rc.retrieveTexture(this.imageSource); // puts retrieved textures in the cache
        }

        if (texture == null) {
            return; // no texture to draw
        }

        // Enqueue a drawable surface texture for processing on the OpenGL thread.
        SurfaceTextureProgram program = this.getShaderProgram(rc);
        Pool<DrawableSurfaceTexture> pool = rc.getDrawablePool(DrawableSurfaceTexture.class);
        DrawableSurfaceTexture drawable = DrawableSurfaceTexture.obtain(pool).set(program, this.sector, texture, texture.getTexCoordTransform());
        rc.offerSurfaceDrawable(drawable, 0 /*z-order*/);

        // Enqueue a picked object that associates the drawable surface texture with this surface image.
        if (rc.pickMode) {
            PickedObject terrainObject = rc.pickedObjects.terrainPickedObject();
            PickedObject pickedObject = PickedObject.fromRenderable(this, terrainObject.getPosition(),
                rc.currentLayer, rc.nextPickedObjectId());
            PickedObject.identifierToUniqueColor(pickedObject.getIdentifier(), drawable.color);
            rc.offerPickedObject(pickedObject);
        }
    }

    protected SurfaceTextureProgram getShaderProgram(RenderContext rc) {
        SurfaceTextureProgram program = (SurfaceTextureProgram) rc.getShaderProgram(SurfaceTextureProgram.KEY);

        if (program == null) {
            program = (SurfaceTextureProgram) rc.putShaderProgram(SurfaceTextureProgram.KEY, new SurfaceTextureProgram(rc.resources));
        }

        return program;
    }

    /**
     * A position associated with the object that indicates its aggregate geographic position. The chosen position
     * varies among implementers of this interface. For objects defined by a list of positions, the reference position
     * is typically the first position in the list. For symmetric objects the reference position is often the center of
     * the object. In many cases the object's reference position may be explicitly specified by the application.
     *
     * @return the object's reference position, or null if no reference position is available.
     */
    @Override
    public Position getReferencePosition() {
        Position refPosition = new Position(this.sector.centroidLatitude(), this.sector.centroidLongitude(), 0);
        return refPosition;
    }

    /**
     * Move the shape over the globe's surface while maintaining its original azimuth, its orientation relative to
     * North.
     *
     * @param globe    the globe on which to move the shape.
     * @param position the new position of the shape's reference position.
     */
    @Override
    public void moveTo(Globe globe, Position position) {
        Position oldRef = this.getReferencePosition();
        if (oldRef == null)
            return;

        Location swCorner = new Location(this.sector.minLatitude(), this.sector.minLongitude());
        Location nwCorner = new Location(this.sector.maxLatitude(), this.sector.minLongitude());
        Location seCorner = new Location(this.sector.minLatitude(), this.sector.maxLongitude());
        Location neCorner = new Location(this.sector.maxLatitude(), this.sector.maxLongitude());

        final double EAST = 90;
        final double WEST = -90;
        final double NORTH = 0;
        final double SOUTH = 180;

        // Determine the delta from the reference point to sector's anchor (SW corner)
        double azimuthDegrees = oldRef.greatCircleAzimuth(swCorner);
        double distanceRadians = oldRef.greatCircleDistance(swCorner);
        // Determine the width and height of the sector
        double widthRadians = swCorner.rhumbDistance(seCorner);
        double heightRadians = swCorner.rhumbDistance(nwCorner);
        double dLat = nwCorner.latitude - swCorner.latitude;

        // Compute a new position for the SW corner
        position.greatCircleLocation(azimuthDegrees, distanceRadians, swCorner);

        // If the dragged image would span the pole then constrain to the pole
        if (swCorner.latitude + dLat > 90) {
            nwCorner.set(90, swCorner.longitude);
            //swCorner.set(nwCorner.latitude - dLat, swCorner.longitude);
            nwCorner.rhumbLocation(SOUTH, heightRadians, swCorner);
        } else {
            // Compute the NW corner with the original height
            nwCorner.set(swCorner.latitude + dLat, swCorner.longitude);
            swCorner.rhumbLocation(NORTH, heightRadians, nwCorner);
        }

        // Compute the SE corner, using the original width
        swCorner.rhumbLocation(EAST, widthRadians, seCorner);


        // If the dragged image would span the dateline then snap the image the other side
        if (Location.locationsCrossAntimeridian(Arrays.asList(new Location[]{swCorner, seCorner}))) {
            // TODO: create JIRA issue regarding Sector Anti-meridian limitation
            // There's presently no support for placing SurfaceImages crossing the Anti-meridian
            // Snap the image to the other side of the date line
            double dragAzimuth = oldRef.greatCircleAzimuth(position);
            if (dragAzimuth < 0) {
                // Set the East edge of the sector to the dateline
                seCorner.set(seCorner.latitude, 180);
                seCorner.rhumbLocation(WEST, widthRadians, swCorner);
            } else {
                // Set the West edge of the sector to the dateline
                swCorner.set(swCorner.latitude, -180);
                swCorner.rhumbLocation(EAST, widthRadians, seCorner);
            }
        }

        // Compute the delta lon values from the new SW position
        double dLon = seCorner.longitude - swCorner.longitude;

        // Update the image's sector to move the image
        if (dLat > 0 && dLon > 0) {
            this.sector.set(swCorner.latitude, swCorner.longitude, dLat, dLon);
        }
    }

}
