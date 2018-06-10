package com.siva.Service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.siva.Common.Common;
import com.siva.Model.Token;

/**
 * Created by MANIKANDAN on 24-12-2017.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken= FirebaseInstanceId.getInstance().getToken();
        updateTokenServer(refreshedToken);


    }

    private void updateTokenServer(String refreshedToken) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference().child(Common.token_tbl);

        Token token=new Token(refreshedToken);
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(token);

        }



    }

}
