package org.geometerplus.android.fbreader.network.bookshare;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.bookshare.net.BookshareWebservice;
import org.geometerplus.zlibrary.ui.android.R;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Login page for the webservice account of Bookshare.
 * Webservice login is mandatory for accessing generally
 * available services of bookshare API. This includes
 * searching and downloading public domain books.
 */
public class Bookshare_Webservice_Login extends Activity{

	private String BOOKSHARE_URL = "https://api.bookshare.org/book/searchFTS/title/*potter*";
	private String FORGOT_PW_URL = "http://www.bookshare.org/forgotPassword";
	private String SIGNUP_URL = "https://www.bookshare.org/signUpType";

	private Button btn_login;
	private Button btn_reset;
	private Button btn_continue_without_login;
	private Button btn_free_content;
	private TextView text_username;
	private TextView text_password;
	private EditText editText_username;
	private EditText editText_password;
	private Intent intent;
	private final static int LOGIN_SUCCESSFUL = 1;
	private final static int LOGIN_FAILED = -1;
	private final static int NETWORK_ERROR = -2;

	private String username;
	private String password;
	private int status;
	private ProgressDialog pd_spinning;
	private boolean isFree= false;
	private String developerKey = BookshareDeveloperKey.DEVELOPER_KEY;
	private boolean isOM = false;
	private String response;
	

	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		// Obtain the SharedPreferences object shared across the application
		SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		username = login_preference.getString("username", "");
		password = login_preference.getString("password", "");
		
		// If login credentials are already stored, navigate to the next Activity
		if(!username.equals("") && !password.equals("")){
			intent = new Intent(getApplicationContext(), Bookshare_Menu.class);
			intent.putExtra("username", username);
			intent.putExtra("password", password);
			startActivity(intent);
			finish();
		}
		
		// Remove the title bar
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		// Set to full screen
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.bookshare_webservice_login);

		btn_login = (Button)findViewById(R.id.btn_bookshare_bookshare_webservice_login);
		btn_reset = (Button)findViewById(R.id.btn_bookshare_bookshare_webservice_password);
		btn_continue_without_login = (Button)findViewById(R.id.btn_bookshare_bookshare_continue_without_login);
		
		text_username = (TextView)findViewById(R.id.bookshare_login_username_text);
		text_password = (TextView)findViewById(R.id.bookshare_login_password_text);
		editText_username = (EditText)findViewById(R.id.bookshare_login_username_edit_text);
		editText_password = (EditText)findViewById(R.id.bookshare_login_password_edit_text);
		//editText_username.setText("partnerdemo@bookshare.org");
		//editText_password.setText("partner");
		
		// Listener for edit text box to handle the enter key
		editText_password.setOnKeyListener(new OnKeyListener() {
		    public boolean onKey(View v, int keyCode, KeyEvent event) {
		        // If the event is a key-down event on the "enter" button
		        if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
		            (keyCode == KeyEvent.KEYCODE_ENTER)) {
		          // Perform action on key press
		        	loginAction();
		          return true;
		        }
		        return false;
		    }
		});
		// Listener for login button
		btn_login.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				loginAction();
			}
		});
		
		// Listener for reset button
		btn_reset.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				editText_username.setText("");
				editText_password.setText("");
				editText_username.requestFocus();
			}
		});
		
		btn_continue_without_login.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				getFreeContent();
			}
		});
	}
	private void loginAction(){

		// Hide the virtual keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(editText_username.getWindowToken(), 0);
	        final Dialog finishedDialog = new Dialog(btn_login.getContext());

		username = editText_username.getText().toString().trim();
		password = editText_password.getText().toString().trim();
		
		// Test for conditions where the input might be blank
		if(username.equals("") && password.equals("")) {
        	    String message =  "Username/Password field cannot be blank!";
	            showAndCloseDialog(finishedDialog, message, 5000);
		}
		else if(username.equals("") && !password.equals("")) {
            	    String message =  "Username field cannot be blank!";
            	    showAndCloseDialog(finishedDialog, message, 5000);
		}
		else if(!username.equals("") && password.equals("")) {
            	    String message =  "Password field cannot be blank!";
            	    showAndCloseDialog(finishedDialog, message, 5000);
		}
		else{
			startProgressDialog();
			// Start a new AsyncTask for background processing
			new AuthenticationTask().execute();
		}
	}
	
    private void showAndCloseDialog(final Dialog finishedDialog, String message, int wait) {
        finishedDialog.setTitle(message);
        finishedDialog.show();

        // Close the dialog after a short wait
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
             public void run() {
                  finishedDialog.cancel();
             }
        }, wait);
     }
    
	private void getFreeContent(){
		isFree = true;
		isOM = false;
		username = null;
		password = null;
		
		if(isFree){
			pd_spinning = ProgressDialog.show(this, null, "Fetching free books data.", Boolean.TRUE);
		}
		else{
			pd_spinning = ProgressDialog.show(this, null, "Authenticating.", Boolean.TRUE);
		}

		// Start a new AsyncTask for background processing
		new AuthenticationTask().execute();
	}
	
	@Override
	/*
	 * @Non Javadoc
	 * Menu items that will shown when the Menu button on the phone 
	 * is clicked.
	 */
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE,1,Menu.NONE,"Forgot Password");
		menu.add(Menu.NONE,2,Menu.NONE,"Signup");
		return true;
	}
	
	@Override
	/*
	 * @Non Javadoc
	 * Callback method that will be called when a menu item is clicked.
	 */
	public boolean onOptionsItemSelected(MenuItem menuitem){
		
		if(menuitem.getTitle().equals("Forgot Password")){
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FORGOT_PW_URL));
			startActivity(myIntent);
		}
		else if(menuitem.getTitle().equals("Signup")){
			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(SIGNUP_URL));
			startActivity(myIntent);
		}
		return true;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}

	/*
	 * An AsyncTask class which carries out the authentication 
	 * in the background.
	 */
	private class AuthenticationTask extends AsyncTask<Void, Void, Void>{

		/*
		 * (non-Javadoc)
		 * This method is called in the UI thread just before the
		 * doInBackground is called. Disable the UI elements while
		 * the authentication is being done.
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute(){
			
			btn_login.setEnabled(false);
			btn_reset.setEnabled(false);
			
			if(isFree){
				editText_username.setText("");
				editText_password.setText("");
				text_username.setText("");
				text_password.setText("");
			}
			editText_username.setEnabled(false);
			editText_password.setEnabled(false);
		}


		/*
		 * (non-Javadoc)
		 * The entire body of this method is executed in a 
		 * newly spawned thread. Carry out the actual authentication task here
		 * @see android.os.AsyncTask#doInBackground(Params[])
		 */
		@Override
		protected Void doInBackground(Void... params) {

			String result_HTML = "";
			try{
				
				// Get a BookshareWebservice instance for accessing the utility methods

				final BookshareWebservice bws = new BookshareWebservice();
				if(isFree){
					BOOKSHARE_URL = BOOKSHARE_URL + "?api_key="+developerKey;
				}
				else{
					BOOKSHARE_URL = "https://api.bookshare.org/user/preferences/list/for/"+username+"/?api_key="+developerKey;
				}

				InputStream inputStream = bws.getResponseStream(password, BOOKSHARE_URL);
				result_HTML = bws.convertStreamToString(inputStream);

				// Cleanup the HTML formatted tags
				response = result_HTML.replace("&apos;", "'").replace("&quot;", "\"").replace("&amp;", "&").replace("&#xd;","").replace("&#x97;", "-");

			}
			catch(URISyntaxException use){
				status = NETWORK_ERROR;
			}
			catch(IOException ioe){
				status = NETWORK_ERROR;
			}
			finally {
				status = NETWORK_ERROR;

            }
			// Authentication failed
			if(result_HTML.contains("<status-code>401</status-code>") || result_HTML.contains("<status-code>500</status-code>")
					|| result_HTML.contains("<status-code>403</status-code>") || result_HTML.contains("<status-code>404</status-code>") 
					|| result_HTML.contains("<status-code>400</status-code>")) {

				status = LOGIN_FAILED;
			}
			else if (status != NETWORK_ERROR) {
				if(!isFree){
					Bookshare_UserType userTypeObj = new Bookshare_UserType();
					isOM = userTypeObj.isOM(response);
					if(isOM){
						String downloadPassword = userTypeObj.getDownloadPassword();
						if(downloadPassword == null){
							status = LOGIN_FAILED;
							return null;
						}
						
						SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
						SharedPreferences.Editor editor = login_preference.edit();
						editor.putString("downloadPassword", downloadPassword);
						editor.commit();
					}
				}
				status = LOGIN_SUCCESSFUL;
			}
			return null;
		}
		
        private void showAndCloseDialog(final Dialog finishedDialog, String message, int wait) {
            finishedDialog.setTitle(message);
            finishedDialog.show();

            // Close the dialog after a short wait
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                 public void run() {
                      finishedDialog.cancel();
                 }
            }, wait);
        }
        
		/*
		 * (non-Javadoc)
		 * Called in the UI thread immediately after the
		 * doInBackground ends. Re-enable the UI elements.
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		public void onPostExecute(Void result){
			super.onPostExecute(result);

            final Dialog finishedDialog = new Dialog(btn_login.getContext());
			btn_login.setEnabled(true);
			btn_reset.setEnabled(true);
			editText_username.setEnabled(true);
			editText_password.setEnabled(true);
			editText_username.requestFocus();
			if(pd_spinning != null)
				pd_spinning.cancel();

			switch(status){
			
			// Navigate to the next Activity
			case LOGIN_SUCCESSFUL:
				intent = new Intent(getApplicationContext(), Bookshare_Menu.class);
				
				if(!isFree){
					intent.putExtra("username", username);
					intent.putExtra("password", password);
				}
				
				// Obtain the application wide SharedPreferences object and store the login information
				SharedPreferences login_preference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				SharedPreferences.Editor editor = login_preference.edit();
				editor.putString("username", username);
				editor.putString("password", password);
				editor.putBoolean("isOM", isOM);
				editor.commit();
				startActivity(intent);
				finish();
				break;
				
			// Give the failure notification and show the login screen
			case LOGIN_FAILED:
		            	String message =  "Log in Failed!";
		            	showAndCloseDialog(finishedDialog, message, 5000);
				editText_username.setText("");
				editText_password.setText("");
				break;
			case NETWORK_ERROR:
	        	    	String nMessage =  "Network Error";
	            		showAndCloseDialog(finishedDialog, nMessage, 5000);
				editText_username.setText("");
				editText_password.setText("");
				break;
			default:
				break;
			}
		}
	}
	private void startProgressDialog(){
		if(isFree){
			pd_spinning = ProgressDialog.show(this, null, "Fetching free books data. Please wait.", Boolean.TRUE);
		}
		else{
			pd_spinning = ProgressDialog.show(this, null, "Authenticating. Please wait.", Boolean.TRUE);
		}
	}

}

