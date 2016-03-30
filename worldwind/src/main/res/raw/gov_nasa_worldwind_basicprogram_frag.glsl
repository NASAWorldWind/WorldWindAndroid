/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform bool enableTexture;
uniform vec4 color;
uniform sampler2D texSampler;

varying vec2 texCoord;

void main() {
    if (enableTexture) {
        /* Modulate the specified fragment color by the specified 2D texture's color */
        gl_FragColor = color * texture2D(texSampler, texCoord);
    } else {
        /* Return the specified fragment color */
        gl_FragColor = color;
    }
}