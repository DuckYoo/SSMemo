package app.ssm.duck.duckapp.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import app.ssm.duck.duckapp.R;

class SaveJavaFileFilter implements FilenameFilter{

	@Override
	public boolean accept(File dir, String filename) {
		// TODO Auto-generated method stub
//		return false;
		return (filename.endsWith(".java")); // Ȯ���ڰ� java���� Ȯ��
	}
	
}

public class SaveFileList extends Activity {
	
//	private static final String FILE_PATH = new String("/sdcard/javaeditor/");
	private List<String> mFileNames = new ArrayList<String>();
	ListView mFlieListView;
	ArrayAdapter<String> mFileList;
	String mMenuCommand;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.save_flie_list);
			
			Intent intent = getIntent();

			
			mMenuCommand= intent.getStringExtra("command_name");
			
			mFlieListView = (ListView) findViewById(R.id.save_file_listview);
			this.updateFileList();
			
			mFlieListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					if(mMenuCommand.equals("Load")) {
						String fileName = mFileList.getItem(position);
						Intent intent = new Intent();
						intent.putExtra("file_name", fileName);
						setResult(RESULT_OK, intent);
						finish();
					}	
				}
			});
		}
		catch(NullPointerException e)
		{
			Log.v(getString(R.string.app_name), e.getMessage());
		}
	}
	
	public void updateFileList()
	{
		String ext = Environment.getExternalStorageState();
		String path = null;
		if(ext.equals(Environment.MEDIA_MOUNTED)) {
			path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/javaeditor/";
		}
		else
		{
			path = Environment.MEDIA_UNMOUNTED;
		}
		
		File files = new File(path);
//		File files = new File(FILE_PATH);
		mFileList = new ArrayAdapter<String>(this, R.layout.save_file_list_item, mFileNames);

		if(files.listFiles(new SaveJavaFileFilter()).length > 0)
		{
			for(File file : files.listFiles(new SaveJavaFileFilter()))
			{
				mFileNames.add(file.getName());
			}
		}
		mFlieListView.setAdapter(mFileList);
	}
}
