package app.ssm.duck.duckapp.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.ssm.duck.duckapp.R;

//class LoadFileFilter implements FilenameFilter{
//
//	@Override
//	public boolean accept(File dir, String filename) {
//		// TODO Auto-generated method stub
////		return false;
//		return (filename.endsWith(".java") | filename.endsWith(".txt")); // Ȯ���ڰ� java, txt���� Ȯ��
//	}
//}

public class LoadFileList extends Activity {
	
	private static final int INDEX_GO_ROOT = 0;
	private static final int INDEX_UPPON_LEVEL = 1;
	private static final int MENU_DELETE = 0;
	private static final int MENU_CANCEL = 1;
	private static final int ACT_LOADPOPUP = 0;
	private static final int INDEX_DIRECTRY_START = 2;
	private int INDEX_DIRECTORY_END = 2;
	
	private int mPosition;
	
	private List<IconfiedText> mDirectoryEntries = new ArrayList<IconfiedText>();
	private List<IconfiedText> mFileEntries = new ArrayList<IconfiedText>();
	private ListView mFlieListView;
	private IconfiedTextListAdapter mFileListAdapter;
	private File mCurrentDirectory;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		try
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.load_file_list);
			
			if(mCurrentDirectory == null)
			{
				 mCurrentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
			}

			
			mFlieListView = (ListView) findViewById(R.id.load_file_listview);
			this.updateFileList();
			
			mFlieListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					// TODO Auto-generated method stub
					mPosition = position;
					Intent intent = new Intent(LoadFileList.this, LoadPopup.class);
					startActivityForResult(intent, ACT_LOADPOPUP);

					return true;
				}
			});
			
			mFlieListView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// TODO Auto-generated method stub
					
					switch(position) {
					case INDEX_GO_ROOT : 
						String ext = Environment.getExternalStorageState();
						if(ext.equals(Environment.MEDIA_MOUNTED)) {
							mCurrentDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
						}
						else
						{
							String path = Environment.MEDIA_UNMOUNTED;
							mCurrentDirectory = new File(path);
						}
						updateFileList();
						break;
					case INDEX_UPPON_LEVEL :
						if(mCurrentDirectory.getParent() != null)
								mCurrentDirectory = mCurrentDirectory.getParentFile();
						updateFileList();
						break;
					default :
						// �����̸�
						if(position >= INDEX_DIRECTRY_START && position <= INDEX_DIRECTORY_END)
						{
							String subPath = "/" + ((IconfiedText) mFileListAdapter.getItem(position)).getText();
							mCurrentDirectory = new File(mCurrentDirectory.getPath() + subPath);
							updateFileList();
						}
						else
						{
							String fileName = ((IconfiedText) mFileListAdapter.getItem(position)).getText();
							Intent intent = new Intent();
							intent.putExtra("file_path", mCurrentDirectory.getPath() + "/" + fileName);
							setResult(RESULT_OK, intent);
							finish();
						}
						break;
					}	
				}
			});
		}
		catch(NullPointerException e)
		{
			Log.v(getString(R.string.app_name), e.getMessage());
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode) {
		case ACT_LOADPOPUP:
			if(resultCode == RESULT_OK) {
				boolean haveToDelete = data.getBooleanExtra("load_popup_result", false);
				if(haveToDelete)
				{
					new File(mCurrentDirectory + "/" + ((IconfiedText)mFileListAdapter.getItem(mPosition)).getText()).delete();
					updateFileList();
				}
			}
			break;
		}
	}
	
	public void updateFileList()
	{
		String ext = Environment.getExternalStorageState();
//		String path = null;
		if(ext.equals(Environment.MEDIA_MOUNTED)) {
//			path = Environment.getExternalStorageDirectory().getAbsolutePath() + mSubPath;
//			path = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		else
		{
			String path = Environment.MEDIA_UNMOUNTED;
//			File files = new File(path);
			mCurrentDirectory = new File(path);
		}
		
//		File files = new File(FILE_PATH);
		mFileListAdapter = new IconfiedTextListAdapter(this);
		
		if(mDirectoryEntries != null) {
			mDirectoryEntries = new ArrayList<IconfiedText>();
		}
		if(mFileEntries != null) {
			mFileEntries = new ArrayList<IconfiedText>();
		}
//		if(files.list().length > 0)
//		{
//			for(File file : files.listFiles())
//			{
//				mFileNames.add(file.getName());
//			}
//		}
		
//		if(files.listFiles(new LoadFileFilter()).length > 0)
//		{
			mDirectoryEntries.add(new IconfiedText(".", getResources().getDrawable(R.drawable.goroot)));
			mDirectoryEntries.add(new IconfiedText("..", getResources().getDrawable(R.drawable.uponelevel)));
			for(File file : mCurrentDirectory.listFiles())
			{
				if(file.isDirectory()) mDirectoryEntries.add(new IconfiedText(file.getName(), getResources().getDrawable(R.drawable.folder)));
			}
			INDEX_DIRECTORY_END = mDirectoryEntries.size() - 1;
			Collections.sort(mDirectoryEntries);
			for(File file : mCurrentDirectory.listFiles())
			{
				if(file.getName().endsWith(".txt")|file.getName().endsWith(".java")|file.getName().endsWith(".c")|
						file.getName().endsWith(".cpp"))
				{
					mFileEntries.add(new IconfiedText(file.getName(), getResources().getDrawable(R.drawable.text)));
				}
			}
			Collections.sort(mFileEntries);
			mDirectoryEntries.addAll(mFileEntries);
//		}
		mFileListAdapter.setListItems(mDirectoryEntries);
		mFlieListView.setAdapter(mFileListAdapter);
	}
	
    private void showToast(String s)
    {
    	Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }
}
