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

import com.example.tbrams.markerdemo.db.DbAdmin;
import com.example.tbrams.markerdemo.dbModel.WpItem;

import java.util.List;

import static com.example.tbrams.markerdemo.TripAdapter.TRIP_KEY;
import static com.example.tbrams.markerdemo.TripAdapter.WP_KEY;



public class WpAdapter extends RecyclerView.Adapter<WpAdapter.ViewHolder>  {
    public static final String TAG = "TBR:WpAdapter";

    private List<WpItem> mWpList;
    private Context    mContext;
    private static DbAdmin mDbAdmin;
    private String tripName;

    private static View mRootView;


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        // I need to change the backgroundcolor of the RecycleView when in Maintenance mode.
        // To do that I have to get a copy of the recyclerView itself so I can access the
        // rootView on that

        mRootView = recyclerView.getRootView();
    }


    // Constructor
    // Keep context reference and a copy of the data list
    public WpAdapter(Context context, List<WpItem> wps) {
        this.mContext = context;
        this.mWpList = wps;

        // Get a handle to the database helper and prepare the database
        mDbAdmin = new DbAdmin(mContext);
    }



    /*
     * Inflate the list item view and wrap it in a ViewHolder
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.list_element, parent, false);
        WpAdapter.ViewHolder viewHolder = new WpAdapter.ViewHolder(view);

        updateBackgroundColor();

        return viewHolder;
    }

    public static void updateBackgroundColor() {

        if (mDbAdmin.isInMaintenanceMode()) {
            mRootView.getBackground().setColorFilter(Color.parseColor("#CC0000"), PorterDuff.Mode.DARKEN);
        } else {
            if (mRootView.getBackground()!=null)
                mRootView.getBackground().clearColorFilter();
        }
    }

    /*
     * Get the object at the indicated position in the list of objects
     * and bind data to the fields of the ViewHolder.
     *
     * Also alternate the background color of the entire view
     *
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        WpItem wp = mWpList.get(position);
        holder.tvName.setText(wp.getWpName());
        holder.tvDist.setText(String.format("%.2f nm", wp.getWpDistance()));
        holder.tvDate.setText(String.format("#%d", wp.getWpSequenceNumber()));

        if (position % 2 == 1) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#A4A4A4"));
        }

    }

    @Override
    public int getItemCount() {
        return mWpList.size();
    }



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

        @Override
        public void onClick(View view) {

            WpItem wp= mWpList.get(this.getAdapterPosition());

            if (mDbAdmin.isInMaintenanceMode()) {
      //          Toast.makeText(mContext, "You selected "+wp.getWpName(), Toast.LENGTH_SHORT).show();
                Toast.makeText(mContext, "Long press to delete this Way Point", Toast.LENGTH_SHORT).show();
            } else {

                // Trip selection mode - start the map with focus on this WP

                Intent intent = new Intent(mContext, MarkerDemoActivity.class);
                intent.putExtra(TRIP_KEY, wp.getTripIndex());
                intent.putExtra(WP_KEY, wp.getWpId());
                Log.d(TAG,"Passing Trip# "+wp.getTripIndex()+" and WP #"+wp.getWpId());
                mContext.startActivity(intent);
            }

        }


        /*
         * This is the severe action - delete the Way Point
         */
        @Override
        public boolean onLongClick(View view) {

            // First get a copy of the item to be deleted
            WpItem wp= mWpList.get(this.getAdapterPosition());

            if (mDbAdmin.isInMaintenanceMode()) {

                // Drop from database and make sure trip distance is updated
                mDbAdmin.open();
                mDbAdmin.deleteWp(wp, true);
                Toast.makeText(mContext, "You deleted "+wp.getWpName(), Toast.LENGTH_SHORT).show();

                List<WpItem> newList = mDbAdmin.getAllWps(wp.getTripIndex());
                mDbAdmin.close();
                mWpList = newList;

                // Update display
                notifyDataSetChanged();

                return false;

            } else {

                // Trip Selection - guide the user

                Toast.makeText(mContext, "Change mode to Maintenance if you want to delete something", Toast.LENGTH_SHORT).show();

                return false;
            }
        }
    }
}
