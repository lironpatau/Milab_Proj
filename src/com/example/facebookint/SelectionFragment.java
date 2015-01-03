package com.example.facebookint;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;

public class SelectionFragment extends Fragment {


	private static final String TAG = "SelectionFragment";
	private ProfilePictureView profilePictureView;
	private TextView usernameView;

	@Override
	public View onCreateView(LayoutInflater inflater, 
			ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.selection, 
				container, false);

		//Find the user's profile picture custom view
		profilePictureView = (ProfilePictureView) view.findViewById(R.id.selection_profile_pic);
		profilePictureView.setCropped(true);

		//Find the user's name view
		usernameView = (TextView) view.findViewById(R.id.selection_user_name);

		//*****************step 2e****************************
		//Find the list view
		listView = (ListView) view.findViewById(R.id.selection_list);
		
		//Set up the list view items, based on a list of BaseListElement elements
		
		listElements = new ArrayList<BaseListElement>();
		//Add an item for the friend picker
		listElements.add(new PeopleListElement(0));
		///Set the list view adapter
		listView.setAdapter(new ActionListAdapter(getActivity(),
				R.id.selection_list, listElements));
		
		
		//****************end of step 2e*********************
		
		//Check for an open session
		Session session = Session.getActiveSession();
		if (session != null && session.isOpened()) {
			// Get the user's data
			makeMeRequest(session);
		}

		return view;
	}

	private void makeMeRequest (final Session session) {
		//Make an API call to get user data and define a
		//new callback to handle the response

		Request request = Request.newMeRequest(session,
				new Request.GraphUserCallback() {
			@Override
			public void onCompleted(GraphUser user, Response response){
				//if the response is successful 
				if (session == Session.getActiveSession()) {
					if (user != null) {
						//Set the id for the ProfilePictureView
						// view that in turn displays the profile picture.
						profilePictureView.setProfileId(user.getId());
						//Set the textview's text to the user's name.
						usernameView.setText(user.getName());
					}
				}
				if (response.getError() != null) {
					//Handle errors , will do so later.
				}
			}

		});
		request.executeAsync();
	}

	private void onSessionStateChange(final Session session, SessionState state ,Exception exceprion) {
		if (session != null && session.isOpened()) {
			//Get the users data.
			makeMeRequest(session);
		}
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(final Session session, final SessionState state, final Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(getActivity(), callback);
		uiHelper.onCreate(savedInstanceState);
	}

	private static final int REAUTH_ACTIVITY_CODE = 100;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REAUTH_ACTIVITY_CODE) {
			uiHelper.onActivityResult(requestCode, resultCode, data);

		} else if (requestCode == Activity.RESULT_OK) {
			//Do nothing
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		uiHelper.onResume();
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		uiHelper.onSaveInstanceState(bundle);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}


	//**************************List View Part*****************************

	private class ActionListAdapter extends ArrayAdapter<BaseListElement> {
		private List<BaseListElement> listElements;

		public ActionListAdapter (Context context , int resourceId,
				List<BaseListElement> listElements) {
			super(context, resourceId, listElements);
			this.listElements = listElements;
			//Set up as an observer for list item changes to refresh the view
			for (int i = 0; i < listElements.size(); i++) {
				listElements.get(i).setAdapter(this);

			}

		}

		@Override

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				LayoutInflater inflater = 
						(LayoutInflater) getActivity()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = inflater.inflate(R.layout.listitem, null);
			}

			BaseListElement listElement = listElements.get(position);
			if (listElement != null) {
				view.setOnClickListener(listElement.getOnClickListener());
				ImageView icon = (ImageView) view.findViewById(R.id.icon);
				TextView text1 = (TextView) view.findViewById(R.id.text1);
				TextView text2 = (TextView) view.findViewById(R.id.text2);
				if (icon != null) {
					icon.setImageDrawable(listElement.getIcon());
				}
				if (text1 != null) {
					text1.setText(listElement.getText1());
				}
				if (text2 != null) {
					text2.setText(listElement.getText2());
				}

			}
			return view;
		}
	}


	private class PeopleListElement extends BaseListElement {

		public PeopleListElement(int requestCode) {
			super(getActivity().getResources().getDrawable(R.drawable.add_friends),
					getActivity().getResources().getString(R.string.action_people),
					getActivity().getResources().getString(R.string.action_people_default),
					requestCode);
		}

		@Override
		protected View.OnClickListener getOnClickListener() {
			return new View.OnClickListener() {

				@Override
				public void onClick(View view) {
					startPickerActivity(PickerActivity.FRIEND_PICKER, getRequestCode());

				}
			};
		}
	}

	//***************************step 2e***************************
	
	private ListView listView;
	private List<BaseListElement> listElements;

	//***************************step 3c****************************

	private void startPickerActivity(Uri data, int requestCode){
		Intent intent = new Intent();
		intent.setData(data);
		intent.setClass(getActivity(), PickerActivity.class);
		startActivityForResult(intent, requestCode);
	}
}
