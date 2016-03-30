/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;
precision mediump int;

const int FRAGMODE_SKY = 1;
const int FRAGMODE_GROUND_PRIMARY = 2;
const int FRAGMODE_GROUND_SECONDARY = 3;
const int FRAGMODE_GROUND_PRIMARY_TEX_BLEND = 4;

const int SAMPLE_COUNT = 2;
const float SAMPLES = 2.0;

uniform int fragMode;
uniform mat4 mvpMatrix;
uniform mat3 texCoordMatrix;
uniform vec3 vertexOrigin;
uniform vec3 eyePoint;
uniform float eyeMagnitude;	        /* The eye point's magnitude */
uniform float eyeMagnitude2;	    /* eyeMagnitude^2 */
uniform vec3 lightDirection;	    /* The direction vector to the light source */
uniform vec3 invWavelength;	        /* 1 / pow(wavelength, 4) for the red, green, and blue channels */
uniform float atmosphereRadius;     /* The outer (atmosphere) radius */
uniform float atmosphereRadius2;    /* atmosphereRadius^2 */
uniform float globeRadius;		    /* The inner (planetary) radius */
uniform float KrESun;			    /* Kr * ESun */
uniform float KmESun;			    /* Km * ESun */
uniform float Kr4PI;			    /* Kr * 4 * PI */
uniform float Km4PI;			    /* Km * 4 * PI */
uniform float scale;			    /* 1 / (atmosphereRadius - globeRadius) */
uniform float scaleDepth;		    /* The scale depth (i.e. the altitude at which the atmosphere's average density is found) */
uniform float scaleOverScaleDepth;	/* fScale / fScaleDepth */

attribute vec4 vertexPoint;
attribute vec2 vertexTexCoord;

varying vec3 primaryColor;
varying vec3 secondaryColor;
varying vec3 direction;
varying vec2 texCoord;

void main() {
    /* Transform the vertex point by the modelview-projection matrix */
    gl_Position = mvpMatrix * vertexPoint;

    if (fragMode == FRAGMODE_SKY) {
        sampleSky();
    } else if (fragMode == FRAGMODE_GROUND_PRIMARY) {
        sampleGround();
    } else if (fragMode == FRAGMODE_GROUND_SECONDARY) {
        sampleGround();
    } else if (fragMode == FRAGMODE_GROUND_PRIMARY_TEX_BLEND) {
        sampleGround();
        /* Transform the vertex texture coordinate by the tex coord matrix */
        texCoord = (texCoordMatrix * vec3(vertexTexCoord, 1.0)).st;
    }
}
