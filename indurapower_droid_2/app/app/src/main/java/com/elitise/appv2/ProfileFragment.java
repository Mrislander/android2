package com.elitise.appv2;


import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.LikeView;
import com.facebook.share.widget.ShareButton;
import com.facebook.share.widget.ShareDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters


    ShareDialog shareDialog;
    ShareButton shareButton;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    public ProfileFragment() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View  view = inflater.inflate(R.layout.profile, container, false);
        TextView emailAddress = (TextView) view.findViewById(R.id.emailAddress);
        if(user != null) {
            emailAddress.setText(user.getEmail().toString());
        }else {
            emailAddress.setText("Please Login First");
        }
        LikeView likeView = (LikeView) view.findViewById(R.id.like_view);

        likeView.setObjectIdAndType(
                "https://www.facebook.com/InduraPower-239075489865230/",
                LikeView.ObjectType.PAGE);
        shareButton = (ShareButton)view.findViewById(R.id.fb_share_button);
        shareDialog = new ShareDialog(this);

        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("InduraPower")
                .setContentDescription(
                        "Lithium ion Batteries #lithiumion #batteries #lithium #leadacid #agm @InduraPower")
                .setContentUrl(Uri.parse("http://www.indurapower.com/"))
                .build();
        shareButton.setShareContent(linkContent);

        return view;
    }

}
