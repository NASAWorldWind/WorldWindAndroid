/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

precision mediump float;

uniform bool enablePickMode;
uniform bool enableTexture;
uniform vec4 color;
uniform sampler2D texSampler;

varying vec2 texCoord;

void main() {
    if (enablePickMode && enableTexture) {
        /* Return the specified fragment RGB color with the texture's Alpha component */
        float texMask = floor(texture2D(texSampler, texCoord).a + 0.5);
        gl_FragColor = color * texMask;
    } else if (!enablePickMode && enableTexture) {
       /* Modulate the specified fragment color by the specified 2D texture's color */
       gl_FragColor = color * texture2D(texSampler, texCoord);
    } else {
        /* Return the specified fragment color */
        gl_FragColor = color;
    }
}