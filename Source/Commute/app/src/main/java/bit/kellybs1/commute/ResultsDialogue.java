package bit.kellybs1.commute;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * ResultsDialogue class
 * Author: Brendan Kelly
 * Date: 30 May 2017
 * Description: Dialog that lists results of traffic incident search
 */

public class ResultsDialogue extends DialogFragment
{
    private Resources resR;
    private MapsActivity theMainActivity;
    public View resultDiaglogue;


    public ResultsDialogue(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //get context
        theMainActivity = (MapsActivity) getActivity();
        resR = theMainActivity.getResources();

        //inflate the layout for this fragment
        resultDiaglogue = inflater.inflate(R.layout.fragment_result_dialogue, container, false);
        //set title
        getDialog().setTitle(resR.getString(R.string.title_results_dialogue));
        //get passed data
        Bundle incidentData = getArguments();
        ArrayList<String> incidents = incidentData.getStringArrayList(resR.getString(R.string.key_bundle_incidents));
        //put passed data in listview
        ListView lvResults = (ListView) resultDiaglogue.findViewById(R.id.listViewResults);
        ArrayAdapter<String> resultsAdapter = new ArrayAdapter(theMainActivity, R.layout.listview_results, incidents);
        lvResults.setAdapter(resultsAdapter);
        //make button work like a button
        Button buttonOK = (Button) resultDiaglogue.findViewById(R.id.buttonResultOK);
        buttonOK.setOnClickListener(new ButtonOKClickHandler());

        return resultDiaglogue;
    }

    //OK button handler
    public class ButtonOKClickHandler implements View.OnClickListener
    {

        @Override
        public void onClick(View v)
        {
            //dismiss dialogue
            dismiss();
        }
    }

}
