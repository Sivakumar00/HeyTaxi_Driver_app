package com.siva;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class BusListActivity extends AppCompatActivity{

    DatabaseReference mDatabase;
    EditText edtBusNo;
    PlaceAutocompleteFragment from;
    PlaceAutocompleteFragment to;
    Button mSubmit;
    String busNo;
    String currentUser;
    Spinner mFromSpinner, mToSpinner;
    String fromPlaces,toPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_list);

        mDatabase= FirebaseDatabase.getInstance().getReference().child("TripDetails");
        mDatabase.keepSynced(true);
        currentUser=FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
        edtBusNo=(EditText)findViewById(R.id.bus_no);
        mSubmit=(Button)findViewById(R.id.submit_busList);
        final Spinner mToSpinner  = (Spinner) findViewById(R.id.spinner_to);
        final Spinner mFromSpinner = (Spinner) findViewById(R.id.spinner_from);

        List<String> list=new ArrayList<String>();
        list.add("Saranathan College of Engineering");
        list.add("Thillai nagar");
        list.add("Sri Rangam - Teppakulam - Renga Bhavan - Periyar Nagar");
        list.add("HAPP");
        list.add("RC School - Melapudhur - Palakarai");
        list.add("TVS Tolgate - American - Court - Puthur 4 road");
        list.add("E.Pudhur via Collector office - Puthur - Agraharam - Aruna theatre - Koratheru");
        list.add("M.K Kottai");
        list.add("C.S.I Hospital - Nachiyar kovil - Bazar - Rukmani");
        list.add("Ganesa - BHEL Training - BHEL EC - NIT");
        list.add("Srinivasa nagar -  Geetha Nagar - UT Malai - Rettai Vaikal - Somarasampettai");
        list.add("Devi theatre - Sri Rangam kovil");
        list.add("Kajamalai colony - Indian Bank colony - SundarNagar -  LIC Colony");
        list.add("Balaji nagar - Prakash nagar - Malaikovil - D.Nagar - Thiruverumbur");
        list.add("Oil mill - Kattur - ManjalThidal - Palam - Vignesh nagar - Kailash nagar");
        list.add("No.1 Tolgate");
        list.add("SIGC - Anna Statue");
        list.add("Senthanneerpuram - Palpannai - TV kovil");
        list.add("Mannarpuram - Kallukuzhi - Passport office - Ibrahim park");
        list.add("VVV Pon nagar - Karumandabam - RMS Colony - Dheeran Colony");
        list.add("Girls Hostel");
        list.add("KK Nagar Park - JK Nagar - RPF");
        list.add("SIT - Rail Nagar");
        list.add("NO.1 Tolgate");
        list.add("Bishop Heber - Kumaran Nagar");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter <String>(this, android.R.layout.simple_spinner_item, list);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mFromSpinner.setAdapter(dataAdapter);
        mToSpinner.setAdapter(dataAdapter);


        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    busNo=edtBusNo.getText().toString();
                    fromPlaces=mFromSpinner.getSelectedItem().toString();
                    toPlaces=mToSpinner.getSelectedItem().toString();
                if(!TextUtils.isEmpty(busNo)&&!TextUtils.isEmpty(fromPlaces)&&!TextUtils.isEmpty(toPlaces)&&!fromPlaces.equals(toPlaces)){

                    mDatabase.child(currentUser).child("from").setValue(fromPlaces);
                    mDatabase.child(currentUser).child("to").setValue(toPlaces);
                    mDatabase.child(currentUser).child("busNo").setValue(busNo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(BusListActivity.this, "Updated Successfully..!", Toast.LENGTH_LONG).show();
                            Intent in=new Intent(getApplicationContext(),Welcome.class);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(in);
                            finish();

                        }
                    });


                }
            }
        });


    }


}
