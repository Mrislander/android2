package com.elitise.appv2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.util.ArrayList;


/**
 * Created by andy on 5/9/16.
 */
public class ListViewFragment extends Fragment {

    private SwipeMenuListView mListBatteries;
    private UserData mUser = UserData.getInstance();
    private ItemAdapter adapter;

    public ItemAdapter getAdapter() {
        return adapter;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.list_batteries, container, false);


        mListBatteries = (SwipeMenuListView) view.findViewById(R.id.listView);



        adapter = new ItemAdapter(getActivity(),mUser.getBatteriesList());

        SwipeMenuCreator mMenu = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem renameItem = new SwipeMenuItem(getActivity());
                renameItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                renameItem.setWidth(dp2px(100));
                renameItem.setTitle("Rename");
                renameItem.setTitleSize(18);
                renameItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(renameItem);

                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity());
                deleteItem.setBackground(new ColorDrawable(Color.RED));
                deleteItem.setWidth(dp2px(100));
                deleteItem.setTitle("Delete");
                deleteItem.setTitleSize(18);
                deleteItem.setIcon(R.drawable.ic_delete);
                deleteItem.setTitleColor(Color.WHITE);
                menu.addMenuItem(deleteItem);

            }
        };
        mListBatteries.setMenuCreator(mMenu);
        mListBatteries.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {
                switch (index) {
                    case 0:
                        new MaterialDialog.Builder(getActivity())
                                .title("Battery Name")
                                .titleColor(Color.parseColor("#ff6529"))
                                .theme(Theme.LIGHT)
                                .inputRangeRes(0, -1, R.color.material_drawer_dark_background)
                                .content("What would you like your battery to be renamed?")
                                .negativeText("Cancel")
                                .negativeColor(Color.parseColor("#ff6529"))
                                .positiveText("OK")
                                .positiveColor(Color.parseColor("#ff6529"))
                                .titleGravity(GravityEnum.CENTER)
                                .input(null, null, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                            mUser.getBatteriesList().get(position).mName=input.toString();
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                       dialog.dismiss();
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(MaterialDialog dialog, DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .show();

                        break;
                    case 1:
                        // delete
                        delete(position);
                        adapter.notifyDataSetChanged();
                        break;
                    default:
                        // delete
                        break;
                }
                // false : close the menu; true : not close the menu
                return false;
            }
        });
        mListBatteries.setAdapter(adapter);
        mListBatteries.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(mUser.getBatteriesList().get(position).mImageId!=13) {
                    Intent I = new Intent(view.getContext(), Peripheral.class);
                    I.putExtra("mBname", mUser.getBatteriesList().get(position).mName);
                    I.putExtra("idx", position);
                    mUser.setCurrentIdx(mUser.getBatteriesList().get(position).mImageId);
                    startActivity(I);
                }else{
                    Intent I = new Intent(view.getContext(), DeviceBatteryActivity.class);
                    I.putExtra("mBname", mUser.getBatteriesList().get(position).mName);
                    I.putExtra("idx", position);
                    mUser.setCurrentIdx(mUser.getBatteriesList().get(position).mImageId);
                    startActivity(I);
                }
            }
        });
        return view;
    }

    public class ItemAdapter extends ArrayAdapter<UserData.OwnedBattery> {

        public ItemAdapter(Context context, ArrayList<UserData.OwnedBattery> objects) {

            super(context, 0, objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.listviewitems,parent,false);
            }

            TextView mName = (TextView) convertView.findViewById(R.id.mBname);
            TextView mSerial = (TextView) convertView.findViewById(R.id.mBserial);
            TextView mMacAddress = (TextView) convertView.findViewById(R.id.macAddress);
            ImageView mBimage = (ImageView) convertView.findViewById(R.id.mBimage);

            UserData.OwnedBattery item = getItem(position);

            mName.setText(item.mName);
            mSerial.setText("TYPE: "+item.mType);
            if(item.getMac()!=null) {
                mMacAddress.setText("MAC: " + item.getMac());
            }
            switch (item.mImageId){
                case 1:
                    mBimage.setImageResource(R.drawable.car193_orange);
                    break;
                case 2:
                    mBimage.setImageResource(R.drawable.car194_orange);
                    break;
                case 3:
                    mBimage.setImageResource(R.drawable.city_car_orange);
                    break;
                case 4:
                    mBimage.setImageResource(R.drawable.truck_orange);
                    break;
                case 5:
                    mBimage.setImageResource(R.drawable.van_orange);
                    break;
                case 6:
                    mBimage.setImageResource(R.drawable.sports_bike_orange);
                    break;
                case 7:
                    Bitmap bitmap = BitmapFactory.decodeByteArray(mUser.getBatteriesList().get(position).getImage(), 0, mUser.getBatteriesList().get(position).getImage().length);
                    mBimage.setImageBitmap(bitmap);
                    break;
                case 8:
                    mBimage.setImageResource(R.drawable.car193_orange);
                    break;
                case 9:
                    mBimage.setImageResource(R.drawable.car193_orange);
                    break;
                case 10:
                    mBimage.setImageResource(R.drawable.car193_orange);
                    break;
                case 11:
                    mBimage.setImageResource(R.drawable.demo_battery2_icon);
                    mUser.getBatteriesList().get(position).isDemo = true;
                    mUser.getBatteriesList().get(position).isBonded = true;
                    mUser.getBatteriesList().get(position).setmSN("a23456d");
                    break;
                case 12:
                    mBimage.setImageResource(R.drawable.deep_cycle_battery_icon);
                    break;
                case 13:
                    Bitmap bitmap2 = BitmapFactory.decodeByteArray(mUser.getBatteriesList().get(position).getImage(), 0, mUser.getBatteriesList().get(position).getImage().length);
                    mBimage.setImageBitmap(bitmap2);
                    break;
                default:
                    break;
            }


            return convertView;

        }
    }

    private  int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics());
    }

    private void delete(int idx){
        mUser.getBatteriesList().remove(idx);
    }

}
