/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;

public class TextRenderer {

    protected float textSize;

    protected Typeface typeface;

    protected boolean enableOutline;

    protected float outlineWidth;

    protected Paint paint;

    protected Canvas canvas;

    protected Rect scratchBounds = new Rect();

    public TextRenderer() {
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setTextAlign(Paint.Align.LEFT);
        this.canvas = new Canvas();

        this.textSize = this.paint.getTextSize();
        this.typeface = this.paint.getTypeface();
        this.enableOutline = false;
        this.outlineWidth = this.paint.getStrokeWidth();
    }

    public float getTextSize() {
        return this.textSize;
    }

    public TextRenderer setTextSize(float size) {
        this.textSize = size;
        this.paint.setTextSize(size);
        return this;
    }

    public Typeface getTypeface() {
        return this.typeface;
    }

    public TextRenderer setTypeface(Typeface typeface) {
        this.typeface = typeface;
        this.paint.setTypeface(typeface);
        return this;
    }

    public boolean isEnableOutline() {
        return this.enableOutline;
    }

    public TextRenderer setEnableOutline(boolean enable) {
        this.enableOutline = enable;
        return this;
    }

    public float getOutlineWidth() {
        return this.outlineWidth;
    }

    public TextRenderer setOutlineWidth(float lineWidth) {
        this.outlineWidth = lineWidth;
        this.paint.setStrokeWidth(lineWidth);
        return this;
    }

    public Texture renderText(String text) {
        if (text != null && text.length() > 0) {
            Bitmap bitmap = this.drawText(text);
            return new Texture(bitmap);
        } else {
            return null;
        }
    }

    protected Bitmap drawText(String text) {
        this.paint.getTextBounds(text, 0, text.length(), this.scratchBounds);
        int x = -this.scratchBounds.left + 1;
        int y = -this.scratchBounds.top + 1;
        int width = this.scratchBounds.width() + 2;
        int height = this.scratchBounds.height() + 2;

        if (this.enableOutline) {
            int strokeWidth_2 = (int) Math.ceil(this.paint.getStrokeWidth() * 0.5f);
            x += strokeWidth_2;
            y += strokeWidth_2;
            width += (strokeWidth_2 * 2);
            height += (strokeWidth_2 * 2);
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        this.canvas.setBitmap(bitmap);

        if (this.enableOutline) {
            this.paint.setStyle(Paint.Style.FILL_AND_STROKE);
            this.paint.setColor(Color.BLACK);
            this.canvas.drawText(text, 0, text.length(), x, y, this.paint);
        }

        this.paint.setStyle(Paint.Style.FILL);
        this.paint.setColor(Color.WHITE);
        this.canvas.drawText(text, 0, text.length(), x, y, this.paint);

        this.canvas.setBitmap(null);

        return bitmap;
    }
}
