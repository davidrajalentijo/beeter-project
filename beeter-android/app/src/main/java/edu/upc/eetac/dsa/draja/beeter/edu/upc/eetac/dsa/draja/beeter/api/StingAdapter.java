package edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.upc.eetac.dsa.draja.beeter.R;
import edu.upc.eetac.dsa.draja.beeter.edu.upc.eetac.dsa.draja.beeter.api.Sting;

/**
 * Created by david on 19/11/2014.
 */

public class StingAdapter extends BaseAdapter{

private ArrayList<Sting>data;
    private LayoutInflater inflater;
public StingAdapter(Context context, ArrayList<Sting> data) {
        super();
        inflater = LayoutInflater.from(context);
        this.data = data;
        }

private static class ViewHolder {
    TextView tvSubject;
    TextView tvUsername;
    TextView tvDate;
}


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((Sting) getItem(position)).getStingid();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_row_sting, null);
            //creas un viewholder donde vas asociando los elementos definidos en el layout
            viewHolder = new ViewHolder();
            viewHolder.tvSubject = (TextView) convertView
                    .findViewById(R.id.tvSubject);
            viewHolder.tvUsername = (TextView) convertView
                    .findViewById(R.id.tvUsername);
            viewHolder.tvDate = (TextView) convertView
                    .findViewById(R.id.tvDate);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //le doy el valor a cada uno
        String subject = data.get(position).getSubject();
        String username = data.get(position).getUsername();
        String date = SimpleDateFormat.getInstance().format(
                data.get(position).getLastModified());
        viewHolder.tvSubject.setText(subject);
        viewHolder.tvUsername.setText(username);
        viewHolder.tvDate.setText(date);
        return convertView;
    }


}