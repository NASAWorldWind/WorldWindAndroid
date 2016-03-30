/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.util.Logger;

public class GestureGroup {

    protected List<GestureRecognizer> recognizerList = new ArrayList<>();

    public GestureGroup() {
    }

    public void addRecognizer(GestureRecognizer recognizer) {
        if (recognizer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GestureGroup", "addRecognizer", "missingRecognizer"));
        }

        this.recognizerList.add(recognizer);
    }

    public void removeRecognizer(GestureRecognizer recognizer) {
        if (recognizer == null) {
            throw new IllegalArgumentException(
                Logger.logMessage(Logger.ERROR, "GestureGroup", "removeRecognizer", "missingRecognizer"));
        }

        this.recognizerList.remove(recognizer);
    }

    public List<GestureRecognizer> getRecognizers() {
        return this.recognizerList;
    }

    public boolean onTouchEvent(MotionEvent event) {

        for (GestureRecognizer recognizer : this.recognizerList) {
            recognizer.onTouchEvent(event);
        }

        return true;
    }
}
