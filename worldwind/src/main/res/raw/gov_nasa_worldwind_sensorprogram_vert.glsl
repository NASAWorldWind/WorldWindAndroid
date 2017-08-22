/*
 * Copyright (c) 2017 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

uniform mat4 mvpMatrix;
uniform mat4 svpMatrix[2];

attribute vec4 vertexPoint;

varying vec4 sensorPosition;
varying float sensorDistance;

void main() {
    /* Transform the vertex position by the modelview-projection matrix. */
    gl_Position = mvpMatrix * vertexPoint;

    /* Transform the vertex position by the sensorview-projection matrix. */
    vec4 sensorEyePosition = svpMatrix[1] * vertexPoint;
    sensorPosition = svpMatrix[0] * sensorEyePosition;
    sensorDistance = length(sensorEyePosition);
}