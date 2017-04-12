package com.elitise.appv2;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PurchaseFragment extends Fragment {

    private ListView mPurchase;
    private ItemAdapter mAdapter;
    private UserData mUser = UserData.getInstance();

    public PurchaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_purchase, container, false);

        mAdapter =  new ItemAdapter(getActivity(),mUser.getBatteries());
        mPurchase = (ListView) v.findViewById(R.id.purchase_fragment);
        mPurchase.setAdapter(mAdapter);
        mPurchase.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent DetailIntent;
                Bundle bundle = new Bundle();
                switch (position) {
                    case 0:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","108");
                        bundle.putString("CCAMPS","350 CCA");
                        bundle.putString("CAPACITY","8 A-Hr");
                        bundle.putString("VOLTAGE","12.8V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","Motorcycles,sports vehicles");
                        bundle.putString("FEATURES","short circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 4.7in(H)");
                        bundle.putString("WEIGHT","4.6lbs(2.08kg) max");
                        break;
                    case 1:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","110");
                        bundle.putString("CCAMPS","280 CCA");
                        bundle.putString("CAPACITY","10 A-Hr");
                        bundle.putString("VOLTAGE","12.8V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","Compact cars");
                        bundle.putString("FEATURES","short circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 4.7in(H)");
                        bundle.putString("WEIGHT","4.6lbs(2.08kg) max");
                        break;
                    case 2:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","120");
                        bundle.putString("CCAMPS","560 CCA");
                        bundle.putString("CAPACITY","20 A-Hr");
                        bundle.putString("VOLTAGE","12.8V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","Compact cars.sedans");
                        bundle.putString("FEATURES","short circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 7.8in(H)");
                        bundle.putString("WEIGHT","8lbs(3.63kg) max");
                        break;
                    case 3:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","210");
                        bundle.putString("CCAMPS","560 CCA");
                        bundle.putString("CAPACITY","20 A-Hr");
                        bundle.putString("VOLTAGE","12.8 V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","trucks,SUVS,vans");
                        bundle.putString("FEATURES","battery redundancy\nshort circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 4.7in(H)");
                        bundle.putString("WEIGHT","9.2lbs(4.16kg) max");
                        break;
                    case 4:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","220");
                        bundle.putString("CCAMPS","1100 CCA");
                        bundle.putString("CAPACITY","40 A-Hr");
                        bundle.putString("VOLTAGE","12.8V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","trucks,SUVs,vans,recreational vehicles");
                        bundle.putString("FEATURES","battery redundancy\nshort circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 6.8in(W) x 4.7in(H)");
                        bundle.putString("WEIGHT","9.2lbs(4.16kg) max");
                        break;
                    default:
                        DetailIntent = new Intent(getActivity(), PurchaseDetail.class);
                        bundle.putString("MODE","108");
                        bundle.putString("CCAMPS","350");
                        bundle.putString("CAPACITY","8 A-Hr");
                        bundle.putString("VOLTAGE","13.2V");
                        bundle.putString("CELLTYPE","LiFePO4");
                        bundle.putString("APPLICATIONS","Motorcycles,sports vehicles");
                        bundle.putString("FEATURES","short circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
                        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 4.7in(H)");
                        bundle.putString("WEIGHT","4.6lbs(2.08kg) max");
                        break;
                  }
                  DetailIntent.putExtras(bundle);
                startActivity(DetailIntent);
            }
        });
        return v;

    }

    public class ItemAdapter extends ArrayAdapter<UserData.BatteriesInventory> {

        public ItemAdapter(Context context, ArrayList<UserData.BatteriesInventory> objects) {

            super(context, 0, objects);

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null){
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.purchaselistitems,parent,false);
            }

            TextView mName = (TextView) convertView.findViewById(R.id.mBname);
            TextView mSerial = (TextView) convertView.findViewById(R.id.mBserial);
            ImageView mBimage = (ImageView) convertView.findViewById(R.id.mBimage);
            UserData.BatteriesInventory item = getItem(position);

            switch (item.mCnt) {
                case 1:
                    mBimage.setImageResource(R.drawable.indurapower_108);
                    break;
                case 2:
                    mBimage.setImageResource(R.drawable.indurapower_110);
                    break;
                case 3:
                    mBimage.setImageResource(R.drawable.indurapower_120);
                    break;
                case 4:
                    mBimage.setImageResource(R.drawable.indurapower_210);
                    break;
                case 5:
                    mBimage.setImageResource(R.drawable.indurapower_220);
                    break;
                default:
                    break;
            }

            mName.setText(item.mType);
            mSerial.setText(item.mDescription);
            return convertView;
        }
    }
}
