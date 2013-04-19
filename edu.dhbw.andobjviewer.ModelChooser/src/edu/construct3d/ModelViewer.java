





package edu.dhbw.andobjviewer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import edu.dhbw.andobjviewer.parser.SimpleTokenizer;
import edu.dhbw.andobjviewer.util.ArrayIterator;
import edu.dhbw.andobjviewer.util.AssetsFileUtil;
import edu.dhbw.andobjviewer.util.BaseFileUtil;

/**

 *Edu-Construct 3D
 *
 */
public class ModelViewer extends AndARActivity implements SurfaceHolder.Callback {
	
	
	
	/* Menu Options: */
	private final int MENU_SCALE = 0;
	private final int MENU_ROTATE = 1;
	private final int MENU_TRANSLATE = 2;
	private final int MENU_SCREENSHOT = 3;
	private final int MENU_INSTRUCTIONS=4;
	
	private int mode = MENU_SCALE;
	String names;
	SimpleTokenizer temptoken= new SimpleTokenizer();
	
	private Model[] models;
	private Model3D[] models3d;
	
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
		artoolkit =getArtoolkit();		
		names =(String) res.getText(R.string.Names_markers_models); 
		temptoken.setDelimiter(",");
		temptoken.setStr(names);
		
		models = new Model[temptoken.delimOccurCount()];
		models3d=new Model3D[temptoken.delimOccurCount()];
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
        menu.add(0,MENU_INSTRUCTIONS,0,res.getText(R.string.instructions))
        .setIcon(R.drawable.help);
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
	        case MENU_INSTRUCTIONS:
		        Intent intent = new Intent(ModelViewer.this, InstructionsActivity.class);
	            intent.setAction(Intent.ACTION_VIEW);
	            startActivity(intent);
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
    	if(ArrayIterator.isAnynull(models)) {
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
			 
				
					//Model model=models[i];
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
						if(!ArrayIterator.isAnynull(models) ){
							switch(mode) {
								case MENU_SCALE:
									for(int i=0;i<models.length;i++)
									{
										if(models3d[i].isVisible()){
								
											Log.d("SCALE","SCALE FOR --"+i);
											models[i].setScale(dY/100.0f);
										}
									}

						            break;
						        case MENU_ROTATE:
						        	for(int i=0;i<models.length;i++)
									{
						        		if(models3d[i].isVisible()){
											Log.d("ROTATE","ROTATE FOR --"+i);
						        			models[i].setXrot(-1*dX);//dY-> Rotation um die X-Achse
											models[i].setYrot(-1*dY);//dX-> Rotation um die Y-Achse
									
						        		}
						        		
									}
									break;
						            
						        case MENU_TRANSLATE:
						        	for(int i=0;i<models.length;i++)
									{
						        		if(models3d[i].isVisible()){
											Log.d("TRANSLATE","TRANSALATE FOR --"+i);
						        			models[i].setXpos(dY/10f);
						        			models[i].setYpos(dX/10f);
										}
									}
						        	

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
				
			
//				models[i]=model;
				
				}
				
				
			
			
			
			return true;
		}
    	
    }
    
	private class ModelLoader extends AsyncTask<Void, Void, Void> {
		@Override
    	protected Void doInBackground(Void... params) {
    		
    		for(int i=0;i<models.length;i++)
    		{
    		
    		String number=temptoken.next();
    		
    		String modelFileName = number+".obj";
			BaseFileUtil fileUtil= null;
			
			Log.d("Model Name", number);
				fileUtil = new AssetsFileUtil(getResources().getAssets());
				fileUtil.setBaseFolder("models/");
			
			//read the model file   :						
			if(modelFileName.endsWith(".obj")) {
				ObjParser parser = new ObjParser(fileUtil);
				try {
		            if(fileUtil != null) {
						BufferedReader fileReader = fileUtil.getReaderFromName(modelFileName);
						if(fileReader != null) {
							models[i] = parser.parse("Model no-"+number, fileReader);
							models3d[i] = new Model3D(models[i],number+".patt");
						}
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
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
    			if(!ArrayIterator.isAnynull(models3d))
    				{for(int i=0;i<models3d.length;i++)
    				{artoolkit.registerARObject(models3d[i]);}
    				}
			} catch (AndARException e) {
				e.printStackTrace();
			}
    		startPreview();
    	}
    }
	
	
	class TakeAsyncScreenshot extends AsyncTask<Void, Void, Void> {
		
		private String errorMsg = null;
		String name="";
		String path="";
		
		@Override
		protected Void doInBackground(Void... params) {
			Bitmap bm = takeScreenshot();
			FileOutputStream fos;
			
			try {
				path= (String) getResources().getText(R.string.screenshotpath);
				name= (String) getResources().getText(R.string.screenshotname)+"-"+new Date().getTime()+".png";
				fos = new FileOutputStream(path+name);
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
				Toast.makeText(ModelViewer.this, getResources().getString(R.string.screenshotsaved)+" "+ name, Toast.LENGTH_SHORT ).show();
			else
				Toast.makeText(ModelViewer.this, getResources().getText(R.string.screenshotfailed)+errorMsg, Toast.LENGTH_SHORT ).show();
		};
		
	}
}
	
	

