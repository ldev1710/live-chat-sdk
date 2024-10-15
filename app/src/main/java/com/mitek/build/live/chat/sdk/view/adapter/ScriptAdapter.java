package com.mitek.build.live.chat.sdk.view.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.mitek.build.live.chat.sdk.R;
import com.mitek.build.live.chat.sdk.core.LiveChatFactory;
import com.mitek.build.live.chat.sdk.listener.observe.OnClickObserve;
import com.mitek.build.live.chat.sdk.model.chat.LCMessageSend;
import com.mitek.build.live.chat.sdk.model.internal.LCButtonAction;

import java.util.ArrayList;

public class ScriptAdapter extends RecyclerView.Adapter<ScriptAdapter.ViewHolder> {

    Context context;
    ArrayList<LCButtonAction> arrayList;
    OnClickObserve onClickObserve;

    public ScriptAdapter(Context context, ArrayList<LCButtonAction> arrayList,OnClickObserve onClickObserve) {
        this.context = context;
        this.arrayList = arrayList;
        this.onClickObserve = onClickObserve;
    }

    @Override
    public ScriptAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_script, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScriptAdapter.ViewHolder holder, int position) {
        LCButtonAction buttonAction = arrayList.get(position);
        holder.title.setText(buttonAction.getTextSend());
        holder.title.setOnClickListener(view -> {
            LiveChatFactory.INSTANCE.sendScriptMessage(new LCMessageSend(buttonAction.getTextSend()),buttonAction.getNextId());
            onClickObserve.onClick(buttonAction);
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_script);
        }
    }
}
