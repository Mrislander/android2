package com.elitise.appv2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PurchaseDetail extends Activity {
    @BindView(R.id.batterySeriesImage)
    ImageView batterySeriesImage;
    @BindView(R.id.batterySeries)
    TextView batterySeries;
    @BindView(R.id.modeNo)
    TextView modeNo;
    @BindView(R.id.CCAmps)
    TextView CCAmps;
    @BindView(R.id.CapacityVal)
    TextView CapacityVal;
    @BindView(R.id.VoltageVal)
    TextView VoltageVal;
    @BindView(R.id.CellTypeVal)
    TextView CellTypeVal;
    @BindView(R.id.ApplicationsVal)
    TextView ApplicationsVal;
    @BindView(R.id.FeaturesVal)
    TextView FeaturesVal;
    @BindView(R.id.DimensionVal)
    TextView DimensionVal;
    @BindView(R.id.WeightVal)
    TextView WeightVal;
    @BindView(R.id.buyBtn)
    Button buyBtn;

    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_detail);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        type = bundle.getString("MODE");
        batterySeries.setText("InduraPower "+type+" Series");
        modeNo.setText(bundle.getString("MODE"));
        CCAmps.setText(bundle.getString("CCAMPS"));
        CapacityVal.setText(bundle.getString("CAPACITY"));
        VoltageVal.setText(bundle.getString("VOLTAGE"));
        CellTypeVal.setText(bundle.getString("CELLTYPE"));
        ApplicationsVal.setText(bundle.getString("APPLICATIONS"));
        FeaturesVal.setText(bundle.getString("FEATURES"));
        DimensionVal.setText(bundle.getString("DIMENSIONS"));
        WeightVal.setText(bundle.getString("WEIGHT"));
        switch (type){
            case "108":
                batterySeriesImage.setImageResource(R.drawable.indurapower_108);
                break;
            case "110":
                batterySeriesImage.setImageResource(R.drawable.indurapower_110);
                break;
            case "120":
                batterySeriesImage.setImageResource(R.drawable.indurapower_120);
                break;
            case "210":
                batterySeriesImage.setImageResource(R.drawable.indurapower_210);
                break;
            case "220":
                batterySeriesImage.setImageResource(R.drawable.indurapower_220);
                break;
            default:
                break;
        }
//        bundle.putString("MODE","108");
//        bundle.putString("CCAMPS","350");
//        bundle.putString("CAPACITY","8 A-Hr");
//        bundle.putString("VOLTAGE","13.2V");
//        bundle.putString("CELLTYPE","LiFePO4");
//        bundle.putString("APPLICATIONS","Motorcycles,sports vehicles");
//        bundle.putString("FEATURES","short circuit protection\ncell balancing\nover/under voltage protection\nover temperature protection");
//        bundle.putString("DIMENSIONS","6.3in(L) x 3.4in(W) x 4.7in(H)");
//        bundle.putString("WEIGHT","4.6lbs(2.08kg) max");

        Window wd = this.getWindow();
        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        wd.setStatusBarColor(Color.parseColor("#000000"));


    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        Window wd = this.getWindow();
//        wd.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        wd.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        wd.setStatusBarColor(Color.parseColor("#000000"));
//        return super.onCreateOptionsMenu(menu);
//    }

    @OnClick(R.id.buyBtn)
    public void onBuyBtn(){
        Intent browserIntent;
        browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.indurapower.com/"));
        startActivity(browserIntent);
    }


}
