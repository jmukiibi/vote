package info.androidhive.party;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import info.android.fileupload.Config;
import info.android.pnu.R;
import info.androidhive.party.model.Validation;

@SuppressLint("NewApi")
public class Backup extends Activity implements View.OnClickListener {
	ProgressDialog prgDialog;
	String encodedString;

	String imgPath, fileName;
	Bitmap bitmap;
	private static int RESULT_LOAD_IMG = 1;
	EditText etSubCounty,etStationNumber,etPollStation,votes;
	Button submit, addImage;
	private String provider;
    private Uri fileUri;
    Uri selectedImage;
    Bitmap photo;
	Spinner spinnerCty;
	String subCounty,stationNumber,pollStation,vote,picturePath,ba1;
    // LogCat tag
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final int MEDIA_TYPE_IMAGE = 1;



    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.now);
		prgDialog = new ProgressDialog(this);
		// Set Cancelable as False
		prgDialog.setCancelable(false);
	//	ivImage = (ImageView)findViewById(R.id.ivImage);
		addImage=(Button) findViewById(R.id.buttonLoadPicture);
		submit = (Button) findViewById(R.id.btnPost);

		addImage.setOnClickListener(this);
		submit.setOnClickListener(this);
		formulate();
	}

	@Override
	public void onClick(View view) {

		switch(view.getId()){
			case R.id.btnPost:
				prgDialog.setMessage("Processing Upload");
				if(checkValidation()){
                    //    subCounty=etSubCounty.getText().toString();
                    stationNumber=etStationNumber.getText().toString();
                    pollStation=etPollStation.getText().toString();
                    vote=votes.getText().toString();
                    upload();
				}else{
					Toast.makeText(Backup.this, "Form contains error", Toast.LENGTH_LONG).show();
				}
				break;
			case R.id.buttonLoadPicture:

                    clickPicture();

				}
		}


    private void upload(){
    Log.e("Path", "----" + fileUri.getPath());
        Bitmap bm=BitmapFactory.decodeFile(fileUri.getPath());
        ByteArrayOutputStream bao=new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,90,bao);
        byte[] ba= bao.toByteArray();
        ba1 = Base64.encodeToString(ba, Base64.DEFAULT);
        Log.e("base64", "---" + ba1);
        new uploadToServer().execute();
    }

    private void clickPicture(){



        if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Intent intent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);
            startActivityForResult(intent,100);
        }   else{
            Toast.makeText(getApplicationContext(), "Camera not supported", Toast.LENGTH_LONG);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on screen orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }
    public class uploadToServer extends AsyncTask<Void, Void, String>{
        @Override
        protected void onPreExecute() {
        super.onPreExecute();
        prgDialog.setMessage("Processing Image UpLoad");
        prgDialog.show();
    }
     @Override
    protected String doInBackground(Void...params){

         ArrayList<NameValuePair> nameValuePairs= new ArrayList<NameValuePair>();
         nameValuePairs.add(new BasicNameValuePair("base64",ba1));
         nameValuePairs.add(new BasicNameValuePair("filename", getCurrentDateAndTime()));
         nameValuePairs.add(new BasicNameValuePair("subCounty", subCounty));
         nameValuePairs.add(new BasicNameValuePair("stationNumber", stationNumber));
         nameValuePairs.add(new BasicNameValuePair("pollStation", pollStation));
         nameValuePairs.add(new BasicNameValuePair("vote", vote));


         try {
             HttpClient httpClient = new DefaultHttpClient();
             HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);
             httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
             HttpResponse response = httpClient.execute(httppost);
             String st = EntityUtils.toString(response.getEntity());
             Log.v("log_tag", "In the try loop" + st);
             Log.v("List",response.toString());
             Log.v("trial",response.getEntity().toString());

         }catch(Exception e){
             Log.v("log_tag","Error in http connection"+e.getMessage());
         }
         return "success";
         }
    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
        Log.i("resp",result);
        prgDialog.hide();
        prgDialog.dismiss();
    }

     }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if (requestCode==100 && resultCode==RESULT_OK) {
            previewMedia(true);
        } else if (resultCode == RESULT_CANCELED) {

            // user cancelled Image capture
            Toast.makeText(getApplicationContext(),
                    "User cancelled image capture", Toast.LENGTH_SHORT)
                    .show();

        } else {
            // failed to capture image
            Toast.makeText(getApplicationContext(),
                    "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                    .show();
        }

    }


    private void previewMedia(boolean isImage) {
        ImageView imageView = (ImageView) findViewById(R.id.imgView);
        if (isImage) {
            imageView.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // down sizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(), options);

            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setVisibility(View.GONE);

        }
    }


	private void formulate() {

		spinnerCty = (Spinner) findViewById(R.id.spinnerCounty);
		// TextWatcher would let us check validation error on the fly
		spinnerCty.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {


			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
									   int arg2, long arg3) {
				arg0.getItemAtPosition(arg2);

				subCounty = arg0.getItemAtPosition(arg2).toString();

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub

			}
		});


		etStationNumber = (EditText) findViewById(R.id.etStationNumber);
		// TextWatcher would let us check validation error on the fly
		etStationNumber.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				Validation.hasText(etStationNumber);
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		etPollStation = (EditText) findViewById(R.id.etPoll);
		// TextWatcher would let us check validation error on the fly
		etPollStation.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				Validation.hasText(etPollStation);
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});

		votes = (EditText) findViewById(R.id.etvotesNumber);
		// TextWatcher would let us check validation error on the fly
		votes.addTextChangedListener(new TextWatcher() {
			public void afterTextChanged(Editable s) {
				Validation.hasText(votes);
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
		});



	}
	private boolean checkValidation() {
		boolean ret = true;

		if (!Validation.hasText(etStationNumber)) ret = false;

		if (!Validation.hasText(etPollStation)) ret = false;

		if (!Validation.hasText(votes)) ret = false;


		return ret;
	}
    private String getCurrentDateAndTime() {
        Calendar c=Calendar.getInstance();
        SimpleDateFormat df= new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String formattedDate=df.format(c.getTime());

        return formattedDate;
    }
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                Config.IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "Oops! Failed create "
                        + Config.IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }
}
