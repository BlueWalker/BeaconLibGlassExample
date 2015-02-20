package example.walker.blue.beacon.lib.glass;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import java.util.ArrayList;
import java.util.List;

import walker.blue.beacon.lib.beacon.Beacon;

/**
 * Adapter that handles the cards and beacons for the cardscrollview
 */
class BeaconCardScrollAdapter extends CardScrollAdapter {

    /**
     * Enums to specify the result of adding a beacon
     */
    public static enum BeaconAddResult { REPEAT_BEACON, NEW_BEACON }
    /**
     * Format used to create cards from the Beacon information
     */
    private static final String CARD_TEXT_FORMAT = "UUID: %s\nMajor: %d\nMinor: %d";
    /**
     * Context used throughout the class
     */
    private Context context;
    /**
     * List of all the Beacons
     */
    private List<Beacon> beacons;
    /**
     * List of the cards
     */
    private List<View> cards;

    /**
     * Constructor for the class. Sets the context field to the given context
     * and intializes the other fields
     *
     * @param context Context
     */
    public BeaconCardScrollAdapter(final Context context) {
        super();
        this.context = context;
        this.beacons = new ArrayList<>();
        this.cards = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return this.cards.size();
    }

    @Override
    public Object getItem(final int position) {
        return this.cards.get(position);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        return this.cards.get(position);
    }

    @Override
    public int getPosition(final Object obj) {
        if (obj instanceof Beacon) {
            return this.beacons.indexOf(obj);
        } else if (obj instanceof Card) {
            return this.cards.indexOf(obj);
        } else {
            return -1;
        }
    }

    /**
     * Handles adding a beacon to the adapter. If the beacon already exits, it adds the rssi value to the existing
     *
     * @param beacon Beacon
     * @return BeaconAddResult (REPEAT_BEACON if the beacon already existed,
     *          NEW_BEACON if it the added beacon was a new beacon)
     */
    public BeaconAddResult addBeacon(final Beacon beacon) {
        final BeaconAddResult result;
        int beaconPos;
        if ((beaconPos = this.getPosition(beacon)) != -1) {
            this.beacons.get(beaconPos).addMeasuredRSSI(beacon.getMeasuredRSSIValues().get(0));
            this.cards.set(beaconPos, beaconToCard(this.beacons.get(beaconPos)));
            result = BeaconAddResult.REPEAT_BEACON;
        } else {
            this.beacons.add(beacon);
            this.cards.add(beaconToCard(beacon));
            result = BeaconAddResult.NEW_BEACON;
        }
        notifyDataSetChanged();
        return result;
    }

    /**
     * Creates a Card using the information of the given Beacon
     *
     * @param beacon Beacon
     * @return View
     */
    private View beaconToCard(final Beacon beacon) {
        return new CardBuilder(this.context, CardBuilder.Layout.TEXT_FIXED)
                .setText(String.format(CARD_TEXT_FORMAT, beacon.getUUID(), beacon.getMajor(), beacon.getMinor()))
                .getView();
    }
}
