package app.ssm.duck.duckapp.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import app.ssm.duck.duckapp.R;

public class LoadPopup extends Activity {

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.load_popup);
		
		Button deleteButton = (Button) findViewById(R.id.load_delete_button);
		Button cancelButton = (Button) findViewById(R.id.load_cancel_button);

		deleteButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra("load_popup_result", true);
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
//		Intent intent = getIntent();
	}
}
