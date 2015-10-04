package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainScreenFragment;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilies;

public class WidgetRemoteViewService extends RemoteViewsService {
    public WidgetRemoteViewService() {
    }

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table._ID,
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.TIME_COL,
            DatabaseContract.scores_table.MATCH_ID,
            DatabaseContract.scores_table.LEAGUE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    // these indices must match the projection
    static final int INDEX_ID = 0;
    static final int INDEX_DATE_COL = 1;
    static final int INDEX_TIME_COL = 2;
    static final int INDEX_MATCH_ID = 3;
    static final int INDEX_LEAGUE_COL = 4;
    static final int INDEX_HOME_COL = 5;
    static final int INDEX_AWAY_COL = 6;
    static final int INDEX_HOME_GOALS_COL = 7;
    static final int INDEX_AWAY_GOALS_COL = 8;

    private static final String SCORES_FROM_DATE =
            DatabaseContract.scores_table.DATE_COL + " >= ?";



    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                String[] fromdateArray = new String[1];
                Date fromdate = new Date(System.currentTimeMillis()+((0-7)*86400000));
                SimpleDateFormat mformat = new SimpleDateFormat("yyyy-MM-dd");
                fromdateArray[0]=mformat.format(fromdate);

                Uri AllScoresUri = DatabaseContract.scores_table.buildScores();
                data = getContentResolver().query(AllScoresUri,
                        SCORE_COLUMNS,
                        SCORES_FROM_DATE,
                        fromdateArray,
                        DatabaseContract.scores_table.DATE_COL + " ASC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_list_item);

                String formattedScore =Utilies.getScores(data.getInt(INDEX_HOME_GOALS_COL),
                        data.getInt(INDEX_AWAY_GOALS_COL));
/*                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, description);
                }*/
                views.setTextViewText(R.id.widget_date, data.getString(INDEX_DATE_COL) );
                views.setTextViewText(R.id.widget_time, data.getString(INDEX_TIME_COL));
                views.setTextViewText(R.id.widget_league, Utilies.getLeague(data.getInt(INDEX_LEAGUE_COL)));
                views.setTextViewText(R.id.widget_home_team, data.getString(INDEX_HOME_COL));
                views.setTextViewText(R.id.widget_score, formattedScore);
                views.setTextViewText(R.id.widget_away_team, data.getString(INDEX_AWAY_COL));
//                views.setTextViewText(R.id.widget_description, description);
//                views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemperature);
//                views.setTextViewText(R.id.widget_low_temperature, formattedMinTemperature);

                final Intent fillInIntent = new Intent();
                Uri AllScoresUri1 = DatabaseContract.scores_table.buildScores();
                fillInIntent.setData(AllScoresUri1);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }


            @Override
            public long getItemId(int position) {

                return position;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_list_item);
            }

            @Override
            public boolean hasStableIds() {
                 return true;
             }




        };
    }
}

