package bit.kellybs1.commute;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * ResultsDialogue class
 * Author: Brendan Kelly
 * Date: 30 May 2017
 * Description: Dialog that displays "about" information
 */

public class AboutDialog extends DialogFragment
{
    public View aboutDialogue;
    private Button buttonOK;

    public AboutDialog()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //get context
        MapsActivity theMainActivity = (MapsActivity) getActivity();
        //inflate the layout for this fragment
        aboutDialogue = inflater.inflate(R.layout.fragment_about_dialogue, container, false);
        getDialog().setTitle(R.string.title_about_dialogue);
        buttonOK = (Button) aboutDialogue.findViewById(R.id.buttonAboutOK);
        buttonOK.setOnClickListener(new OnAboutOKButtonClickHandler());

        return aboutDialogue;
    }

    //handler for "OK" button in about dialogue
    public class OnAboutOKButtonClickHandler implements View.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            //get rid of dialogue
            dismiss();
        }
    }
}
