/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

uniform mat4 mvpMatrix;
uniform mat4 slpMatrix[2];

attribute vec4 vertexPoint;

varying vec4 sightlinePosition;
varying float sightlineDistance;

void main() {
    /* Transform the vertex position by the modelview-projection matrix. */
    gl_Position = mvpMatrix * vertexPoint;

    /* Transform the vertex position by the sightline-projection matrix. */
    vec4 sightlineEyePosition = slpMatrix[1] * vertexPoint;
    sightlinePosition = slpMatrix[0] * sightlineEyePosition;
    sightlineDistance = length(sightlineEyePosition);
}