package iss.ca.wbgt;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotificationFragment extends Fragment implements AdapterView.OnItemClickListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Notification Data
    private ArrayList<NotificationModel> notifications;
    File mTargetFile;
    private ArrayList<NotificationModel> notificationList = new ArrayList<NotificationModel>();
    private ArrayList<NotificationModel> notificationsTest = new ArrayList<NotificationModel>();

    public NotificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NotificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NotificationFragment newInstance(String param1, String param2) {
        NotificationFragment fragment = new NotificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_notification, container, false);
        getNotificationList();
        //store and retrieve notification data
        //store notifications to file
        String folder = "NotificationsTest";
        String fileName = "notification_list";
        mTargetFile = new File(requireContext().getFilesDir(), folder+"/"+fileName);
        writeToFile();
        readFromFile();

        ListViewAdapter adapter = new ListViewAdapter(getActivity(), notificationsTest);
        ListView listView = rootView.findViewById(R.id.notification_list);
        if(listView!=null){
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
//        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.notification_list);
//        NotificationListAdapter adapter = new NotificationListAdapter(notifications);
//        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return rootView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getContext(), "some"+position, Toast.LENGTH_SHORT).show();
    }

    private void getNotificationList(){
        notifications = new ArrayList<NotificationModel>();
        notifications.add(new NotificationModel("Title", "This is notification message body", "Aug 2 2023 10:10pm"));
        notifications.add(new NotificationModel("Title", "This is notification message body", "Aug 2 2023 10:10pm"));
        notifications.add(new NotificationModel("Title", "This is notification message body", "Aug 2 2023 10:10pm"));
    }

    //Notification
    protected void readFromFile(){
        try{
            FileInputStream fis = new FileInputStream(mTargetFile);
            DataInputStream dis = new DataInputStream(fis);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            //Assign the lines from buffer reader to strline and check if it is null
            while ((strLine = br.readLine())!= null){
                notificationsTest.add(convertStringToNotification(strLine));
            }
            dis.close();
            //mInputTxt.setText(data);
            Toast.makeText(getContext(), "Read File OK!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    protected void writeToFile(){
        ArrayList<String> notificationString = getNotificationString();
        try {
            File parent = mTargetFile.getParentFile();
            if(!parent.exists() && !parent.mkdirs()){
                throw new IllegalStateException("Could not create dir: "+ parent);
            }
            FileOutputStream fos = new FileOutputStream(mTargetFile);
            for (String notification: notificationString){
                fos.write((notification+"\n").getBytes());
            }
            fos.close();
            Toast.makeText(getContext(), "Write File OK!", Toast.LENGTH_SHORT).show();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    protected ArrayList<String> getNotificationString(){
        //initialize some notification data for testing purpose
        notificationList.add(new NotificationModel("Title1", "Body1", "Time1"));
        notificationList.add(new NotificationModel("Title2", "Body2", "Time2"));
        notificationList.add(new NotificationModel("Title3", "Body3", "Time3"));
        notificationList.add(new NotificationModel("Title4", "Body4", "Time4"));
        notificationList.add(new NotificationModel("Title5", "Body5", "Time5"));
        notificationList.add(new NotificationModel("Title6", "Body6", "Time6"));
        notificationList.add(new NotificationModel("Title7", "Body7", "Time7"));
        notificationList.add(new NotificationModel("Title8", "Body8", "Time8"));
        notificationList.add(new NotificationModel("Title6", "Body6", "Time6"));
        notificationList.add(new NotificationModel("Title7", "Body7", "Time7"));
        notificationList.add(new NotificationModel("Title8", "Body8", "Time8"));

        ArrayList<String> notificationStrings = new ArrayList<String>();

        for(NotificationModel notification: notificationList){
            String notiString = notification.getTitle()+"|"+notification.getMessage()+"|"+notification.getTime();
            notificationStrings.add(notiString);
        }
        return notificationStrings;
    }

    private NotificationModel convertStringToNotification(String notiString){
        NotificationModel notification = new NotificationModel();
        String[] stringArr = notiString.split("\\|");
        notification.setTitle(stringArr[0]);
        notification.setMessage(stringArr[1]);
        notification.setTime(stringArr[2]);

        return notification;
    }
}