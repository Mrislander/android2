package com.elitise.appv2;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Andy.Xiang on 9/15/2016.
 */
public class SubModuleAdapter extends RecyclerView.Adapter<SubModuleAdapter.ViewHolder> {
    private ArrayList<SubModuleData> mData = new ArrayList<>();
    public SubModuleAdapter(ArrayList<SubModuleData> data){
        mData.addAll(data);
    }

    public void addItem(SubModuleData data){
        mData.add(data);
    }

    public SubModuleData getDatabyIdx(int i){
        return mData.get(i);
    }

    public void changeTempLebal(boolean isCelsius){
        for(SubModuleData d:mData){
            d.setTempLable(isCelsius);
        }
    }

    public void resetAlldata(boolean isCelsius){
        for(SubModuleData d:mData){
            d.resetData(isCelsius);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),R.layout.sub_module,parent,false);
        ViewHolder holder = new ViewHolder(binding.getRoot());
        holder.setBinding(binding);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.getBinding().setVariable(com.elitise.appv2.BR.data,mData.get(position));
        holder.getBinding().executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private ViewDataBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        public void setBinding(ViewDataBinding binding) {
            this.binding = binding;
        }

        public ViewDataBinding getBinding() {
            return this.binding;
        }
    }
}
