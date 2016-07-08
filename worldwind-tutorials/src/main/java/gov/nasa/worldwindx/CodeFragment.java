/*
 * Copyright (c) 2016 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration. All Rights Reserved.
 */

package gov.nasa.worldwindx;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import gov.nasa.worldwind.WorldWindow;

public class CodeFragment extends Fragment {

    protected WorldWindow wwd;

    public CodeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_code, container, false);
        WebView webView = (WebView) rootView.findViewById(R.id.code_view);

        // Enable JavaScript (which is off by default)
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        if (getArguments().containsKey("url")) {
            String url = getArguments().getString("url");
            webView.loadUrl(url);
        }
        return rootView;
    }
}
