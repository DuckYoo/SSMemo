package app.ssm.duck.duckapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

public class GroupingActivity extends AppCompatActivity {
    ArrayList<Vertex> vtx;
    private groupView gview;
    private LinearLayout.LayoutParams params;
    private String btnText, image_path;
    private int id = 0, cwidth=0, cheight=0;
    Bitmap bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setLayoutView(this);

        vtx = new ArrayList<Vertex>();
        image_path = getIntent().getStringExtra("convertedImagePath");
        Log.d("TAG","" + image_path);
        bm = BitmapFactory.decodeFile(image_path);

    }

    public class Vertex{
        float x;
        float y;
        boolean draw;

        public Vertex(float x, float y, boolean draw){
            this.x = x;
            this.y = y;
            this.draw = draw;
        }
    }

    void setLayoutView(Context context){
        LinearLayout vlayout = new LinearLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vlayout.setLayoutParams(params);
        vlayout.setOrientation(LinearLayout.VERTICAL);
        vlayout.setBackgroundColor(Color.BLACK);

        final LinearLayout hlayout = new LinearLayout(context);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT,1);
        hlayout.setOrientation(LinearLayout.HORIZONTAL);

        //setButton
        final Button setBtn = new Button(context);
        setBtn.setLayoutParams(params);
        btnText = getString(R.string.diversion_string);
        setBtn.setText(btnText);
        setBtn.setId(id + 0);

        //skipButton
        final Button skipBtn = new Button(context);
        skipBtn.setLayoutParams(params);
        btnText = getString(R.string.skip_string);
        skipBtn.setText(btnText);
        skipBtn.setId(id + 1);

        gview = new groupView(context);

        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        gview.setLayoutParams(params);
        gview.setDrawingCacheEnabled(true);

        hlayout.addView(setBtn);
        hlayout.addView(skipBtn);
        vlayout.addView(hlayout);
        vlayout.addView(gview);

        setContentView(vlayout);

        setBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Toast toast = Toast.makeText(v.getContext(),"setClicked", Toast.LENGTH_LONG);
                toast.show();
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast toast = Toast.makeText(v.getContext(),"skipClicked", Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    class groupView extends View {
        Paint paint = new Paint();
        Path path = new Path();
        DashPathEffect dashPath = new DashPathEffect(new float[]{5,5},2);
        float pressedX,pressedY;
        Bitmap originbitmap;

        public groupView(Context context){
            super(context);
            paint.setPathEffect(dashPath);
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(5);
            paint.setAntiAlias(true);
        }

        public groupView(Context context, AttributeSet attrs){
            super(context, attrs);
            paint.setPathEffect(dashPath);
            paint.setColor(Color.GRAY);
            paint.setStrokeWidth(5);
            paint.setAntiAlias(true);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event){
            super.onTouchEvent(event);
            pressedX = event.getX();
            pressedY = event.getY();

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    vtx.add(new Vertex(pressedX,pressedY,false));
                    break;
                case MotionEvent.ACTION_MOVE:
                    vtx.add(new Vertex(pressedX,pressedY,true));
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            cwidth = canvas.getWidth();
            cheight = canvas.getHeight();

            canvas.drawBitmap(bm,0,0,paint);

            for(int i=0; i<vtx.size(); i++){
                if(vtx.get(i).draw){
                    canvas.drawLine(vtx.get(i-1) .x,vtx.get(i-1).y,vtx.get(i).x,vtx.get(i).y,paint);
                }else{
                    canvas.drawPoint(vtx.get(i).x,vtx.get(i).y,paint);
                }
            }
        }
    }

//    protected void setBitmapSize(Bitmap bitmap){
//        int w = bitmap.getWidth()/cwidth;
//        int h = bitmap.getHeight()/cheight;
//
//            Bitmap resizedbitmap = Bitmap.createBitmap(bitmap, 0, 0, 0, 0);
//            if (bitmap != resizedbitmap) {
//                bitmap.recycle();
//                bitmap = resizedbitmap;
//            }
//        }catch (OutOfMemoryError e){
//
//        }
//    }



}
