/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

uniform mat4 mvpMatrix;
uniform mat3 texCoordMatrix;
uniform bool enableTexture;

attribute vec4 vertexPoint;
attribute vec2 vertexTexCoord;

varying vec2 texCoord;

void main() {
    gl_Position = mvpMatrix * vertexPoint;

    if (enableTexture) {
        texCoord = (texCoordMatrix * vec3(vertexTexCoord, 1.0)).st;
    }
}