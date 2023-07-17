package com.example.malanglinejavamvvm.model;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.example.malanglinejavamvvm.utilities.CDM;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RouteTransport {

    private PointTransport.TransportCost cost;
    private List<PointTransport> path;
    private PointTransport source;
    private PointTransport destination;
    private List<String> lineCodes;
    private List<Integer> colorCodes;

    public RouteTransport(PointTransport source,
                          PointTransport destination,
                          List<PointTransport> path) {
        this.source = source;
        this.destination = destination;
        this.path = path;
        this.cost = destination.getCost();

        this.lineCodes = new ArrayList<>();
        this.colorCodes = new ArrayList<>();

        if(path != null) {
            String prevPath = null;
            for(PointTransport p: path) {
                String dir = p.getLineName() + " " + (p.getDirection().substring(0,1).equals("O")?"\u25B6":"\u25C0");
                if(prevPath == null) {
                    prevPath = dir;
                    this.lineCodes.add(prevPath);
                    this.colorCodes.add(p.getColor());
                    continue;
                } else {
                    if (prevPath.equals(dir))
                        continue;
                    prevPath = dir;
                    this.lineCodes.add(prevPath);
                    this.colorCodes.add(p.getColor());
                }
            }
        }

    }

    public SpannableString getNames() {

        SpannableStringBuilder builder = new SpannableStringBuilder();

        int i = 0;
        for(String line: this.lineCodes) {
            SpannableString lineSpannable = new SpannableString(line);
            lineSpannable.setSpan(new ForegroundColorSpan(this.colorCodes.get(i)),
                    0, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            lineSpannable.setSpan(new RelativeSizeSpan(0.7f), line.length()-1, line.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            if(i == 0)
                builder.append(lineSpannable);
            else builder.append(" \u203A ").append(lineSpannable);
            i++;
        }

        return SpannableString.valueOf(builder);

    }

    public int getDistanceReadable() {
        return (int) (this.cost.distance / CDM.oneMeterInDegree());
    }

    public double getTotalPrice() {
        return this.cost.price + CDM.getStandardCost();
    }

    public PointTransport getSource() {
        return source;
    }

    public PointTransport getDestination() {
        return destination;
    }

    public enum ComparatorType {
        PRICE, DISTANCE
    }

    public static Comparator<RouteTransport> getComparator(ComparatorType comparator) {
        if(comparator == ComparatorType.PRICE) {
            return new java.util.Comparator<RouteTransport>() {
                @Override
                public int compare(RouteTransport o1, RouteTransport o2) {
                    if (o1.getTotalPrice() < o2.getTotalPrice()) return -1;
                    if (o1.getTotalPrice() == o2.getTotalPrice()) {
                        return Integer.compare(o1.getDistanceReadable(), o2.getDistanceReadable());
                    }
                    else return 1;
                }
            };
        } else {
            return new java.util.Comparator<RouteTransport>() {
                @Override
                public int compare(RouteTransport o1, RouteTransport o2) {
                    if(Math.abs(o1.getDistanceReadable() - o2.getDistanceReadable()) <= 100) { // less than 100 meters means equals distance
                        return Double.compare(o1.getNumLines(), o2.getNumLines());
                    }
                    else return Double.compare(o1.getDistanceReadable(), o2.getDistanceReadable());
                }
            };
        }
    }

    public List<PointTransport> getPath() {
        return this.path;
    }

    public int getNumLines() {
        return this.lineCodes.size();
    }

}