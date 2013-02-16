





package edu.dhbw.andobjviewer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import edu.dhbw.andar.ARToolkit;
import edu.dhbw.andar.AndARActivity;
import edu.dhbw.andar.exceptions.AndARException;
import edu.dhbw.andarmodelviewer.R;
import edu.dhbw.andobjviewer.graphics.LightingRenderer;
import edu.dhbw.andobjviewer.graphics.Model3D;
import edu.dhbw.andobjviewer.models.Model;
import edu.dhbw.andobjviewer.parser.ObjParser;
import edu.dhbw.andobjviewer.parser.ParseException;
import edu.dhbw.andobjviewer.util.AssetsFileUtil;
import edu.dhbw.andobjviewer.util.BaseFileUtil;

/**

 *Edu-Construct 3D
 *
 */
public class ModelViewer extends AndARActivity implements SurfaceHolder.Callback {
	
	/**
	 * View a file in the assets folder
	 */
	public static final int TYPE_INTERNAL = 0;
	/**
	 * View a file on the sd card.
	 */
	public static final int TYPE_EXTERNAL = 1;
	
	public static final boolean DEBUG = false;
	
	/* Menu Options: */
	private final int MENU_SCALE = 0;
	private final int MENU_ROTATE = 1;
	private final int MENU_TRANSLATE = 2;
	private final int MENU_SCREENSHOT = 3;
	
	private int mode = MENU_SCALE;
	

	private Model model;
	private Model3D model3d;
	private Model model2;
	private Model3D model3d2;
	private ProgressDialog waitDialog;
	private Resources res;
	
	ARToolkit artoolkit;
	
	public ModelViewer() {
		super(false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setNonARRenderer(new LightingRenderer());//or might be omitted
		res=getResources();
		artoolkit = getArtoolkit();		
		getSurfaceView().setOnTouchListener(new TouchEventHandler());
		getSurfaceView().getHolder().addCallback(this);
	}
	
	

	/**
	 * Inform the user about exceptions that occurred in background threads.
	 */
	
	public void uncaughtException(Thread thread, Throwable ex) {
		System.out.println("");
	}
	

    /* create the menu
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, MENU_TRANSLATE, 0, res.getText(R.string.translate))
    		.setIcon(R.drawable.translate);
        menu.add(0, MENU_ROTATE, 0, res.getText(R.string.rotate))
        	.setIcon(R.drawable.rotate);
        menu.add(0, MENU_SCALE, 0, res.getText(R.string.scale))
        	.setIcon(R.drawable.scale);     
        menu.add(0, MENU_SCREENSHOT, 0, res.getText(R.string.take_screenshot))
    		.setIcon(R.drawable.screenshoticon);     
        return true;
    }
    
    /* Handles item selections */
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
	        case MENU_SCALE:
	            mode = MENU_SCALE;
	            return true;
	        case MENU_ROTATE:
	        	mode = MENU_ROTATE;
	            return true;
	        case MENU_TRANSLATE:
	        	mode = MENU_TRANSLATE;
	            return true;
	        case MENU_SCREENSHOT:
	        	new TakeAsyncScreenshot().execute();
	        	return true;
        }
        return false;
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	super.surfaceCreated(holder);
    	//load the model
    	//this is done here, to assure the surface was already created, so that the preview can be started
    	//after loading the model
    	if(model == null) {
			waitDialog = ProgressDialog.show(this, "", 
	                getResources().getText(R.string.loading), true);
			waitDialog.show();
			new ModelLoader().execute();
		}
    }
    
	
    /**
     * Handles touch events.
     *Edu-Construct 3D
     *
     */
    class TouchEventHandler implements OnTouchListener {
    	
    	private float lastX=0;
    	private float lastY=0;

		/* handles the touch events.
		 * the object will either be scaled, translated or rotated, dependen on the
		 * current user selected mode.
		 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
		 */
		
		public boolean onTouch(View v, MotionEvent event) {
			if(model2!=null) {
				switch(event.getAction()) {
					//Action started
					default:
					case MotionEvent.ACTION_DOWN:
						lastX = event.getX();
						lastY = event.getY();
						break;
					//Action ongoing
					case MotionEvent.ACTION_MOVE:
						float dX = lastX - event.getX();
						float dY = lastY - event.getY();
						lastX = event.getX();
						lastY = event.getY();
						if(model2 != null) {
							switch(mode) {
								case MENU_SCALE:
									model2.setScale(dY/100.0f);
									model.setScale(dY/100.0f);

						            break;
						        case MENU_ROTATE:
						        	model2.setXrot(-1*dX);//dY-> Rotation um die X-Achse
									model2.setYrot(-1*dY);//dX-> Rotation um die Y-Achse
									model.setXrot(-1*dX);//dY-> Rotation um die X-Achse
									model.setYrot(-1*dY);//dX-> Rotation um die Y-Achse
						            break;
						        case MENU_TRANSLATE:
						        	model2.setXpos(dY/10f);
									model2.setYpos(dX/10f);
									model.setXpos(dY/10f);
									model.setYpos(dX/10f);
						        	break;
							}		
						}
						break;
					//Action ended
					case MotionEvent.ACTION_CANCEL:	
					case MotionEvent.ACTION_UP:
						lastX = event.getX();
						lastY = event.getY();
						break;
				}
			}
			return true;
		}
    	
    }
    
	private class ModelLoader extends AsyncTask<Void, Void, Void> {
		
		
    	@Override
    	protected Void doInBackground(Void... params) {
    		
			String modelFileName = "sofa.obj";
			BaseFileUtil fileUtil= null;
			
				fileUtil = new AssetsFileUtil(getResources().getAssets());
				fileUtil.setBaseFolder("models/");
				
			
			
			//read the model file   :						
			if(modelFileName.endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
										if(fileUtil != null) {
						BufferedReader fileReader = fileUtil.getReaderFromName(modelFileName);
						if(fileReader != null) {
							model = parser.parse("Model", fileReader);
							model3d = new Model3D(model,"barcode.patt");
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			
			
			}
			modelFileName="superman.obj";
			if(modelFileName.endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
					
				
					if(fileUtil != null) {
						BufferedReader fileReader = fileUtil.getReaderFromName(modelFileName);
						if(fileReader != null) {
							model2 = parser.parse("Model2", fileReader);
							model3d2 = new Model3D(model2,"center.patt");
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			
			
			}
    		return null;
    	}
    	@Override
    	protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		waitDialog.dismiss();
    		
    		//register model
    		try {
    			if(model3d!=null)
    				artoolkit.registerARObject(model3d);
			} catch (AndARException e) {
				e.printStackTrace();
			}
    		try {
    			if(model3d2!=null)
    				artoolkit.registerARObject(model3d2);
			} catch (AndARException e) {
				e.printStackTrace();
			}
			startPreview();
    	}
    }
	
	
	class TakeAsyncScreenshot extends AsyncTask<Void, Void, Void> {
		
		private String errorMsg = null;

		@Override
		protected Void doInBackground(Void... params) {
			Bitmap bm = takeScreenshot();
			FileOutputStream fos;
			try {
				fos = new FileOutputStream("/sdcard/AndARScreenshot"+new Date().getTime()+".png");
				bm.compress(CompressFormat.PNG, 100, fos);
				fos.flush();
				fos.close();					
			} catch (FileNotFoundException e) {
				errorMsg = e.getMessage();
				e.printStackTrace();
			} catch (IOException e) {
				errorMsg = e.getMessage();
				e.printStackTrace();
			}	
			return null;
		}
		
		protected void onPostExecute(Void result) {
			if(errorMsg == null)
				Toast.makeText(ModelViewer.this, getResources().getText(R.string.screenshotsaved), Toast.LENGTH_SHORT ).show();
			else
				Toast.makeText(ModelViewer.this, getResources().getText(R.string.screenshotfailed)+errorMsg, Toast.LENGTH_SHORT ).show();
		};
		
	}
	
	
}
