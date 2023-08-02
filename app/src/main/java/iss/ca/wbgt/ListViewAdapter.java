package iss.ca.wbgt;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class ListViewAdapter extends ArrayAdapter<Object> {
    private final Context context;

    protected ArrayList<Notification> notifications = new ArrayList<Notification>();
    public ListViewAdapter(Context context, ArrayList<Notification> notifications){
        super(context, R.layout.item_notification);
        this.context = context;
        this.notifications = notifications;
        add(new Object[notifications.size()]);
    }

    public View getView(int pos, View view, @NonNull ViewGroup parent){
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_notification, parent, false);
        }
        TextView title = view.findViewById(R.id.title);
        title.setText(notifications.get(pos).getTitle());
        TextView msgBody = view.findViewById(R.id.message_body);
        msgBody.setText(notifications.get(pos).getMessage());
        TextView time = view.findViewById(R.id.timeTxt);
        time.setText(notifications.get(pos).getTime());
        ImageView imageView = view.findViewById(R.id.arrow_head);
        imageView.setImageResource(notifications.get(pos).getDrawable());
        return view;
    }
}
