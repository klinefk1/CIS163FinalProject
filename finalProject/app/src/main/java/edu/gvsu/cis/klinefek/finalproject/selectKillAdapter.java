package edu.gvsu.cis.klinefek.finalproject;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.multiplayer.Participant;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by katherineklinefelter on 3/29/15.
 */
public class selectKillAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private ArrayList<Participant> players;
    private ArrayList<Integer> gameResults;
    private SelectorListener killListener;
    private String myId;

    private ArrayList<Participant>livingOpponents;


    public selectKillAdapter(ArrayList<Participant> gamePlayers, ArrayList<Integer> results,
                             String id, SelectorListener listener){
        players = gamePlayers;
        killListener = listener;
        gameResults = results;
        myId = id;
        livingOpponents = new ArrayList<Participant>();

        for(int i = 0; i < players.size(); i++) {
            if (gameResults.get(i) == 0 && !players.get(i).getParticipantId().equals(myId)) {
                livingOpponents.add(players.get(i));
            }
        }
    }

    private class PlayerHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView name;
        public ImageView picture;
        public TextView playerId;


        public PlayerHolder(final View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.name);
            picture = (ImageView) itemView.findViewById(R.id.playerImg);
            playerId = (TextView) itemView.findViewById(R.id.playerId);


            itemView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    TextView t = (TextView) itemView.findViewById(R.id.playerId);
                    killListener.onWordSelected(t.getText().toString());
                }
            });

        }

        @Override
        public void onClick(View v) {

            killListener.onWordSelected(name.getText().toString());

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

        //checks that the player is not already dead
        //and is not the user
        PlayerHolder tmp = (PlayerHolder) viewHolder;
        tmp.name.setText(livingOpponents.get(i).getDisplayName());
        tmp.playerId.setText(livingOpponents.get(i).getParticipantId());

        if(livingOpponents.get(i).getIconImageUrl() != null) {

            String pic = livingOpponents.get(i).getIconImageUrl();
            Bitmap img = null;
            try {
                img = new ImageSetTask().execute(pic).get();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (ExecutionException e) {
                e.printStackTrace();
            }
            if(img != null)
                tmp.picture.setImageBitmap(img);
            else
                tmp.picture.setImageResource(R.drawable.ic_launcher);

            }
        else{
            //default image if the player does not have one
            tmp.picture.setImageResource(R.drawable.ic_launcher);
        }
    }

    @Override
    public int getItemCount() {
        return livingOpponents.size();
    }

    public interface SelectorListener{
        void onWordSelected(String w);

    }

    public class ImageSetTask extends AsyncTask<String, Void, Bitmap> {
        //loads the poster image of a given TV show or movie and sets it
        //in the imageView on the UI

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                URL urlConnection = new URL(params[0]);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);

            try {

            }
            catch (Exception e){

            }
        }

    }
}
