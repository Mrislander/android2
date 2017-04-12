package com.elitise.appv2;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.GridView;

/**
 * Created by andy on 5/10/16.
 */
public class NewBatteryFragment extends Fragment implements View.OnClickListener {

    private GridLayout gridView;
    private selectResultListener selectResultListener;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().findViewById(R.id.imageButton1).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton2).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton3).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton4).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton5).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton6).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton7).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton8).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton9).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton10).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton11).setOnClickListener(this);
        getActivity().findViewById(R.id.imageButton12).setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_add_battery_image, container, false);

        gridView = (GridLayout)view.findViewById(R.id.gridview);

        return  view;
    }




    @Override
    public void onAttach(Activity acitvity) {
        super.onAttach(acitvity);
        try{
            selectResultListener = (selectResultListener) acitvity;
        }catch (ClassCastException e){
            throw new ClassCastException(getActivity().toString()+ "must implement selectResultEvent");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        selectResultListener = null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imageButton1:
                selectResultListener.getResult(1);
                break;
            case R.id.imageButton2:
                selectResultListener.getResult(2);
                break;
            case R.id.imageButton3:
                selectResultListener.getResult(3);
                break;
            case R.id.imageButton4:
                selectResultListener.getResult(4);
                break;
            case R.id.imageButton5:
                selectResultListener.getResult(5);
                break;
            case R.id.imageButton6:
                selectResultListener.getResult(6);
                break;
            case R.id.imageButton7:
                selectResultListener.getResult(7);
                break;
//            case R.id.imageButton8:
//                selectResultListener.getResult(8);
//                break;
//            case R.id.imageButton9:
//                selectResultListener.getResult(9);
//                break;
//            case R.id.imageButton10:
//                selectResultListener.getResult(10);
//                break;
//            case R.id.imageButton11:
//                selectResultListener.getResult(11);
//                break;
            case R.id.imageButton12:
                selectResultListener.getResult(12);
                break;
        }


    }



    public interface selectResultListener{
        public void getResult(int idx);
    }

}
