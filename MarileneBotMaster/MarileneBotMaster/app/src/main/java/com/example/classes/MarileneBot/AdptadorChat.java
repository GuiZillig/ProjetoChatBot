package com.example.classes.MarileneBot;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AdptadorChat extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Activity activity;
    private int SELF = 100;
    private ArrayList<Message> messageArrayList;


    public AdptadorChat(ArrayList<Message> messageArrayList) {
        this.messageArrayList = messageArrayList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        // Indentificador de onde renderizar o chat sendo esquerda ou direita

        if (viewType == SELF) {

            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_self, parent, false);
        } else {
            // MarileneBot messagem
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_watson, parent, false);
        }


        return new ViewHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messageArrayList.get(position);
        if (message.getId() != null && message.getId().equals("1")) {
            return SELF;
        }

        return position;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Message message = messageArrayList.get(position);
        switch (message.type) {
            case TEXT:
                ((ViewHolder) holder).message.setText(message.getMessage());
                break;
            case IMAGE:
                ((ViewHolder) holder).message.setVisibility(View.GONE);
                ImageView iv = ((ViewHolder) holder).image;
                Glide
                        .with(iv.getContext())
                        .load(message.getUrl())
                        .into(iv);
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView image;

        public ViewHolder(View view) {
            super(view);
            message = (TextView) itemView.findViewById(R.id.message);
            image = (ImageView) itemView.findViewById(R.id.image);
            String customFont = "Montserrat-Regular.ttf";
            Typeface typeface = Typeface.createFromAsset(itemView.getContext().getAssets(), customFont);
            message.setTypeface(typeface);

        }
    }


}