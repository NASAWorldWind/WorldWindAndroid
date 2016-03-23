/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform mat4 mvpMatrix;
uniform mat3 texCoordMatrix[2];

attribute vec4 vertexPoint;
attribute vec2 vertexTexCoord;

varying vec2 texCoord[2];

void main() {
    /* Transform the vertex position by the modelview-projection matrix. */
    gl_Position = mvpMatrix * vertexPoint;

    /* Transform the vertex tex coord by the tex coord matrices. */
    vec3 texCoord3 = vec3(vertexTexCoord, 1.0);
    texCoord[0] = (texCoordMatrix[0] * texCoord3).st;
    texCoord[1] = (texCoordMatrix[1] * texCoord3).st;
}