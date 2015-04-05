package edu.gvsu.cis.klinefek.finalproject;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Participant;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by katherineklinefelter on 3/29/15.
 */
public class selectKillAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<Participant> players;
    private SelectorListener listener;


    public selectKillAdapter(ArrayList<Participant> gamePlayers, SelectorListener listener){
        players = gamePlayers;
    }

    private class PlayerHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public ImageView picture;


        public PlayerHolder(final View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            picture = (ImageView) itemView.findViewById(R.id.playerImg);

            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TextView t = (TextView) itemView.findViewById(R.id.name);
                    listener.onWordSelected(t.getText().toString());
                }
            });
        }

        @Override
        public void onClick(View v) {

            listener.onWordSelected(name.getText().toString());

        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.selectgroupcell,
                viewGroup, false);
        PlayerHolder holder = new PlayerHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        PlayerHolder tmp = (PlayerHolder) viewHolder;

        tmp.name.setText(players.get(i).getDisplayName());

        if(players.get(i).getIconImageUri() != null) {
            Uri picture = players.get(i).getIconImageUri();
            tmp.picture.setImageURI(picture);
        }
        else{
            //default image if the player does not have one
            tmp.picture.setImageResource(R.drawable.ic_launcher);
        }

    }


    @Override
    public int getItemCount() {
        return players.size();
    }

    public interface SelectorListener{
        void onWordSelected(String w);

    }
}
