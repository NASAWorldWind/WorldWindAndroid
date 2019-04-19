package gov.nasa.worldwind.gesture;

import android.view.MotionEvent;

public class MousePanRecognizer extends PanRecognizer {

    protected int buttonState = MotionEvent.BUTTON_PRIMARY;

    public int getButtonState() {
        return buttonState;
    }

    public void setButtonState(int buttonState) {
        this.buttonState = buttonState;
    }

    @Override
    protected boolean shouldRecognize(MotionEvent event) {
        return super.shouldRecognize(event) && event.getButtonState() == buttonState;
    }

}
