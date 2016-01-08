package app.ssm.duck.duckapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.DrawableRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cardview, null);

            holder.mImage = (ImageView) convertView.findViewById(R.id.memo_image);
            holder.mName = (TextView) convertView.findViewById(R.id.memo_name);
            holder.mUpdate = (TextView) convertView.findViewById(R.id.update_date);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ListData mData = mListData.get(position);

        if (mData.mImage != 0) {
            holder.mImage.setVisibility(View.VISIBLE);
            holder.mImage.setImageResource(mData.mImage);
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

        return convertView;
    }

    public void addItem(@DrawableRes int image, String mName, String mUpdate, String mHash) {
        ListData addInfo = new ListData();
        addInfo.mImage = image;
        addInfo.mName = mName;
        addInfo.mUpdate = mUpdate;
        addInfo.mHash = mHash;

        mListData.add(addInfo);
    }

    public void remove(int position) {
        mListData.remove(position);
    }
}
