package edu.gvsu.cis.klinefek.finalproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.plus.Plus;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

//TODO check Toast and replace with alert popups when appropriate
public class MapsActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        RoomUpdateListener, RealTimeMessageReceivedListener, RoomStatusUpdateListener,
        OnInvitationReceivedListener, View.OnClickListener, RealTimeMultiplayer.ReliableMessageSentCallback{

    //instance for map
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private static final String TAG = "Assassin";
    private static final String KEY_IN_RESOLUTION = "is_in_resolution";
    private Marker myMarker;
    private boolean confirmedKill = false;
    private TextView kill;
    private TextView returnToMap;
    private TextView selectkill;
    private TextView instructions;
    private ImageView knifeImg;
    private ImageView bountyPlayer;
    private ArrayList<LatLng> killLocations;
    private ArrayList<String> killInfo;
    private ArrayList<String> killTitle;



    //game logic
    private int gameMode = 0;  //0 = not selected, 1 = free-for-all, 2 = bounty hunter
    private boolean gameStarted;
    private Participant theHunted;  //target player in bounty hunter
    private boolean gameOver;
    private FrameLayout mapDisplay;
    private FrameLayout killDisplay;
    private RecyclerView selectPlayer;
    private RecyclerView.Adapter selectPlayerAdapter;
    private RecyclerView.LayoutManager selectPlayerManager;
    private int numberOfKills;
    private ArrayList<Integer> gameResults; //0 for in progress, 1 for win, 2 for lose
                                        //the index number is the same index number
                                        //of the cooresponding participant

    //instance for multiplayer API

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation,
            R.id.button_see_invitations, R.id.freeForAll, R.id.bountyHunter
    };


    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Are we currently resolving a connection failure?
    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Set to true to automatically start the sign in flow when the Activity starts.
    // Set to false to require the user to click the button in order to sign in.
    private boolean mAutoStartSignInFlow = false;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // The participants in the currently active game
    ArrayList<Participant> players = null;

    // My participant ID in the currently active game
    String mMyId = null;


    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[2];


    //map stuff

    /**
     * Request code for auto Google Play Services error resolution.
     */
    protected static final int REQUEST_CODE_RESOLUTION = 1;

    /**
     * Google API client.
     */
    private GoogleApiClient mGoogleApiClient;

    /**
     * Determines if the client is in a resolution state, and
     * waiting for resolution intent to return.
     */
    private boolean mIsInResolution;

    /**
     * Called when the activity is starting. Restores the activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mIsInResolution = savedInstanceState.getBoolean(KEY_IN_RESOLUTION, false);
        }


        setContentView(R.layout.mapdisplay);
        setUpMapIfNeeded();

        gameStarted = false;
        kill = (TextView) findViewById(R.id.killbutton);
        returnToMap = (TextView) findViewById(R.id.returnToMap);

        killLocations = new ArrayList<LatLng>();
        killInfo = new ArrayList<String>();
        killTitle = new ArrayList<String>();
        players = new ArrayList<Participant>();
        mapDisplay = (FrameLayout) findViewById(R.id.mapfrag);
        killDisplay = (FrameLayout) findViewById(R.id.killfrag);
        selectkill = (TextView) findViewById(R.id.selectkill);
        instructions = (TextView) findViewById(R.id.inGameInstructions);
        knifeImg = (ImageView) findViewById(R.id.knifeImage);
        bountyPlayer = (ImageView) findViewById(R.id.bountyplayer);


        numberOfKills = 0;

        selectPlayer = (RecyclerView) findViewById(R.id.playerToKill);
        selectPlayerManager = new LinearLayoutManager(getApplicationContext());
        selectPlayer.setLayoutManager(selectPlayerManager);
        selectPlayerAdapter = new selectKillAdapter(players, gameResults, mMyId, new selectKillAdapter.SelectorListener() {
            @Override
            public void onWordSelected(String w) {
                //this is just so that everything is declared to prevent an error
                //when back is pushed and recyclerView has not been instantiated by
                //the onClickListener
            }
        });
        selectPlayer.setAdapter(selectPlayerAdapter);
        selectPlayerAdapter.notifyDataSetChanged();


        instructions.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                String message = "Error: Game mode not selected.";
                if (gameMode == 1){
                    //set message to Free-for-all
                    message = getString(R.string.mode1instructions);
                }
                else if(gameMode == 2) {
                    //set message to bounty hunter - the if should never happen
                    if(theHunted.getParticipantId().equals(mMyId)){
                        message = "Bounty Hunter\nYou are the target!\n" + getString(R.string.mode2instructions);
                    }
                    else {
                        message = "Bounty Hunter\nThe target is " + theHunted.getDisplayName() +".\n" + getString(R.string.mode2instructions);
                    }
                    //TODO set brief instruction strings

                }
                new AlertDialog.Builder(MapsActivity.this) //
                        .setTitle("Instructions")
                        .setMessage(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();
            }

        });



        //used to initiate kill player
        kill.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gameMode == 1) {
                    //sets adapter when kill is clicked
                    //switch from map to recyclerView
                    selectPlayer = (RecyclerView) findViewById(R.id.playerToKill);
                    selectPlayerManager = new LinearLayoutManager(getApplicationContext());
                    selectPlayer.setLayoutManager(selectPlayerManager);
                    selectPlayerAdapter = new selectKillAdapter(players, gameResults, mMyId, new selectKillAdapter.SelectorListener() {
                        @Override
                        public void onWordSelected(String w) {
                            //make sure the player trying to kill is not yet dead
                            int myIndex = 0;
                            for (int i = 0; i < players.size(); i++) {
                                if (mMyId == players.get(i).getParticipantId()) {
                                    myIndex = i;
                                    break;
                                }
                            }

                            if (gameResults.get(myIndex) == 0) {

                                //finds player who was killed in the arraylist
                                Participant playerKilled = null;
                                for (Participant p : players) {
                                    if (p.getParticipantId().equals(w)) {
                                        playerKilled = p;
                                        break;
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "You selected " + playerKilled.getDisplayName()
                                        + ".  A message" +
                                        " is being sent for confirmation.", Toast.LENGTH_LONG).show();


                                //sends message to that player to either accept or deny the kill
                                if (playerKilled != null) {
                                    mMsgBuf[0] = 'K';
                                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                                            mRoomId, playerKilled.getParticipantId());
                                } else
                                    Toast.makeText(getApplicationContext(), playerKilled.getDisplayName() + " is not a valid player.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "You are dead.  You can't " +
                                        "kill people.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                    selectPlayer.setAdapter(selectPlayerAdapter);
                    selectPlayerAdapter.notifyDataSetChanged();

                    mapDisplay.setVisibility(View.GONE);
                    kill.setVisibility(View.GONE);
                    killDisplay.setVisibility(View.VISIBLE);
                    returnToMap.setVisibility(View.VISIBLE);
                    selectkill.setVisibility(View.VISIBLE);
                } else if (gameMode == 2) {
                    //TODO game logic here
                    //and if you are theHunted, not sure what to do
                    if (!mMyId.equals(theHunted.getParticipantId())) {
                        //not sure what the person being hunted can do to win
                        //after confirmation

                        mapDisplay.setVisibility(View.GONE);
                        kill.setVisibility(View.GONE);
                        knifeImg.setVisibility(View.VISIBLE);
                        returnToMap.setVisibility(View.VISIBLE);
                        selectkill.setText("Push to Kill\n" + theHunted.getDisplayName());
                        selectkill.setVisibility(View.VISIBLE);
                        bountyPlayer.setVisibility(View.VISIBLE);
                        //TODO add an imageView here for theHunted

                        if(theHunted.getIconImageUrl() != null) {

                            String pic = theHunted.getHiResImageUrl();
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
                            if(img != null) {
                                   bountyPlayer.setImageBitmap(img);
                            }
                            else {
                                   bountyPlayer.setImageResource(R.drawable.ninjaforprofilepic);
                            }

                        }
                        else{
                            //default image if the player does not have one
                            bountyPlayer.setImageResource(R.drawable.ninjaforprofilepic);
                        }

                        bountyPlayer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mMsgBuf[0] = 'K';
                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                                        mRoomId, theHunted.getParticipantId());
                            }
                        });
                    } else {
                        //You are the hunted...you can't kill
                    }
                }
            }
        });

        returnToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //switch from recyclerView to map
                killDisplay.setVisibility(View.GONE);
                returnToMap.setVisibility(View.GONE);
                selectkill.setVisibility(View.GONE);
                mapDisplay.setVisibility(View.VISIBLE);
                kill.setVisibility(View.VISIBLE);
                knifeImg.setVisibility(View.GONE);
                bountyPlayer.setVisibility(View.GONE);
            }
        });

        // Create the Google Api Client with access to Plus and Games
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    .build();
        }

        // set up a click listener for everything else
        for (int id : CLICKABLES) {
            findViewById(id).setOnClickListener(this);
        }

        mGoogleApiClient.connect();
    }


    /**
     * Called when the Activity is made visible.
     * A connection to Play Services need to be initiated as
     * soon as the activity is visible. Registers {@code ConnectionCallbacks}
     * and {@code OnConnectionFailedListener} on the
     * activities itself.
     */
    @Override
    protected void onStart() {
        switchToScreen(R.id.screen_wait);
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected())
        {
            Log.d(TAG,"Connecting client.");
            mGoogleApiClient.connect();
        }
        super.onStart();

    }


    /**
     * Handles Google Play Services resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SELECT_PLAYERS:
                 // we got the result from the "select players" UI -- ready to create the room
                 handleSelectPlayersResult(resultCode, data);
                 break;
            case RC_INVITATION_INBOX:
                // we got the result from the "select invitation" UI (invitation inbox). We're
                // ready to accept the selected invitation:
                handleInvitationInboxResult(resultCode, data);
                break;
            case RC_WAITING_ROOM:
                // we got the result from the "waiting room" UI.
                if (resultCode == Activity.RESULT_OK) {
                    // ready to start playing
                    Log.d(TAG, "Starting game (waiting room returned OK).");
                    startGame(true);
                } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                    // player indicated that they want to leave the room
                    leaveRoom();
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Dialog was cancelled (user pressed back key, for instance). In our game,
                    // this means leaving the room too. In more elaborate games, this could mean
                    // something else (like minimizing the waiting room UI).
                    leaveRoom();
                }
                break;
            case RC_SIGN_IN:
                Log.d(TAG, "onActivityResult with requestCode == RC_SIGN_IN, responseCode="
                        + resultCode + ", intent=" + data);
                mSignInClicked = false;
                mResolvingConnectionFailure = false;
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                } else {
                    BaseGameUtils.showActivityResultError(this,requestCode,resultCode, R.string.signin_other_error);
                }
                break;
        }
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.
    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            switchToScreen(R.id.screen_main);
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());


        // create the room
        Log.d(TAG, "Creating room...");
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(this);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(this);
        rtmConfigBuilder.setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        Games.RealTimeMultiplayer.create(mGoogleApiClient, rtmConfigBuilder.build());

        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation inv = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        acceptInviteToRoom(inv.getInvitationId());
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invId);
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(this);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
        switchToScreen(R.id.screen_wait);
        keepScreenOn();
        Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());
    }

    // Activity is going to the background. We need to have player stay in room while
    // accomplishing other tasks.  Goes to sign-in screen if becomes disconnected
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            switchToScreen(R.id.screen_sign_in);
        }
        super.onStop();
    }
    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if(!gameStarted && mCurScreen != R.id.screen_main){
            switchToScreen(R.id.screen_main);
            gameMode = 0;
            return false;
        }
        else if (!gameStarted){
            gameMode = 0;
            finish();
            return false;
        }
        //returns to the main screen if on a mode selector, or viewing invitations
        else if (keyCode == KeyEvent.KEYCODE_BACK && mCurScreen == R.id.screen_sign_in ||
                R.id.screen_wait == mCurScreen || mCurScreen == R.id.invitation_popup) {
            switchToScreen(R.id.screen_main);

            return false;
        }
        //switches from the kill screen back to the map
        else if(keyCode == KeyEvent.KEYCODE_BACK && (killDisplay.getVisibility() == View.VISIBLE ||
            knifeImg.getVisibility() == View.VISIBLE)){

            killDisplay.setVisibility(View.GONE);
            returnToMap.setVisibility(View.GONE);
            selectkill.setVisibility(View.GONE);
            mapDisplay.setVisibility(View.VISIBLE);
            kill.setVisibility(View.VISIBLE);
            knifeImg.setVisibility(View.GONE);
            bountyPlayer.setVisibility(View.GONE);
            return false;
        }

        //displays warning and requires confirmation before
        //leaving the game.
        else if (keyCode == KeyEvent.KEYCODE_BACK) {
            new AlertDialog.Builder(this)
                    .setTitle("Leaving game")
                    .setMessage("Pressing back and returning to the " +
                        " previous screen will cause you to forfeit the match. Would " +
                        "you like to continue? (You can press your home key to accomplish " +
                        "other tasks)")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // leave the match
                            checkResponse(true);
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(R.drawable.ic_launcher)
                    .show();

            return false;
        }
        return super.onKeyDown(keyCode, e);
    }

    private void checkResponse(boolean leave){
        if(leave){
            mMsgBuf[0] = 'V';   //player forfeits the game
            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, mMsgBuf, mRoomId);

            leaveRoom();
            Intent finalScreen = new Intent(MapsActivity.this, ResultActivity.class);
            finalScreen.putExtra("mode", gameMode);
            finalScreen.putExtra("win", false);
            finalScreen.putExtra("kills", numberOfKills);
            startActivity(finalScreen);
            finish();
        }
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
            mRoomId = null;
            switchToScreen(R.id.screen_main);
            gameMode = 0;
        } else {
            switchToMainScreen();
            gameMode = 0;
        }
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, MIN_PLAYERS);

        // show waiting room UI
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    //send reliable message in after a few seconds in onCreate

    // Called when we get an invitation to play a game. We react by showing that to the user.
    @Override
    public void onInvitationReceived(Invitation invitation) {
        // We got an invitation to play a game! So, store it in
        // mIncomingInvitationId
        // and show the popup on the screen.

        //verifies that a game is not in progress
        mIncomingInvitationId = invitation.getInvitationId();
        ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                invitation.getInviter().getDisplayName() + " " +
                        getString(R.string.is_inviting_you));
        if(gameMode == 0) {
            switchToScreen(mCurScreen); // This will show the invitation popup
        }
    }


    @Override
    public void onInvitationRemoved(String invitationId) {
        if (mIncomingInvitationId.equals(invitationId)) {
            mIncomingInvitationId = null;
            switchToScreen(mCurScreen); // This will hide the invitation popup
        }
    }

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private void retryConnecting() {
        mIsInResolution = false;
        if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Called when {@code mGoogleApiClient} is connected.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
        LocationRequest req = new LocationRequest();
        req.setInterval(10000); /*every 10 seconds*/
        req.setFastestInterval(1000); /*how fast our app can handle the notifications */
        req.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, req, this);

        Log.d(TAG, "onConnected() called. Sign in successful!");

        Log.d(TAG, "Sign-in succeeded.");

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        if (connectionHint != null) {
            Log.d(TAG, "onConnected: connection hint provided. Checking for invite.");
            Invitation inv = connectionHint
                    .getParcelable(Multiplayer.EXTRA_INVITATION);
            if (inv != null && inv.getInvitationId() != null) {
                // retrieve and cache the invitation ID
                Log.d(TAG,"onConnected: connection hint has a room invite!");
                acceptInviteToRoom(inv.getInvitationId());
                return;
            }
        }
        switchToScreen(R.id.screen_main);
    }



    /**
     * Called when {@code mGoogleApiClient} connection is suspended.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
        retryConnecting();
    }

    /**
     * Called when {@code mGoogleApiClient} is trying to connect but failed.
     * Handle {@code result.getResolution()} if there is a resolution
     * available.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "onConnectionFailed() called. Trying to reconnect." + result.getErrorCode());
        if (mResolvingConnectionFailure) {
            Log.d(TAG, "onConnectionFailed() ignoring connection failure; already resolving.");
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = BaseGameUtils.resolveConnectionFailure(this, mGoogleApiClient,
                    result, RC_SIGN_IN, getString(R.string.signin_other_error));
        }

        switchToScreen(R.id.screen_sign_in);

        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // Show a localized error dialog.
            GooglePlayServicesUtil.getErrorDialog(
                    result.getErrorCode(), this, 0, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            retryConnecting();
                        }
                    }).show();
            return;
        }
        // If there is an existing resolution error being displayed or a resolution
        // activity has started before, do nothing and wait for resolution
        // progress to be completed.
        if (mIsInResolution) {
            return;
        }
        mIsInResolution = true;
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            retryConnecting();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        switchToScreen(R.layout.mapdisplay);

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated..
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();

        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */

    @Override
    public void onLocationChanged(Location location) {
        LatLng geoPos = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition campos = CameraPosition.builder()
                .target(geoPos)
                .zoom(15)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(campos));
        if (myMarker == null) /*if we don't have a marker yet, create and add */ {
            myMarker = mMap.addMarker(new MarkerOptions().position(geoPos)
                    .title("My Location"));

        }
        else
            myMarker.setPosition(geoPos);
    }


    // create a RoomConfigBuilder that's appropriate for your implementation
    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }


    @Override
    public void onRoomCreated(int i, Room room) {
        Log.d(TAG, "onRoomCreated(" + i + ", " + room + ")");
        if (i != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomCreated, status " + i);
            showGameError();
            return;
        }

        // show the waiting room UI
        showWaitingRoom(room);
    }

    @Override
    public void onJoinedRoom(int i, Room room) {
        Log.d(TAG, "onJoinedRoom(" + i + ", " + room + ")");
        if (i != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + i);
            showGameError();
            return;
        }
        showWaitingRoom(room);
    }

    @Override
    public void onLeftRoom(int i, String s) {
        // we have left the room; return to main screen.
        Log.d(TAG, "onLeftRoom, code " + i);
        switchToMainScreen();
    }

    @Override
    public void onRoomConnected(int i, Room room) {
        Log.d(TAG, "onRoomConnected(" + i + ", " + room + ")");
        if (i != GamesStatusCodes.STATUS_OK) {
            Log.e(TAG, "*** Error: onRoomConnected, status " + i);
            showGameError();
            return;
        }
        updateRoom(room);
    }


    @Override
    public void onRoomConnecting(Room room) {
        updateRoom(room);
    }

    @Override
    public void onRoomAutoMatching(Room room) {
        updateRoom(room);
    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerDeclined(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerJoined(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeerLeft(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        players = room.getParticipants();
        mMyId = room.getParticipantId(Games.Players.getCurrentPlayerId(mGoogleApiClient));

        gameResults = new ArrayList<Integer>();
        for(Participant p : players){
            gameResults.add(0);             //indicates active player
        }

        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");

        //sets game mode for all players
        if(gameMode != 0){

            byte[] message = new byte[25];
            message[0] = 'X';
            message[1] = (byte) gameMode;

//            if(gameMode == 2) {
//                int pickPlayer = (int) (Math.random() * players.size());
//                theHunted = players.get(pickPlayer);
//
//
//                byte[] hunted = theHunted.getParticipantId().getBytes();
//                for (int i = 0; i < hunted.length; i++) {
//                    message[i + 2] = hunted[i];
//                }
//            }

            //used to randomize who is supposed to kill whom in Bounty Hunter
            if(gameMode == 2) {

                Collections.shuffle(Arrays.asList(players));
                //randomizes the numbers in the array for number of people playing

                for(int j = 0; j < players.size(); j++) {

                    if(players.get(j).getParticipantId().equals(mMyId)){
                        if(j == players.size()-1){
                            theHunted = players.get(0);
                        }
                        else{
                            theHunted = players.get(j+1);
                        }
                    }
                    else if(j < players.size() -1) {
                        String toKill = players.get(j+1).getParticipantId();
                        byte[] hunted = toKill.getBytes();

                        for(int k = 0; k < hunted.length; k++){
                            message[k+2] = hunted[k];
                        }


                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message,
                                mRoomId, players.get(j).getParticipantId());
                    }
                    else{
                        String toKill = players.get(0).getParticipantId();
                        byte[] hunted = toKill.getBytes();

                        for(int k = 0; k < hunted.length; k++){
                            message[k+2] = hunted[k];
                        }

                        Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message,
                                mRoomId, players.get(j).getParticipantId());
                    }
                }
                String lookingFor = "The player you're looking for is " + theHunted.getDisplayName();
                //this should never happen
                String youAreTheHunted = "Everyone is after you!";

                String message2;
                if(mMyId.equals(theHunted.getParticipantId())){
                    message2 = youAreTheHunted;
                }
                else{
                    message2 = lookingFor;
                }

                new AlertDialog.Builder(MapsActivity.this) //
                        .setTitle("Bounty Hunter")
                        .setMessage(message2)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();

            }
            else if(gameMode == 1){
                Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);
            }
        }
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        mRoomId = null;
        showGameError();
    }

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        BaseGameUtils.makeSimpleDialog(this, getString(R.string.game_problem));
        switchToMainScreen();
    }

    @Override
    public void onPeersConnected(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> strings) {
        updateRoom(room);
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }

    void updateRoom(Room room) {
        if (room != null) {
            players = room.getParticipants();
        }
    }

    /*
     *Game logic
     */


    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        switchToScreen(R.layout.mapdisplay);
        gameStarted = true;


    }


    //sets a marker at the location of a kill.
    private void setKillMarker(String killer, String killed, String killedId){
        if(confirmedKill){
            LatLng geoPos = new LatLng(myMarker.getPosition().latitude, myMarker.getPosition().longitude);
            killLocations.add(geoPos);
            //this is used to store each kill location so that they can be
            //reset when orientation changes and updated for other players

            //finds info to use when setting marker message box
            String killedName;
            String otherinfo;

            otherinfo = setMarkerInfo();
            killedName = setMarkerInfo2(killer, killed);


            double lat = geoPos.latitude;
            double lon = geoPos.longitude;

            byte[] latitude = toByteArray(lat);
            byte[] longitude = toByteArray(lon);
            byte[] killedIdAr = killedId.getBytes();


            //adds marker to the map
            mMap.addMarker(new MarkerOptions()
                    .position(geoPos)
                    .title(killedName)
                    .snippet(otherinfo)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.explosionicon)));
            confirmedKill = false;


            byte[] message = new byte[200];
            message[0] = 'S';

            //lat
            for(int i = 0; i < 8; i++){
                message[i+1] = latitude[i];
            }
            //long
            for(int i = 0; i < 8; i++){
                message[i+9] = longitude[i];
            }
            //killed ID
            for(int i = 0; i < killedId.length(); i++){
                message[i+17] = killedIdAr[i];
            }


            Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient, message, mRoomId);
        }
    }


    public static byte[] toByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putDouble(value);
        return bytes;
    }

    public static double toDouble(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getDouble();
    }

    private String setMarkerInfo(){
        String info = "";
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        Date date = new Date();
        info += "Time Killed: " + dateFormat.format(date).toString();

        killInfo.add(info);
        //stores the kill info at the same index of its arrayList as the location

        return info;
    }

    private String setMarkerInfo2(String killer, String killed){
        String info = "";
        //fill this in when player storage complete

        info = killed + " was killed by " + killer;

        killTitle.add(info);
        //stores the kill info at the same index of its arrayList as the location

        return info;
    }

    private void checkGameOver() {
        int numRemaining = 0;
        for (int i : gameResults) {
            if (i == 0) {
                numRemaining++;
            }
        }
        if (numRemaining <= 1) {
            //game is over
            //return intent to final screen
            boolean win = false;
            gameOver= true;
            for (int p = 0; p < players.size(); p++) {
                if (mMyId.equals(players.get(p).getParticipantId()) && gameResults.get(p) == 0) {
                    gameResults.set(p, 1); //indicates won game
                    mMsgBuf[0] = 'O';
                    Games.RealTimeMultiplayer.sendUnreliableMessageToOthers(mGoogleApiClient,
                            mMsgBuf, mRoomId);
                    win = true;
                    leaveRoom();
                    Intent finalScreen = new Intent(MapsActivity.this, ResultActivity.class);
                    finalScreen.putExtra("mode", gameMode);
                    finalScreen.putExtra("win", true);
                    finalScreen.putExtra("kills", numberOfKills);
                    startActivity(finalScreen);
                    finish();
                }

            }
            if (!win) {
                leaveRoom();
                Intent finalScreen = new Intent(MapsActivity.this, ResultActivity.class);
                finalScreen.putExtra("mode", gameMode);
                finalScreen.putExtra("win", false);
                finalScreen.putExtra("kills", numberOfKills);
                startActivity(finalScreen);
                finish();
            }
        } else {
            int playersLeft = numRemaining - 1;
            Toast.makeText(getApplicationContext(), playersLeft + " players left  to kill.", Toast.LENGTH_LONG).show();
        }
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */


    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    @Override
    public void onRealTimeMessageReceived(RealTimeMessage rtm) {
        //Makes phone vibrate when message is received
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);


        byte[] buf = rtm.getMessageData();
        final String sender = rtm.getSenderParticipantId();
        Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

        if (buf[0] == 'K') {                //receiving message to confirm a kill
            if (gameMode == 1) {
                String senderName = "Anonymous";
                final String sendTo = sender;
                for (Participant p : players) {
                    if (p.getParticipantId().equals(sender)) {
                        senderName = p.getDisplayName();
                    }
                }
                new AlertDialog.Builder(this)
                        .setTitle("Kill Confirmation")
                        .setMessage(senderName + " claims he/she has killed you. " +
                                "Is this true?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                for (int p = 0; p < players.size(); p++) {
                                    if (mMyId.equals(players.get(p).getParticipantId())) {
                                        gameResults.set(p, 2); //indicates lost game
                                    }
                                }

                                mMsgBuf[0] = 'A';
                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                                        mRoomId, sendTo);
                                leaveRoom();
                                //intent to final screen
                                Intent finalScreen = new Intent(MapsActivity.this, ResultActivity.class);
                                finalScreen.putExtra("mode", gameMode);
                                finalScreen.putExtra("win", false);
                                finalScreen.putExtra("kills", numberOfKills);
                                startActivity(finalScreen);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mMsgBuf[0] = 'D';
                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                                        mRoomId, sendTo);
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();
            }
            else if(gameMode == 2){
                String senderName = "Anonymous";
                final String sendTo = sender;
                for (Participant p : players) {
                    if (p.getParticipantId().equals(sender)) {
                        senderName = p.getDisplayName();
                    }
                }
                new AlertDialog.Builder(this)
                        .setTitle("Kill Confirmation")
                        .setMessage(senderName + " claims he/she has killed you. " +
                                "Is this true?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                for (int p = 0; p < players.size(); p++) {
                                    if (mMyId.equals(players.get(p).getParticipantId())) {
                                        gameResults.set(p, 2); //indicates lost game
                                    }
                                }

                                byte[] message = new byte[25];
                                message[0] = 'A';

                                byte[] newTarget = theHunted.getParticipantId().getBytes();

                                for(int i = 0; i< newTarget.length; i++){
                                    message[i+1] = newTarget[i];
                                }

                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, message,
                                        mRoomId, sendTo);
                                leaveRoom();
                                //intent to final screen
                                Intent finalScreen = new Intent(MapsActivity.this, ResultActivity.class);
                                finalScreen.putExtra("mode", gameMode);
                                finalScreen.putExtra("win", false);
                                finalScreen.putExtra("kills", numberOfKills);
                                startActivity(finalScreen);
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                mMsgBuf[0] = 'D';
                                Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, null, mMsgBuf,
                                        mRoomId, sendTo);
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();
            }
        }
        else if(buf[0] == 'V'){
            int playerIndex = 0;
            String playerName = "error";

            for(int i = 0; i < players.size(); i++){
                if(sender.equals(players.get(i).getParticipantId())){
                    playerIndex = i;
                    playerName = players.get(i).getDisplayName();
                }
            }

            gameResults.set(playerIndex, 2);

            Toast.makeText(getApplicationContext(), playerName + " left the game.", Toast.LENGTH_LONG).show();
            checkGameOver();

        }
        else if(buf[0] == 'X'){
            //sets game mode
            gameMode = (int) buf[1];

            if(gameMode == 2) {
                //byte array to string
                //string to participant, set as theHunted

                int strLen = 0;
                for(int i = 0; i < buf.length; i++){
                    if(buf[i] != 0){
                        strLen++;
                    }
                }

                String huntedId = new String(Arrays.copyOfRange(buf, 2, strLen));

                for(Participant p : players){
                    if(p.getParticipantId().equals(huntedId)){
                        theHunted = p;
                        break;
                    }
                }

                String message = "The player you're looking for is " + theHunted.getDisplayName();


                new AlertDialog.Builder(MapsActivity.this) //
                        .setTitle("Bounty Hunter")
                        .setMessage(message)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setIcon(R.drawable.ic_launcher)
                        .show();
            }


        }
        else if(buf[0] == 'A'){             //receiving message that kill is accepted
            if(gameMode == 1) {
                String senderName = "Anonymous";
                for (Participant p : players) {
                    if (p.getParticipantId().equals(sender)) {
                        senderName = p.getDisplayName();
                    }
                }
                numberOfKills++;
                confirmedKill = true;
                Toast.makeText(getApplicationContext(), senderName + " was killed.", Toast.LENGTH_LONG).show();


                String myName = "Error";
                for (Participant p : players) {
                    if (p.getParticipantId().equals(mMyId)) {
                        myName = p.getDisplayName();
                        break;
                    }
                }
                setKillMarker(myName, senderName, sender);

                for (int p = 0; p < players.size(); p++) {
                    if (sender.equals(players.get(p).getParticipantId())) {
                        gameResults.set(p, 2); //indicates lost game
                    }
                }

                //switch from map to recyclerView
                killDisplay.setVisibility(View.GONE);
                returnToMap.setVisibility(View.GONE);
                selectkill.setVisibility(View.GONE);
                mapDisplay.setVisibility(View.VISIBLE);
                kill.setVisibility(View.VISIBLE);

                checkGameOver();
            }
            else if(gameMode == 2){
                int strLen = 0;
                for(int i = 0; i < buf.length; i++){
                    if(buf[i] != 0){
                        strLen++;
                    }
                }
                String newTarget = new String(Arrays.copyOfRange(buf, 1, strLen));
                for(Participant p : players){
                    if(newTarget.equals(p.getParticipantId())){
                        theHunted = p;
                        break;
                    }
                }

                numberOfKills++;
                confirmedKill = true;


                String newTar =  theHunted.getDisplayName() +
                        " is your new target";

                String myName = "Error";
                for (Participant p : players) {
                    if (p.getParticipantId().equals(mMyId)) {
                        myName = p.getDisplayName();
                        break;
                    }
                }
                setKillMarker(myName, theHunted.getDisplayName(), sender);

                for (int p = 0; p < players.size(); p++) {
                    if (sender.equals(players.get(p).getParticipantId())) {
                        gameResults.set(p, 2); //indicates lost game
                    }
                }

                //switch from map to recyclerView
                killDisplay.setVisibility(View.GONE);
                returnToMap.setVisibility(View.GONE);
                knifeImg.setVisibility(View.GONE);
                mapDisplay.setVisibility(View.VISIBLE);
                kill.setVisibility(View.VISIBLE);

                checkGameOver();

                //TODO verify this does not assign new player if game is over
                if(!gameOver) {
                    new AlertDialog.Builder(MapsActivity.this) //
                            .setTitle("New Target")
                            .setMessage(newTar)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(R.drawable.ic_launcher)
                            .show();
                }
            }
        }


        else if(buf[0] == 'D'){             //receiving message that kill is declined
            Toast.makeText(getApplicationContext(), "The player did not confirm the kill.",
                    Toast.LENGTH_LONG).show();
        }


        else if(buf[0] == 'S'){             //message received to all players set new kill marker
            double lat = toDouble(Arrays.copyOfRange(buf, 1, 9));
            double lon = toDouble(Arrays.copyOfRange(buf, 9, 17));

            int strLen = 0;
            for(int i = 0; i < buf.length; i++){
                if(buf[i] != 0){
                    strLen++;
                }
            }

            String killedId = new String(Arrays.copyOfRange(buf, 17, strLen));

            LatLng geoPos = new LatLng(lat,lon);

            String killedName;
            String otherinfo;

            String senderName = "Anonymous";
            for(Participant p : players){
                if(p.getParticipantId().equals(sender)){
                    senderName = p.getDisplayName();
                    break;
                }
            }

            String killed = "error";
            for(Participant p : players){
                if(killedId.equals(p.getParticipantId())){
                    killed = p.getDisplayName();
                    break;
                }
            }

            otherinfo = setMarkerInfo();
            killedName = setMarkerInfo2(senderName, killed);

            mMap.addMarker(new MarkerOptions()
                    .position(geoPos)
                    .title(killedName)
                    .snippet(otherinfo)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.explosionicon)));
            checkGameOver();
        }
        else if(buf[0] == 'O'){
            for (int p = 0; p < players.size(); p++) {
                if (sender == players.get(p).getParticipantId() && gameResults.get(p) == 0) {
                    gameResults.set(p, 1); //indicates won game
                    break;
                }
            }
            checkGameOver();
        }
    }


    /*
     * UI SECTION. Methods that implement the game's UI.
     */


    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;

        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else{
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        }
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            switchToScreen(R.layout.mapdisplay);
        }
        else {
            switchToScreen(R.id.screen_sign_in);
        }
    }



    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        //TODO implement game modes

        switch (v.getId()) {
            case R.id.freeForAll:
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 7);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                gameMode = 1;
                break;
            case R.id.bountyHunter:
                // play a single-player game
                // show list of invitable players
                intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 7);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_SELECT_PLAYERS);
                gameMode = 2;
                break;
            case R.id.button_sign_in:
                // user wants to sign in
                // Check to see the developer who's running this sample code read the instructions :-)
                // NOTE: this check is here only because this is a sample! Don't include this
                // check in your actual production app.
                if (!BaseGameUtils.verifySampleSetup(this, R.string.app_id)) {
                    Log.w(TAG, "*** Warning: setup problems detected. Sign in may not work!");
                }

                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                mSignInClicked = true;
                mGoogleApiClient.connect();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                mSignInClicked = false;
                Games.signOut(mGoogleApiClient);
                mGoogleApiClient.disconnect();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_see_invitations:
                // show list of pending invitations
                intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
                switchToScreen(R.id.screen_wait);
                startActivityForResult(intent, RC_INVITATION_INBOX);
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
        }
    }

    @Override
    public void onRealTimeMessageSent(int i, int i2, String s) {
        Log.d("Message Sent", "sent");

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