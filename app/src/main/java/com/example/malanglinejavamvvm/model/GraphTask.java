package com.example.malanglinejavamvvm.model;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Set;

public class GraphTask extends AsyncTask<Object, Object, Set<PointTransport>> {

    private ArrayList<Line> lines;
    private ArrayList<Interchange> interchanges;
    private IGraphTask listener;

    public interface IGraphTask {
        void onGraphGenerated(Set<PointTransport> points);
    }

    public GraphTask(ArrayList<Line> lines, ArrayList<Interchange> interchanges, IGraphTask listener) {
        this.lines = lines;
        this.interchanges = interchanges;
        this.listener = listener;
    }

    @Override
    protected Set<PointTransport> doInBackground(Object... voids) {
        return GraphTransport.build(this.lines, this.interchanges);
    }

    @Override
    protected void onPostExecute(Set<PointTransport> points) {
        if(this.listener != null)
            this.listener.onGraphGenerated(points);
        super.onPostExecute(points);
    }


}
