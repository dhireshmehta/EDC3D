package edu.construct3d;

import edu.construct3d.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

public class InstructionsActivity extends Activity {
	
	private WebView mWebView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.instructions_layout);
		final Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	Intent intent = new Intent(InstructionsActivity.this, ModelViewer.class);
	            intent.setAction(Intent.ACTION_VIEW);
	            startActivity(intent);
                // Perform action on click
            }
        });
            
		mWebView = (WebView) findViewById(R.id.instructions_webview);
		
		WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        
        WebChromeClient client = new WebChromeClient();
        mWebView.setWebChromeClient(client);
                
		mWebView.loadUrl("file:///android_asset/help/"+getResources().getString(R.string.help_file));
		
	}
}
