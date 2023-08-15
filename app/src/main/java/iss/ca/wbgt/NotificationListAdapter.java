package iss.ca.wbgt;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ItemView> {
    private List<NotificationModel> notificationList;

    public NotificationListAdapter(List<NotificationModel> notificationList){
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ItemView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new ItemView(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemView holder, int position) {
        holder.title.setText(notificationList.get(position).getTitle());
        holder.msgBody.setText(notificationList.get(position).getMessage());
        holder.time.setText(notificationList.get(position).getTime());
        holder.imageView.setImageResource(notificationList.get(position).getDrawable());
    }

    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ItemView extends RecyclerView.ViewHolder{
        private TextView title;
        TextView msgBody;
        TextView time;
        ImageView imageView;

        public ItemView(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            msgBody = itemView.findViewById(R.id.message_body);
            time = itemView.findViewById(R.id.timeTxt);
            imageView = itemView.findViewById(R.id.arrow_head);
        }
    }

}
