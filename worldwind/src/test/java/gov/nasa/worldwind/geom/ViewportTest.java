/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.geom;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ViewportTest {

    @Test
    public void testConstructor_Default() throws Exception {
        Viewport viewport = new Viewport();

        assertNotNull(viewport);
        assertEquals("x", 0, viewport.x);
        assertEquals("y", 0, viewport.y);
        assertEquals("width", 0, viewport.width);
        assertEquals("height", 0, viewport.height);
    }

    @Test
    public void testConstructor_Copy() throws Exception {
        Viewport original = new Viewport(1, 2, 3, 4);
        Viewport viewport = new Viewport(original);

        assertNotNull(viewport);
        assertEquals("x", 1, viewport.x);
        assertEquals("y", 2, viewport.y);
        assertEquals("width", 3, viewport.width);
        assertEquals("height", 4, viewport.height);
    }

    @Test
    public void testConstructor_Parameters() throws Exception {
        Viewport viewport = new Viewport(1, 2, 3, 4);

        assertNotNull(viewport);
        assertEquals("x", 1, viewport.x);
        assertEquals("y", 2, viewport.y);
        assertEquals("width", 3, viewport.width);
        assertEquals("height", 4, viewport.height);
    }

    @Test
    public void testEquals() throws Exception {
        Viewport viewport1 = new Viewport(1, 2, 3, 4);
        Viewport viewport2 = new Viewport(1, 2, 3, 4);

        assertTrue("equals", viewport1.equals(viewport2));
    }

    @Test
    public void testEquals_Null() throws Exception {
        Viewport viewport = new Viewport(1, 2, 3, 4);

        assertFalse("inequality with null", viewport.equals(null));
    }

    @Test
    public void testEquals_Inequality() throws Exception {
        Viewport viewport1 = new Viewport(1, 2, 3, 4);
        Viewport viewport2 = new Viewport(4, 3, 2, 1);

        assertFalse("not equals", viewport1.equals(viewport2));
    }

    @Test
    public void testHashCode() throws Exception {
        Viewport viewport1 = new Viewport(1, 2, 3, 4);
        Viewport viewport2 = new Viewport(viewport1);
        Viewport viewport3 = new Viewport(4, 3, 2, 1);

        int hashCode1 = viewport1.hashCode();
        int hashCode2 = viewport2.hashCode();
        int hashCode3 = viewport3.hashCode();

        assertEquals(hashCode1, hashCode2);
        assertNotEquals(hashCode1, hashCode3);
    }

    @Test
    public void testToString() throws Exception {
        Viewport viewport = new Viewport(1, 2, 3, 4);
        String string = viewport.toString();

        //System.out.println(viewport);
        assertTrue("x", string.contains(Integer.toString(viewport.x)));
        assertTrue("y", string.contains(Integer.toString(viewport.y)));
        assertTrue("width", string.contains(Integer.toString(viewport.width)));
        assertTrue("height", string.contains(Integer.toString(viewport.height)));
    }

    @Test
    public void testSet_Parameters() throws Exception {
        Viewport viewport = new Viewport();
        viewport.set(1, 2, 3, 4);

        assertEquals("x", 1, viewport.x);
        assertEquals("y", 2, viewport.y);
        assertEquals("width", 3, viewport.width);
        assertEquals("height", 4, viewport.height);
    }

    @Test
    public void testSet_Copy() throws Exception {
        Viewport original = new Viewport(1, 2, 3, 4);
        Viewport viewport = new Viewport();
        viewport.set(original);

        assertTrue("not the same reference", original != viewport);
        assertEquals("x", 1, viewport.x);
        assertEquals("y", 2, viewport.y);
        assertEquals("width", 3, viewport.width);
        assertEquals("height", 4, viewport.height);
    }

    @Test
    public void testSetEmpty() throws Exception {
        Viewport viewport = new Viewport(1, 2, 3, 4);
        viewport.setEmpty();

        assertEquals("x", 1, viewport.x);
        assertEquals("y", 2, viewport.y);
        assertEquals("width", 0, viewport.width);
        assertEquals("height", 0, viewport.height);
    }

    @Test
    public void testIsEmpty() throws Exception {
        Viewport viewport1 = new Viewport();
        Viewport viewport2 = new Viewport(1, 2, 3, 4);

        assertTrue("viewport is empty", viewport1.isEmpty());
        assertFalse("viewport is not empty", viewport2.isEmpty());
    }

    @Test
    public void testContains() throws Exception {
        Viewport viewport = new Viewport(1, 2, 3, 4);

        assertTrue("contains x, y", viewport.contains(viewport.x, viewport.y));
        assertTrue("contains x+width-1, y+height-1", viewport.contains(viewport.x + viewport.width - 1, viewport.y + viewport.height - 1));
        assertFalse("does not contain x+width, y+height", viewport.contains(viewport.x + viewport.width, viewport.y + viewport.height));
        assertFalse("does not contain x-1, y", viewport.contains(viewport.x - 1, viewport.y));
        assertFalse("does not contain x, y-1", viewport.contains(viewport.x, viewport.y - 1));
    }

    @Test
    public void testContains_Empty() throws Exception {
        Viewport empty = new Viewport(1, 2, 0, 0);
        Viewport emptyWidth = new Viewport(1, 2, 3, 0);
        Viewport emptyHeight = new Viewport(1, 2, 0, 3);

        assertFalse("empty does not contain x, y", empty.contains(empty.x, empty.y));
        assertFalse("empty width not contain x, y", emptyWidth.contains(emptyWidth.x, emptyWidth.y));
        assertFalse("empty width not contain x, y", emptyHeight.contains(emptyHeight.x, emptyHeight.y));
    }

    @Test
    public void testIntersect() throws Exception {
        Viewport a = new Viewport(30, 100, 2, 2);
        Viewport b = new Viewport(31, 101, 2, 2);
        Viewport northeast = new Viewport(31, 101, 1, 1);

        boolean intersected = a.intersect(b);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", northeast, a);
    }

    @Test
    public void testIntersect_Empty() throws Exception {
        Viewport a = new Viewport(30, 100, 2, 2);
        Viewport b = new Viewport(31, 101, 0, 0);

        boolean aIntersectedB = a.intersect(b);
        boolean bIntersectedA = b.intersect(a);

        assertFalse("a intersecting b", aIntersectedB);
        assertFalse("b intersecting a", bIntersectedA);
    }

    @Test
    public void testIntersect_Inside() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport inside = new Viewport(31, 101, 1, 1);

        boolean intersected = a.intersect(inside);

        assertTrue("interesecting", intersected);
        assertEquals("inside, intersection is interior sector", inside, a);
    }

    @Test
    public void testIntersect_East() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport east = new Viewport(31, 102, 1, 2);
        Viewport expected = new Viewport(31, 102, 1, 1);

        boolean intersected = a.intersect(east);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_West() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport west = new Viewport(31, 99, 1, 2);
        Viewport expected = new Viewport(31, 100, 1, 1);

        boolean intersected = a.intersect(west);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_North() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport north = new Viewport(32, 101, 2, 1);
        Viewport expected = new Viewport(32, 101, 1, 1);

        boolean intersected = a.intersect(north);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_South() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport south = new Viewport(29, 101, 2, 1);
        Viewport expected = new Viewport(30, 101, 1, 1);

        boolean intersected = a.intersect(south);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_AdjacentEast() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentEast = new Viewport(31, 103, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentEast);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentWest() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentWest = new Viewport(31, 99, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentWest);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentNorth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentNorth = new Viewport(33, 101, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentNorth);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_AdjacentSouth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentSouth = new Viewport(29, 101, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentSouth);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_Doubles() throws Exception {
        Viewport a = new Viewport(30, 100, 2, 2);
        Viewport b = new Viewport(31, 101, 2, 2);
        Viewport northeast = new Viewport(31, 101, 1, 1);

        boolean intersected = a.intersect(b.x, b.y, b.width, b.height);

        assertTrue("intersecting", intersected);
        assertEquals("intersection", northeast, a);
    }

    @Test
    public void testIntersect_DoublesEmpty() throws Exception {
        Viewport a = new Viewport(30, 100, 2, 2);
        Viewport b = new Viewport(31, 101, 0, 0);

        boolean aIntersectedB = a.intersect(b.x, b.y, b.width, b.height);
        boolean bIntersectedA = b.intersect(a.x, a.y, a.width, a.height);

        assertFalse("a intersecting b", aIntersectedB);
        assertFalse("b intersecting a", bIntersectedA);
    }

    @Test
    public void testIntersect_DoublesInside() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport inside = new Viewport(31, 101, 1, 1);

        boolean intersected = a.intersect(inside.x, inside.y, inside.width, inside.height);

        assertTrue("interesecting", intersected);
        assertEquals("inside, intersection is interior sector", inside, a);
    }

    @Test
    public void testIntersect_DoublesEast() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport east = new Viewport(31, 102, 1, 2);
        Viewport expected = new Viewport(31, 102, 1, 1);

        boolean intersected = a.intersect(east.x, east.y, east.width, east.height);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_DoublesWest() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport west = new Viewport(31, 99, 1, 2);
        Viewport expected = new Viewport(31, 100, 1, 1);

        boolean intersected = a.intersect(west.x, west.y, west.width, west.height);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_DoublesNorth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport north = new Viewport(32, 101, 2, 1);
        Viewport expected = new Viewport(32, 101, 1, 1);

        boolean intersected = a.intersect(north.x, north.y, north.width, north.height);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_DoublesSouth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport south = new Viewport(29, 101, 2, 1);
        Viewport expected = new Viewport(30, 101, 1, 1);

        boolean intersected = a.intersect(south.x, south.y, south.width, south.height);

        assertTrue("overlapping", intersected);
        assertEquals("intersection", a, expected);
    }

    @Test
    public void testIntersect_DoublesAdjacentEast() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentEast = new Viewport(31, 103, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentEast.x, adjacentEast.y, adjacentEast.width, adjacentEast.height);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_DoublesAdjacentWest() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentWest = new Viewport(31, 99, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentWest.x, adjacentWest.y, adjacentWest.width, adjacentWest.height);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_DoublesAdjacentNorth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentNorth = new Viewport(33, 101, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentNorth.x, adjacentNorth.y, adjacentNorth.width, adjacentNorth.height);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersect_DoublesAdjacentSouth() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport adjacentSouth = new Viewport(29, 101, 1, 1);
        Viewport copy = new Viewport(a);

        boolean intersected = a.intersect(adjacentSouth.x, adjacentSouth.y, adjacentSouth.width, adjacentSouth.height);

        assertFalse("adjacent, no intersection", intersected);
        assertEquals("adjacent, no changed", a, copy);
    }

    @Test
    public void testIntersects() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport copy = new Viewport(a);

        assertTrue("inside", a.intersects(new Viewport(31, 101, 1, 1)));
        assertTrue("overlap east", a.intersects(new Viewport(31, 102, 1, 2)));
        assertTrue("overlap west", a.intersects(new Viewport(31, 99, 1, 2)));
        assertTrue("overlap north", a.intersects(new Viewport(32, 101, 2, 1)));
        assertTrue("overlap south", a.intersects(new Viewport(29, 101, 2, 1)));
        assertEquals("no mutation", copy, a);
    }

    @Test
    public void testIntersects_Empty() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);

        assertFalse("empty", a.intersects(new Viewport()));
        assertFalse("no dimension", a.intersects(new Viewport(31, 101, 0, 0)));
        assertFalse("no width", a.intersects(new Viewport(31, 101, 5, 0)));
        assertFalse("no height", a.intersects(new Viewport(31, 101, 0, 5)));
    }

    @Test
    public void testIntersects_Coincident() throws Exception {
        Viewport a = new Viewport(30, 100, 1, 1);

        assertTrue("coincident", a.intersects(new Viewport(30, 100, 1, 1)));
        assertFalse("coincident east edge", a.intersects(new Viewport(30, 101, 1, 1)));
        assertFalse("coincident west edge", a.intersects(new Viewport(30, 99, 1, 1)));
        assertFalse("coincident north edge", a.intersects(new Viewport(31, 100, 1, 1)));
        assertFalse("coincident south edge", a.intersects(new Viewport(29, 100, 1, 1)));
        assertFalse("coincident ne point", a.intersects(new Viewport(31, 101, 1, 1)));
        assertFalse("coincident se point", a.intersects(new Viewport(29, 101, 1, 1)));
        assertFalse("coincident nw point", a.intersects(new Viewport(31, 99, 1, 1)));
        assertFalse("coincident sw point", a.intersects(new Viewport(29, 99, 1, 1)));
    }

    @Test
    public void testIntersects_Doubles() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);
        Viewport copy = new Viewport(a);

        assertTrue("inside", a.intersects(31, 101, 1, 1));
        assertTrue("overlap east", a.intersects(31, 102, 1, 2));
        assertTrue("overlap west", a.intersects(31, 99, 1, 2));
        assertTrue("overlap north", a.intersects(32, 101, 2, 1));
        assertTrue("overlap south", a.intersects(29, 101, 2, 1));
        assertEquals("no mutation", copy, a);
    }

    @Test
    public void testIntersects_DoublesEmpty() throws Exception {
        Viewport a = new Viewport(30, 100, 3, 3);

        assertFalse("empty", a.intersects(0, 0, 0, 0));
        assertFalse("no dimension", a.intersects(31, 101, 0, 0));
        assertFalse("no width", a.intersects(31, 101, 5, 0));
        assertFalse("no height", a.intersects(31, 101, 0, 5));
    }

    @Test
    public void testIntersects_DoublesCoincident() throws Exception {
        Viewport a = new Viewport(30, 100, 1, 1);

        assertTrue("coincident", a.intersects(30, 100, 1, 1));
        assertFalse("coincident east edge", a.intersects(30, 101, 1, 1));
        assertFalse("coincident west edge", a.intersects(30, 99, 1, 1));
        assertFalse("coincident north edge", a.intersects(31, 100, 1, 1));
        assertFalse("coincident south edge", a.intersects(29, 100, 1, 1));
        assertFalse("coincident ne point", a.intersects(31, 101, 1, 1));
        assertFalse("coincident se point", a.intersects(29, 101, 1, 1));
        assertFalse("coincident nw point", a.intersects(31, 99, 1, 1));
        assertFalse("coincident sw point", a.intersects(29, 99, 1, 1));
    }
}

