package app.ssm.duck.duckapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.androidquery.AQuery;

import java.util.ArrayList;

/**
 * Created by broDuck on 16. 1. 6.
 */
public class ListViewAdapter extends BaseAdapter {
    private Context mContext = null;
    private ArrayList<ListData> mListData = new ArrayList<ListData>();
    private AQuery aq;
    private UserInfo userInfo;

    public ListViewAdapter(Context mContext, UserInfo userInfo) {
        super();
        this.mContext = mContext;
        this.aq = new AQuery(mContext);
        this.userInfo = userInfo;
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

            aq.id(holder.mImage).image("http://210.118.64.177:8080/resources/images/" + userInfo.getId() + "/" + mData.mImage);

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
                AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
                alt_bld.setMessage("메모를 삭제하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("예", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DeleteMemo deleteMemo = new DeleteMemo(mData.mHash);
                                deleteMemo.execute();

                                remove(position);

                                Toast.makeText(mContext, "메모가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alt_bld.create();
                alertDialog.setTitle("메모 삭제");
                alertDialog.setIcon(R.drawable.ic_delete_black_36dp);
                alertDialog.show();
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
            notifyDataSetChanged();
        }
    }

    public void remove(int position) {
        mListData.remove(position);
        notifyDataSetChanged();
    }

}
