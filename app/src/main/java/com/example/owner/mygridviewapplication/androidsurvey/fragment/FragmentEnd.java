package com.example.owner.mygridviewapplication.androidsurvey.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.owner.mygridviewapplication.R;
import com.example.owner.mygridviewapplication.androidsurvey.Answers;
import com.example.owner.mygridviewapplication.androidsurvey.SurveyActivity;
import com.example.owner.mygridviewapplication.androidsurvey.models.SurveyProperties;

public class FragmentEnd extends Fragment {

    private FragmentActivity mContext;
    private TextView textView_end;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_end, container, false);


        Button button_finish = (Button) rootView.findViewById(R.id.button_finish);
        textView_end = (TextView) rootView.findViewById(R.id.textView_end);


        button_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((SurveyActivity) mContext).event_survey_completed(Answers.getInstance());

            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mContext = getActivity();
        SurveyProperties survey_properties = (SurveyProperties) getArguments().getSerializable("survey_properties");

        assert survey_properties != null;
        textView_end.setText(Html.fromHtml(survey_properties.getEndMessage()));

    }
}