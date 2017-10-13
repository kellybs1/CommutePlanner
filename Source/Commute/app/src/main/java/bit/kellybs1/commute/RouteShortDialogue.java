package bit.kellybs1.commute;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

/**
 * RouteShortDialogue class
 * Author: Brendan Kelly
 * Date: 30 May 2017
 * Description: Dialog that inform user their selected route is not long enough to check
 */

public class RouteShortDialogue extends DialogFragment
{
    private Resources resR;
    MapsActivity theMainActivity;

    public RouteShortDialogue(){}

    //create the yes/no dialogue to confirm creating new route
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        //get main activity
        theMainActivity = (MapsActivity) getActivity();
        //build dialogue
        resR = theMainActivity.getResources();
        AlertDialog.Builder routeShortDialog = new AlertDialog.Builder(theMainActivity);
        routeShortDialog.setTitle(resR.getString(R.string.title_route_too_short));
        routeShortDialog.setMessage(resR.getString(R.string.msg_route_too_short));
        //OK button
        routeShortDialog.setPositiveButton("OK", new dialogOKClickHandler());
        //now give back built dialog
        return routeShortDialog.create();
    }

    public class dialogOKClickHandler implements Dialog.OnClickListener
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dismiss();
        }
    }
}
