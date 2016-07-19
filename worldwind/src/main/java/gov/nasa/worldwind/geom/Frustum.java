/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import gov.nasa.worldwind.util.Logger;

/**
 * Represents a six-sided view frustum in Cartesian coordinates with a corresponding viewport in screen coordinates.
 */
public class Frustum {

    protected final Plane left = new Plane(1, 0, 0, 1);

    protected final Plane right = new Plane(-1, 0, 0, 1);

    protected final Plane bottom = new Plane(0, 1, 0, 1);

    protected final Plane top = new Plane(0, -1, 0, 1);

    protected final Plane near = new Plane(0, 0, -1, 1);

    protected final Plane far = new Plane(0, 0, 1, 1);

    protected final Plane[] planes = {this.left, this.right, this.top, this.bottom, this.near, this.far};

    protected final Viewport viewport = new Viewport(0, 0, 1, 1);

    private Matrix4 scratchMatrix = new Matrix4();

    /**
     * Constructs a new unit frustum with each of its planes 1 meter from the center and a viewport with width and
     * height both 1.
     */
    public Frustum() {
    }

    /**
     * Constructs a frustum.
     *
     * @param left     the frustum's left plane
     * @param right    the frustum's right plane
     * @param bottom   the frustum's bottom plane
     * @param top      the frustum's top plane
     * @param near     the frustum's near plane
     * @param far      the frustum's far plane
     * @param viewport the frustum's viewport
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Frustum(Plane left, Plane right, Plane bottom, Plane top, Plane near, Plane far, Viewport viewport) {
        if (left == null || right == null || bottom == null || top == null || near == null || far == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingPlane"));
        }

        if (viewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "constructor", "missingViewport"));
        }

        this.left.set(left);
        this.right.set(right);
        this.bottom.set(bottom);
        this.top.set(top);
        this.near.set(near);
        this.far.set(far);
        this.viewport.set(viewport);
    }

    /**
     * Sets this frustum to a unit frustum with each of its planes 1 meter from the center a viewport with width and
     * height both 1.
     *
     * @return this frustum, set to a unit frustum
     */
    public Frustum setToUnitFrustum() {
        this.left.set(1, 0, 0, 1);
        this.right.set(-1, 0, 0, 1);
        this.bottom.set(0, 1, 0, 1);
        this.top.set(0, -1, 0, 1);
        this.near.set(0, 0, -1, 1);
        this.far.set(0, 0, 1, 1);
        this.viewport.set(0, 0, 1, 1);

        return this;
    }

    /**
     * Sets this frustum to one appropriate for a modelview-projection matrix. A modelview-projection matrix's view
     * frustum is a Cartesian volume that contains everything visible in a scene displayed using that
     * modelview-projection matrix.
     * <p/>
     * This method assumes that the specified matrices represents a projection matrix and a modelview matrix
     * respectively. If this is not the case the results are undefined.
     *
     * @param projection the projection matrix to extract the frustum from
     * @param modelview  the modelview matrix defining the frustum's position and orientation in Cartesian coordinates
     * @param viewport   the screen coordinate viewport corresponding to the projection matrix
     *
     * @return this frustum, with its planes set to the modelview-projection matrix's view frustum, in Cartesian
     * coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Frustum setToModelviewProjection(Matrix4 projection, Matrix4 modelview, Viewport viewport) {
        if (projection == null || modelview == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "setToModelviewProjection", "missingMatrix"));
        }

        if (viewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "setToModelviewProjection", "missingViewport"));
        }

        // Compute the transpose of the modelview matrix.
        this.scratchMatrix.transposeMatrix(modelview);

        // Get the components of the projection matrix.
        double[] m = projection.m;
        double x, y, z, w;

        // Left Plane = row 4 + row 1:
        x = m[12] + m[0];
        y = m[13] + m[1];
        z = m[14] + m[2];
        w = m[15] + m[3];
        this.left.set(x, y, z, w); // normalizes the plane's coordinates
        this.left.transformByMatrix(this.scratchMatrix);

        // Right Plane = row 4 - row 1:
        x = m[12] - m[0];
        y = m[13] - m[1];
        z = m[14] - m[2];
        w = m[15] - m[3];
        this.right.set(x, y, z, w); // normalizes the plane's coordinates
        this.right.transformByMatrix(this.scratchMatrix);

        // Bottom Plane = row 4 + row 2:
        x = m[12] + m[4];
        y = m[13] + m[5];
        z = m[14] + m[6];
        w = m[15] + m[7];
        this.bottom.set(x, y, z, w); // normalizes the plane's coordinates
        this.bottom.transformByMatrix(this.scratchMatrix);

        // Top Plane = row 4 - row 2:
        x = m[12] - m[4];
        y = m[13] - m[5];
        z = m[14] - m[6];
        w = m[15] - m[7];
        this.top.set(x, y, z, w); // normalizes the plane's coordinates
        this.top.transformByMatrix(this.scratchMatrix);

        // Near Plane = row 4 + row 3:
        x = m[12] + m[8];
        y = m[13] + m[9];
        z = m[14] + m[10];
        w = m[15] + m[11];
        this.near.set(x, y, z, w); // normalizes the plane's coordinates
        this.near.transformByMatrix(this.scratchMatrix);

        // Far Plane = row 4 - row 3:
        x = m[12] - m[8];
        y = m[13] - m[9];
        z = m[14] - m[10];
        w = m[15] - m[11];
        this.far.set(x, y, z, w); // normalizes the plane's coordinates
        this.far.transformByMatrix(this.scratchMatrix);

        // Copy the specified viewport.
        this.viewport.set(viewport);

        return this;
    }

    /**
     * Sets this frustum to one appropriate for a subset of a modelview-projection matrix. A modelview-projection
     * matrix's view frustum is a Cartesian volume that contains everything visible in a scene displayed using that
     * modelview-projection matrix. The subset is defined by the region within the original viewport that the frustum
     * contains.
     * <p/>
     * This method assumes that the specified matrices represents a projection matrix and a modelview matrix
     * respectively. If this is not the case the results are undefined.
     *
     * @param projection  the projection matrix to extract the frustum from
     * @param modelview   the modelview matrix defining the frustum's position and orientation in Cartesian coordinates
     * @param viewport    the screen coordinate viewport corresponding to the projection matrix
     * @param subViewport the screen coordinate region the frustum should contain
     *
     * @return this frustum, with its planes set to the modelview-projection matrix's view frustum, in Cartesian
     * coordinates
     *
     * @throws IllegalArgumentException If any argument is null
     */
    public Frustum setToModelviewProjection(Matrix4 projection, Matrix4 modelview, Viewport viewport, Viewport subViewport) {
        if (projection == null || modelview == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "setToModelviewProjection", "missingMatrix"));
        }

        if (viewport == null || subViewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "setToModelviewProjection", "missingViewport"));
        }

        // Compute the sub-viewport's four edges in screen coordinates.
        double left = subViewport.x;
        double right = subViewport.x + subViewport.width;
        double bottom = subViewport.y;
        double top = subViewport.y + subViewport.height;

        // Transform the sub-viewport's four edges from screen coordinates to Cartesian coordinates.
        Vec3 bln, blf, brn, brf, tln, tlf, trn, trf;
        Matrix4 mvpInv = this.scratchMatrix.setToMultiply(projection, modelview).invert();
        mvpInv.unProject(left, bottom, viewport, bln = new Vec3(), blf = new Vec3());
        mvpInv.unProject(right, bottom, viewport, brn = new Vec3(), brf = new Vec3());
        mvpInv.unProject(left, top, viewport, tln = new Vec3(), tlf = new Vec3());
        mvpInv.unProject(right, top, viewport, trn = new Vec3(), trf = new Vec3());

        Vec3 va = new Vec3(tlf.x - bln.x, tlf.y - bln.y, tlf.z - bln.z);
        Vec3 vb = new Vec3(tln.x - blf.x, tln.y - blf.y, tln.z - blf.z);
        Vec3 nl = va.cross(vb);
        this.left.set(nl.x, nl.y, nl.z, -nl.dot(bln));

        va.set(trn.x - brf.x, trn.y - brf.y, trn.z - brf.z);
        vb.set(trf.x - brn.x, trf.y - brn.y, trf.z - brn.z);
        Vec3 nr = va.cross(vb);
        this.right.set(nr.x, nr.y, nr.z, -nr.dot(brn));

        va.set(brf.x - bln.x, brf.y - bln.y, brf.z - bln.z);
        vb.set(blf.x - brn.x, blf.y - brn.y, blf.z - brn.z);
        Vec3 nb = va.cross(vb);
        this.bottom.set(nb.x, nb.y, nb.z, -nb.dot(brn));

        va.set(tlf.x - trn.x, tlf.y - trn.y, tlf.z - trn.z);
        vb.set(trf.x - tln.x, trf.y - tln.y, trf.z - tln.z);
        Vec3 nt = va.cross(vb);
        this.top.set(nt.x, nt.y, nt.z, -nt.dot(tln));

        va.set(tln.x - brn.x, tln.y - brn.y, tln.z - brn.z);
        vb.set(trn.x - bln.x, trn.y - bln.y, trn.z - bln.z);
        Vec3 nn = va.cross(vb);
        this.near.set(nn.x, nn.y, nn.z, -nn.dot(bln));

        va.set(trf.x - blf.x, trf.y - blf.y, trf.z - blf.z);
        vb.set(tlf.x - brf.x, tlf.y - brf.y, tlf.z - brf.z);
        Vec3 nf = va.cross(vb);
        this.far.set(nf.x, nf.y, nf.z, -nf.dot(blf));

        // Copy the specified sub-viewport.
        this.viewport.set(subViewport);

        return this;
    }

    public boolean containsPoint(Vec3 point) {
        if (point == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"));
        }

        // See if the point is entirely within the frustum. The dot product of the point with each plane's vector
        // provides a distance to each plane. If this distance is less than 0, the point is clipped by that plane and
        // neither intersects nor is contained by the space enclosed by this Frustum.

        if (this.far.dot(point) <= 0)
            return false;
        if (this.left.dot(point) <= 0)
            return false;
        if (this.right.dot(point) <= 0)
            return false;
        if (this.top.dot(point) <= 0)
            return false;
        if (this.bottom.dot(point) <= 0)
            return false;
        if (this.near.dot(point) <= 0)
            return false;

        return true;
    }

    /**
     * Determines whether a line segment intersects this frustum.
     *
     * @param pointA the first line segment endpoint
     * @param pointB the second line segment endpoint
     *
     * @return true if the segment intersects or is contained in this frustum, otherwise false
     *
     * @throws IllegalArgumentException If either point is null
     */
    public boolean intersectsSegment(Vec3 pointA, Vec3 pointB) {
        if (pointA == null || pointB == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "containsPoint", "missingPoint"));
        }

        // First do a trivial accept test.
        if (this.containsPoint(pointA) || this.containsPoint(pointB))
            return true;

        if (pointA.equals(pointB))
            return false;

        for (Plane plane : this.planes) {
            // See if both points are behind the plane and therefore not in the frustum.
            if (plane.onSameSide(pointA, pointB) < 0)
                return false;

            // See if the segment intersects the plane.
            if (plane.clip(pointA, pointB) != null)
                return true;

        }

        return false; // segment does not intersect frustum
    }

    /**
     * Determines whether a screen coordinate viewport intersects this frustum.
     *
     * @param viewport the viewport to test
     *
     * @return true if the viewport intersects or is contained in this frustum, otherwise false
     *
     * @throws IllegalArgumentException If the viewport is null
     */
    public boolean intersectsViewport(Viewport viewport) {
        if (viewport == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "Frustum", "intersectsViewport", "missingViewport"));
        }

        return this.viewport.intersects(viewport);
    }
}
