package com.example.androidclient;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder > {

    Context ct;
    ArrayList<Message> messages;

    public MyAdapter(Context ct,  ArrayList<Message> s1) {
        this.ct = ct;
        this.messages = s1;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(ct);
        View view;
        if(MessageType.MESSAGE_RECEIVED.ordinal() == viewType) {
            view = inflater.inflate(R.layout.my_row, parent, false);
        } else {
            view = inflater.inflate(R.layout.my_row_2, parent, false);
        }

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.title.setText(messages.get(position).getMessage());
        //holder.description.setText(data2[position]);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

      TextView title;

      public MyViewHolder(@NonNull View itemView) {
          super(itemView);
          title = itemView.findViewById(R.id.prog_lang_title);
      }
  }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getType().ordinal();
    }
}
