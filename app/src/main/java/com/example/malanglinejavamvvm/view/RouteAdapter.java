package com.example.malanglinejavamvvm.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.malanglinejavamvvm.R;
import com.example.malanglinejavamvvm.model.PointTransport;
import com.example.malanglinejavamvvm.model.RouteTransport;
import com.example.malanglinejavamvvm.utilities.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private RouteAdapterItemClickListener listener;
    private ArrayList<RouteTransport> routes;


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvRouteName;
        TextView tvPrice;
        TextView tvDistance;
        ImageView ivLine;
        View itemView;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.tvRouteName = itemView.findViewById(R.id.route_name);
            this.tvPrice = itemView.findViewById(R.id.route_price);
            this.tvDistance = itemView.findViewById(R.id.route_distance);
            this.ivLine = itemView.findViewById(R.id.route_icon);
        }

        void bind(final RouteTransport routeTransport, final RouteAdapterItemClickListener listener) {
            this.tvRouteName.setText(routeTransport.getNames());
            this.tvDistance.setText(Helper.humanReadableDistane(routeTransport.getDistanceReadable()));
            String priceLabel = "Rp " + String.format(Locale.getDefault(), "%,.0f", routeTransport.getTotalPrice()).replace(",", ".");
            this.tvPrice.setText(priceLabel);
            this.ivLine.setColorFilter(Color.parseColor("#2196f3"));
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClick(routeTransport);
                }
            });
        }
    }

    public interface RouteAdapterItemClickListener {
        void onItemClick(RouteTransport routeTransport);
    }

    RouteAdapter(ArrayList<RouteTransport> routes, RouteAdapterItemClickListener listener) {
        this.routes = routes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_route_selected, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RouteTransport routeTransport = routes.get(position);
        holder.bind(routeTransport, listener);
    }

    @Override
    public int getItemCount() {
        return this.routes.size();
    }
    @SuppressLint("NotifyDataSetChanged")
    public void setRoutes(ArrayList<RouteTransport> routes) {
        this.routes = routes;
        notifyDataSetChanged();
    }

    // Retrieve the routes
    public ArrayList<RouteTransport> getRoutes() {
        return routes;
    }

}
