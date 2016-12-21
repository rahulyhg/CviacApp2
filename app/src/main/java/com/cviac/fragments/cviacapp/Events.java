package com.cviac.fragments.cviacapp;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.cviac.activity.cviacapp.R;
import com.cviac.adapter.cviacapp.EventsAdapter;
import com.cviac.datamodel.cviacapp.Employee;
import com.cviac.datamodel.cviacapp.Event;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class Events extends Fragment {
    private ListView lv1;
    List<Event> emps;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View events = inflater.inflate(R.layout.events_frgs, container, false);
        //((TextView)event.findViewById(R.id.events)).setText("Events");
        lv1 = (ListView) events.findViewById(R.id.eventslist);
        lv1.setDivider(null);
        emps = getEvents();
        lv1.setAdapter(new EventsAdapter(emps, getActivity().getApplicationContext()));

        lv1.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int pos1,
                                    long pos2) {

                Event emp = emps.get(pos1);

               // Toast.makeText(lv1.getContext(), "clicked:" + emp.getEvent_title(), Toast.LENGTH_SHORT).show();


            }
        });


        return events;
    }

    private List<Event> getEvents() {
        return Event.getevents();
    }




}
