package app.ssm.duck.duckapp.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import app.ssm.duck.duckapp.R;

public class JavaEditor extends Activity {
    /** Called when the activity is first created. */
	
	private static final int  MENU_OPTION = Menu.FIRST;
	private static final int MENU_CANCEL = Menu.FIRST + 1;
	private static final int SUBMENU_FILE_SAVE = Menu.FIRST + 2;
	private static final int SUBMENU_FILE_LOAD = Menu.FIRST + 3;
	private static final int ACT_SAVE = 0;
	private static final int ACT_LOAD = 1;
	
	private String mMenuCommand = null;
	private EditText mCustomEditText;
	private String mAbsolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	private Intent mIntent;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		SubMenu optionMenu = menu.addSubMenu(0, MENU_OPTION, Menu.NONE, "OPTION");
		optionMenu.add(0, SUBMENU_FILE_SAVE	, Menu.NONE, "SAVE");
		optionMenu.add(0, SUBMENU_FILE_LOAD, Menu.NONE, "LOAD");
		MenuItem itemCancel = menu.add(0, MENU_CANCEL, Menu.NONE, "CANCEL");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		
		switch(item.getItemId()) {
		case MENU_OPTION : 
			return true;
		case MENU_CANCEL : 
			return true;
		case SUBMENU_FILE_SAVE:
			mMenuCommand = "Save";
			mIntent = new Intent(JavaEditor.this, SaveFileList.class);
			mIntent.putExtra("command_name", mMenuCommand);
			startActivityForResult(mIntent, ACT_SAVE);
			return true;
		case SUBMENU_FILE_LOAD:
			mMenuCommand = "Load";
			mIntent = new Intent(JavaEditor.this, LoadFileList.class);
			mIntent.putExtra("command_name", mMenuCommand);
//			startActivity(intent);
			startActivityForResult(mIntent, ACT_LOAD);
			return true;
		}
		
		return false;
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
//        LayoutInflater inflator = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        ViewGroup layout = (ViewGroup) inflator.inflate(R.layout.editor, null);

        setContentView(R.layout.editor);

        mCustomEditText = (CustomEditText) findViewById(R.id.custom_edittext);
        
    	this.Initialize();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	switch(requestCode) {
    	case ACT_LOAD:
    		if(resultCode == RESULT_OK) {
    			String filePath = data.getStringExtra("file_path");
    			
//    	    	String ext = Environment.getExternalStorageState();
//    	    	String path = null;

	    		try {
//	    	    	if(ext.equals(Environment.MEDIA_MOUNTED)) {
//	    	    		path = mAbsolutePath + "/javaeditor/";
//	    	    	}
//	    	    	else
//	    	    	{
//	    	    		path = Environment.MEDIA_UNMOUNTED;
//	    	    	}
//	    	    	FileInputStream fis = new FileInputStream(path + filePath);
	    	    	FileInputStream fis = new FileInputStream(filePath);
	    	    	byte[] fileData = new byte[fis.available()];
	    	    	while(fis.read(fileData) != -1) {;}
	    	    	fis.close();
	    	    	mCustomEditText.setText(new String(fileData));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					showToast("File Not Found");
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    		
    		} 
    		break;
    	}
    }
    
    private void showToast(String s)
    {
    	Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
    
    public void Initialize()
    {
    	String ext = Environment.getExternalStorageState();
    	String path = null;
    	if(ext.equals(Environment.MEDIA_MOUNTED)) {
    		path = mAbsolutePath + "/javaeditor/";
    		File directory = new File(path);
    		if(!directory.isDirectory()) directory.mkdir();
    	}
//    	File path = new File("/sdcard/javaeditor/");
//    	if(!path.isDirectory()) path.mkdir();
    }
}