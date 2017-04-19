/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

#ifdef GL_FRAGMENT_PRECISION_HIGH
precision highp float;
#else
precision mediump float;
#endif

uniform bool enablePickMode;
uniform bool enableTexture;
uniform vec4 color;
uniform sampler2D texSampler;

varying vec2 texCoord;
varying vec2 tileCoord;

void main() {
    /* Using the second texture coordinate, compute a mask that's 1.0 when the fragment is inside the surface tile, and
       0.0 otherwise. */
    float sMask = step(0.0, tileCoord.s) * step(0.0, 1.0 - tileCoord.s);
    float tMask = step(0.0, tileCoord.t) * step(0.0, 1.0 - tileCoord.t);
    float tileMask = sMask * tMask;

    if (enablePickMode && enableTexture) {
        /* Using the first texture coordinate, modulate the RGBA color with the 2D texture's Alpha component (rounded to
           0.0 or 1.0). Finally, modulate the result by the tile mask to suppress fragments outside the surface tile. */
        float texMask = floor(texture2D(texSampler, texCoord).a + 0.5);
        gl_FragColor = color * texMask * tileMask;
    } else if (!enablePickMode && enableTexture) {
        /* Using the first texture coordinate, modulate the RGBA color with the 2D texture's RGBA color. Finally,
           modulate by the tile mask to suppress fragments outside the surface tile. */
        gl_FragColor = color * texture2D(texSampler, texCoord) * tileMask;
    } else {
        /* Modulate the RGBA color by the tile mask to suppress fragments outside the surface tile. */
        gl_FragColor = color * tileMask;
    }
}