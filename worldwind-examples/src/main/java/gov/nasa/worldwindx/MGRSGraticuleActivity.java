package gov.nasa.worldwindx;

import android.os.Bundle;

import gov.nasa.worldwind.layer.graticule.MGRSGraticuleLayer;

public class MGRSGraticuleActivity extends GeneralGlobeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.wwd.getLayers().addLayer(new MGRSGraticuleLayer());
    }

}
