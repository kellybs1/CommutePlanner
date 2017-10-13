package bit.kellybs1.commute;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * ResultsDialogue class
 * Author: Brendan Kelly
 * Date: 30 May 2017
 * Description: Dialog that confirms user wishes to start a new route
 */


public class YesNoNewDialogue extends DialogFragment
{
    private Resources resR;
    MapsActivity theMainActivity;

    public YesNoNewDialogue(){}

    //create the yes/no dialogue to confirm creating new route
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //get main activity
        theMainActivity = (MapsActivity) getActivity();
        //build dialogue
        resR = theMainActivity.getResources();
        AlertDialog.Builder newRouteDialog = new AlertDialog.Builder(theMainActivity);
        newRouteDialog.setTitle(resR.getString(R.string.confirm_new_route_title));
        newRouteDialog.setMessage(resR.getString(R.string.confirm_new_route));
        //yes button
        newRouteDialog.setPositiveButton("Yes", new dialogYesNoClickHandler());
        //no button
        newRouteDialog.setNegativeButton("No", new dialogYesNoClickHandler());
        //now give back built dialog
        return newRouteDialog.create();
    }

    public class dialogYesNoClickHandler implements Dialog.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            //send it back
            theMainActivity.DataFromNewConfirmDialog(which);
        }
    }

}
