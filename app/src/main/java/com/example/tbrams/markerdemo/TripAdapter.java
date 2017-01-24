package com.example.tbrams.markerdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tbrams.markerdemo.data.MarkerLab;
import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.TripItem;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.ViewHolder> {

    public static final String WP_KEY = "markerdemo.tbrams.wp_key";
    public static final String TRIP_KEY = "markerdemo.tbrams.trip_key";
    public static final String TAG = "TBR:Tripadapter";

    private List<TripItem>  mTrips;
    private Context         mContext;
    private static DbAdmin    mDbAdmin;
    private final MarkerLab markerLab;
    private String tripName;

    private View mBackgroundView=null;
    private static View mRootView;


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // I need to change the backgroundcolor of the RecycleView when in Maintenance mode.
        // To do that I have to get a copy of the recyclerView itself so I can access the
        // rootView on that

        mRootView = recyclerView.getRootView();
    }

    /*
     * constructor
     * Keep context and a copy of the data list
     */
    public TripAdapter(Context context, List<TripItem> trips) {
        this.mContext = context;
        markerLab = MarkerLab.getMarkerLab(mContext);
        tripName = markerLab.getTripName();

        this.mTrips = trips;

        // Get a handle to the database helper and prepare the database
        mDbAdmin = new DbAdmin(mContext);
    }


    /*
     * Prepare the list_element layout for new list elements
     */
    @Override
    public TripAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View itemView = inflater.inflate(R.layout.list_element, parent, false);


        mBackgroundView = parent.findViewById(R.id.activity_main);

        ViewHolder viewHolder = new ViewHolder(itemView);

        updateBackgroundColor();

        return viewHolder;
    }

    /*
     * Display list item number(position) using handles from ViewHolder
     * Also alternate background color on uneven rows
     */
    @Override
    public void onBindViewHolder(TripAdapter.ViewHolder holder, int position) {
        final TripItem trip = mTrips.get(position);

        holder.tvName.setText(trip.getTripName());
        holder.tvDist.setText(String.format("%.2f nm",trip.getTripDistance()));
        holder.tvDate.setText(trip.getTripDate());

        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#A4A4A4"));
        }

    }

    public static void updateBackgroundColor() {

        if (mDbAdmin.isInMaintenanceMode()) {
            mRootView.getBackground().setColorFilter(Color.parseColor("#CC0000"), PorterDuff.Mode.DARKEN);
        } else {
            if (mRootView.getBackground()!=null)
                mRootView.getBackground().clearColorFilter();
        }
    }


    @Override
    public int getItemCount() {
        return mTrips.size();
    }


    /*
     * Get handle to fields for display
     */
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{

        public TextView tvName;
        public TextView tvDist;
        public TextView tvDate;

        public ViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tripNameText);
            tvDist = (TextView) itemView.findViewById(R.id.tripDistText);
            tvDate = (TextView) itemView.findViewById(R.id.tripDateText);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }


        /*
         * Start a detail session based on Trip Id
         */
        @Override
        public void onClick(View view) {

            TripItem trip = mTrips.get(this.getAdapterPosition());

            if (mDbAdmin.isInMaintenanceMode()) {

                // DB Maintenance mode:
                // Start Detailview and show the waypoints

                markerLab.setTripName(trip.getTripName());
                Intent intent = new Intent(mContext, DetailActivity.class);
                intent.putExtra(TRIP_KEY, trip.getTripId());
                Log.d(TAG, "Passing id: " + trip.getTripId());
                mContext.startActivity(intent);
            } else {

                // Trip Selection Mode:
                // Start MarkerDemoActivity with map at WP 0

                markerLab.setTripName(trip.getTripName());
                Log.d(TAG,"Tripadaptor - tripName set in Singleton: "+markerLab.getTripName());

                Intent intent = new Intent(mContext, MarkerDemoActivity.class);
                intent.putExtra(TRIP_KEY, trip.getTripId());
                intent.putExtra(WP_KEY, "");
                Log.d(TAG,"Passing Trip# "+trip.getTripId()+" and WP #0");
                mContext.startActivity(intent);
            }
        }



        @Override
        public boolean onLongClick(View view) {

            TripItem trip = mTrips.get(this.getAdapterPosition());

            if (mDbAdmin.isInMaintenanceMode()) {

                // Maintenance Mode:
                // Remove from the list and update the listview
                mTrips.remove(this.getAdapterPosition());
                notifyDataSetChanged();

                Toast.makeText(mContext, "You deleted " + trip.getTripName(), Toast.LENGTH_SHORT).show();

                // Drop from database
                mDbAdmin.open();
                mDbAdmin.deleteTrip(trip);
                mDbAdmin.close();

                return false;

            } else {

                // Trip Selection Mode guide the user
                Toast.makeText(mContext, "Change to Maintenance mode if you want to delete something", Toast.LENGTH_SHORT).show();
                return false;  // carry on to map view

            }
        }
    }
}