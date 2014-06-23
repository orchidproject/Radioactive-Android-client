package com.geoloqi.ui;

import models.InstructionV1;
import models.Player;

import com.geoloqi.mapattack.R;
import com.geoloqi.rpc.OrchidClient;
import com.geoloqi.services.SocketIOManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;


public class InstructionFragment extends Fragment {
	 private SupportMapFragment mapFragment;
	    
	 private InstructionV1 instruction;
	 private SocketIOManager sManager;
	 

	 @Override	
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

		View contentView = inflater.inflate(R.layout.task_content, container,false);
		return contentView;
	 }
	
	 @Override
	 public void onViewCreated(View view,Bundle savedInstanceState){
		 setupView();
	 }
	 
	 private void setupView(){
		 if(getView() == null){
			 return;
		 }
		 if(instruction == null || instruction.getTaskId() == -1 ){
			 ((View)getActivity().findViewById(R.id.instruction_control)).setVisibility(View.GONE);
			 ((View)getActivity().findViewById(R.id.instruction_idle)).setVisibility(View.VISIBLE);	
			 ((View)getActivity().findViewById(R.id.task_map_container)).setVisibility(View.GONE);	
		 }
		 else{
			((View)getActivity().findViewById(R.id.instruction_control)).setVisibility(View.VISIBLE);
			((View)getActivity().findViewById(R.id.instruction_idle)).setVisibility(View.GONE);
			((View)getActivity().findViewById(R.id.task_map_container)).setVisibility(View.VISIBLE);
			setupMap();
			setupButton();
		 }
	 }
	 
	 private void setupMap(){
		 
		    mapFragment =  new SupportMapFragment() {
		        @Override
		        public void onActivityCreated(Bundle savedInstanceState) {
		            super.onActivityCreated(savedInstanceState);
		            final GoogleMap sMap = this.getMap();
		            sMap.getUiSettings().setAllGesturesEnabled(false);
		            sMap.getUiSettings().setZoomControlsEnabled(false);
	  
		            if(instruction!=null){
		            	instruction.getPlayer().updateOnStaticMap(sMap);
		            	instruction.getTeammate().updateOnStaticMap(sMap);
		            	instruction.getTaskObj().updateOnStaticMap(sMap);
		            	PolylineOptions op = new PolylineOptions()
		            		.add(instruction.getPlayer().getLatLng())
		            		.add(instruction.getTaskObj().getLatLng());
		            	sMap.addPolyline(op);
		            	
		            	op = new PolylineOptions()
	            			.add(instruction.getTeammate().getLatLng())
	            			.add(instruction.getTaskObj().getLatLng());
		            	sMap.addPolyline(op);
		            }
		           
		            
		            sMap.setOnMapLoadedCallback(new OnMapLoadedCallback(){

						@Override
						public void onMapLoaded() {
							LatLngBounds.Builder  builder = new LatLngBounds.Builder();
				        	
				        	
				        	if(instruction!=null){
				        		builder.include(instruction.getPlayer().getLatLng());
				        		builder.include(instruction.getTeammate().getLatLng());
				        		builder.include(instruction.getTaskObj().getLatLng()); 
				        	}
				        	sMap.moveCamera(
					    		CameraUpdateFactory.newLatLngBounds(builder.build(), 50)
					    	);
							
						}});
		        }
		        
		      
		    };
		    
			getActivity().getSupportFragmentManager().beginTransaction()
			.replace(R.id.task_map_container, mapFragment).commit();
			
	}
	 
	 private void setupButton(){
			Button accept = (Button)getActivity().findViewById(R.id.instruction_accept);
	        Button reject = (Button)getActivity().findViewById(R.id.instruction_reject);
	        TextView status = (TextView)getActivity().findViewById(R.id.instruction_status);
	        int status_code = instruction.getStatus();
	        switch(status_code){
	        	case 1:
	            	status.setText("Waiting for your response");
	            	break;
	        	case 2:
	            	status.setText("Accepted");
	            	accept.setEnabled(false);
	            	reject.setEnabled(true);
	            	break;
	        	case 3:
	            	status.setText("Rejected");
	            	accept.setEnabled(true);
	            	reject.setEnabled(false);
	            	break;
	        	case 4: 
	        		//status.setText("discarded");
	        		break;
	        }
	        
	        accept.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					sManager.sendInstructionAck(
							instruction.getId(), 
							2,
							OrchidClient.getApplicationClient(getActivity()).getPlayerId()
					);
				}});
        
	        	reject.setOnClickListener(new OnClickListener(){

				@Override
	        	public void onClick(View arg0) {
	        		sManager.sendInstructionAck(
						instruction.getId(), 
						3,
						OrchidClient.getApplicationClient(getActivity()).getPlayerId()
	        		);
	        	}});
	 }
	 
	 public void setInstruction(InstructionV1 in){
		 instruction = in;
		 setupView();
	 }
	 
	 public void setIOManger(SocketIOManager sm){
		 sManager = sm;
	 }
	 
	 public void onHiddenChanged(boolean hidden) {
		 	if(mapFragment == null) return;
		 	
			if(hidden){
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.beginTransaction().detach( mapFragment).commit();
			}
			else{
				FragmentManager fm = getActivity().getSupportFragmentManager();
				fm.beginTransaction().attach( mapFragment).commit();
			}
	 }

	public void ackInstruction(int id, Player p, int status) {
		if(instruction == null) return;
		int current_player_id = OrchidClient.getApplicationClient(getActivity()).getPlayerId();
		if(instruction.getId() == id && p.getId() == current_player_id){
			instruction.setStatus(status);
		}
		setupButton();
	}
	
	

}
