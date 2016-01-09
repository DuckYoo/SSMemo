package app.ssm.duck.duckapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by broDuck on 16. 1. 6.
 */
public class ListViewAdapter extends BaseAdapter {
    private Context mContext = null;
    private ArrayList<ListData> mListData = new ArrayList<ListData>();

    public ListViewAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cardview, null);

            holder.mImage = (ImageView) convertView.findViewById(R.id.memo_image);
            holder.mName = (TextView) convertView.findViewById(R.id.memo_name);
            holder.mUpdate = (TextView) convertView.findViewById(R.id.update_date);
            holder.mButton = (Button) convertView.findViewById(R.id.delete_memo);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ListData mData = mListData.get(position);

        if (mData.mImage != null) {
            holder.mImage.setVisibility(View.VISIBLE);
            Bitmap bm = BitmapFactory.decodeFile(mData.mImage);

            holder.mImage.setImageBitmap(bm);
        } else {
            holder.mImage.setVisibility(View.GONE);
        }

        holder.mName.setText(mData.mName);
        holder.mUpdate.setText(mData.mUpdate);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebActivity.class);
                intent.putExtra("memo_id", mData.mHash);

                mContext.startActivity(new Intent(mContext, WebActivity.class));
            }
        });

        holder.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteMemo deleteMemo = new DeleteMemo(mData.mHash);
                deleteMemo.execute();

                remove(position);

                Toast.makeText(mContext, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        return convertView;
    }

    public void addItem(String image, String mName, String mUpdate, String mHash, int key) {
        ListData addInfo = new ListData();
        addInfo.mImage = image;
        addInfo.mName = mName;
        addInfo.mUpdate = mUpdate;
        addInfo.mHash = mHash;

        if (key == 0) {
            mListData.add(addInfo);
        } else {
            mListData.add(0, addInfo);
        }
    }

    public void remove(int position) {
        mListData.remove(position);
        notifyDataSetChanged();
    }

}
