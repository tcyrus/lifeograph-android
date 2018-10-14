/***********************************************************************************

    Copyright (C) 2012-2016 Ahmet Öztürk (aoz_2@yahoo.com)

    This file is part of Lifeograph.

    Lifeograph is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Lifeograph is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Lifeograph.  If not, see <http://www.gnu.org/licenses/>.

 ***********************************************************************************/

package net.sourceforge.lifeograph;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.support.v7.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.SwitchCompat;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

public class ActivityLogin extends AppCompatActivity
        implements DialogInquireText.InquireListener, DialogPassword.Listener
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: REMOVE
        Lifeograph.sContext = this;
        Lifeograph.updateScreenSizes();

        if (Diary.diary == null)
            Diary.diary = new Diary();

        // PREFERENCES
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.pref_general, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sExternalStorage = prefs.getString(
                Lifeograph.getStr(R.string.pref_DIARY_STORAGE_key), "N/A");
        sDiaryPath = prefs.getString(
                Lifeograph.getStr(R.string.pref_DIARY_PATH_key), "N/A");
        LDate.s_format_order = prefs.getString(
                Lifeograph.getStr(R.string.pref_DATE_FORMAT_ORDER_key), "N/A" );
        LDate.s_format_separator = prefs.getString(
                Lifeograph.getStr(R.string.pref_DATE_FORMAT_SEPARATOR_key), ".").charAt(0);

        setContentView(R.layout.login);

        // READ-ONLY SWITCH
        SwitchCompat switchReadOnly = findViewById(R.id.switch_read_only);
        switchReadOnly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSetPathType = Diary.SetPathType.READ_ONLY;
                } else {
                    mSetPathType = Diary.SetPathType.NORMAL;
                }
            }
        });

        // DIARY LIST
        mAdapterDiaries = new ArrayAdapter<>(this,
                                             R.layout.list_item_diary,
                                             R.id.title);

        ListView lv = findViewById(R.id.list_diaries);
        lv.setAdapter(mAdapterDiaries);
        lv.setOnItemClickListener(new ListView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                Log.d(Lifeograph.TAG, "on item selected");
                boolean flag_open_ready = false;

                Diary.diary.clear();

                switch (Diary.diary.set_path(mPaths.get(pos), mSetPathType)) {
                    case SUCCESS:
                        flag_open_ready = true;
                        break;
                    case FILE_NOT_FOUND:
                        Lifeograph.showToast("File is not found");
                        break;
                    case FILE_NOT_READABLE:
                        Lifeograph.showToast("File is not readable");
                        break;
                    case FILE_LOCKED:
                        Lifeograph.showToast("File is locked");
                        break;
                    default:
                        Lifeograph.showToast("Failed to open the diary");
                        break;
                }

                if (flag_open_ready) {
                    flag_open_ready = false;
                    switch (Diary.diary.read_header(getAssets())) {
                        case SUCCESS:
                            flag_open_ready = true;
                            break;
                        case INCOMPATIBLE_FILE:
                            Lifeograph.showToast("Incompatible diary version");
                            break;
                        case CORRUPT_FILE:
                            Lifeograph.showToast("Corrupt file");
                            break;
                        default:
                            Log.e(Lifeograph.TAG, "Unprocessed return value from read_header");
                            break;
                    }
                }

        /*
         * TODO: if (flag_open_ready && (!flag_encrypted) && m_flag_open_directly) {
         * handle_button_opendb_clicked(); return; }
         */

                if (flag_open_ready) {
                    if (Diary.diary.is_encrypted())
                        askPassword();
                    else
                        readBody();
                }
            }

        });

        registerForContextMenu(lv); // ???? What does this do?

        populate_diaries();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // TODO: REMOVE
        Lifeograph.sContext = this;

        if (Lifeograph.sLoginStatus == Lifeograph.LoginStatus.LOGGED_IN) {
            Lifeograph.prepareForLogout();

            Lifeograph.sLoginStatus = Lifeograph.LoginStatus.LOGGED_OUT;
        }

        populate_diaries(); // this also helps with the changes in the diary path
        Log.d(Lifeograph.TAG, "ActivityLogin.onResume()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(Lifeograph.TAG, "ActivityLogin.onDestroy()");
    }

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data ) {
        super.onActivityResult( requestCode, resultCode, data );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_login, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu( Menu menu ) {
        super.onPrepareOptionsMenu( menu );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        switch (item.getItemId()) {
            case R.id.new_diary:
                createNewDiary();
                return true;
            case R.id.settings:
                launchSettings();
                return true;
            case R.id.about:
                DialogAbout dialog = new DialogAbout( this );
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void askPassword() {
        DialogPassword dlg = new DialogPassword(this,
                                                 Diary.diary,
                                                 DialogPassword.DPAction.DPA_LOGIN,
                                                 this);
        dlg.show();
    }

    private void readBody() {
        switch (Diary.diary.read_body()) {
            case SUCCESS:
                Intent i = new Intent(this, ActivityDiary.class);
                Lifeograph.sFlagStartingDiaryEditingActivity = true;
                startActivity(i);
                break;
            case WRONG_PASSWORD:
                Lifeograph.showToast(R.string.wrong_password);
                break;
            case CORRUPT_FILE:
                Lifeograph.showToast("Corrupt file");
                break;
            default:
                break;
        }
    }

    File getDiariesDir() {
        if (sExternalStorage.equals("C")) {
            return new File(sDiaryPath);
        } else if (sExternalStorage.equals("E") ) {
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // We can read and write the media
                return new File(Environment.getExternalStorageDirectory(), sDiaryPath);
            } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
                // We can only read the media (we may do something else here)
                Lifeograph.showToast(R.string.storage_not_available);
                Log.d(Lifeograph.TAG, "Storage is read-only");
            } else {
                // Something else is wrong. It may be one of many other states, but
                // all we need to know is we can neither read nor write
                Lifeograph.showToast(R.string.storage_not_available);
            }
        }

        return new File(getFilesDir(), sDiaryPath);
    }

    // InquireListener INTERFACE METHODS
    public void onInquireAction(int id, String text) {
        switch (id) {
            case R.string.create_diary:
                if (Diary.diary.init_new(Lifeograph.joinPath(getDiariesDir().getPath(), text))
                    == Result.SUCCESS) {
                    Intent i = new Intent(ActivityLogin.this, ActivityDiary.class);
                    Lifeograph.sFlagStartingDiaryEditingActivity = true;
                    startActivity(i);
                }
                // TODO else inform the user about the problem
                break;
        }
    }

    public boolean onInquireTextChanged(int id, String s) {
        switch (id) {
            case R.string.create_diary:
                File fp = new File(getDiariesDir().getPath(), s);
                return !fp.exists();
        }
        return true;
    }

    // DialogPassword INTERFACE METHODS
    public void onDPAction(DialogPassword.DPAction action) {
        if (action == DialogPassword.DPAction.DPA_LOGIN)
            readBody();
    }

    void createNewDiary() {
        // ask for name
        DialogInquireText dlg = new DialogInquireText(this, R.string.create_diary,
                                                      Lifeograph.getStr(R.string.new_diary),
                                                      R.string.create, this);
        dlg.show();
    }

    void populate_diaries() {
        mAdapterDiaries.clear();
        mPaths.clear();

        File dir = getDiariesDir();
        Log.d(Lifeograph.TAG, dir.getPath());
        if (!dir.exists()) {
            if (!dir.mkdirs())
                Lifeograph.showToast("Failed to create the diary folder");
        } else {
            File[] dirs = dir.listFiles();
            if (dirs != null) {
                for (File ff : dirs) {
                    if (!ff.isDirectory()) {
                        mAdapterDiaries.add(ff.getName());
                        mPaths.add(ff.getPath());
                    }
                }
            }
        }

        mPaths.add( Diary.sExampleDiaryPath );
        mAdapterDiaries.add( Diary.sExampleDiaryName );
    }

    void launchSettings() {
        Intent i = new Intent(this, ActivitySettings.class);
        startActivity(i);
    }

    // VARIABLES
    public static String sExternalStorage = "";
    public static String sDiaryPath;
    private List<String> mPaths = new ArrayList<>();
    private ArrayAdapter<String> mAdapterDiaries;
    private Diary.SetPathType mSetPathType = Diary.SetPathType.NORMAL;
    //private DiaryAdapter mAdapterDiaries; MAYBE LATER

    // DIARY ELEMENT ADAPTER CLASS =================================================================
//    class DiaryAdapter extends ArrayAdapter< Diary >
//    {
//        public DiaryAdapter( Context context,
//                             int resource,
//                             int textViewResourceId,
//                             java.util.List< Diary > objects,
//                             LayoutInflater inflater ) {
//            super( context, resource, textViewResourceId, objects );
//            mInflater = inflater;
//        }
//
//        @Override
//        public View getView( int position, View convertView, ViewGroup parent ) {
//            ViewHolder holder;
//            final Diary diary = getItem( position );
//
//            if( convertView == null ) {
//                View view = mInflater.inflate( R.layout.list_item_diary, parent, false );
//                holder = new ViewHolder( view );
//
//                convertView = holder.getView();
//
//                view.setTag( holder );
//            }
//            else {
//                holder = ( ViewHolder ) convertView.getTag();
//            }
//
//            TextView title = holder.getName();
//            title.setText( diary.get_list_str() );
//
//            TextView detail = holder.getDetail();
//            detail.setText( diary.getListStrSecondary() );
//
//            return convertView;
//        }
//
//        private LayoutInflater mInflater;
//
//        // VIEW HOLDER
// =============================================================================
//        private class ViewHolder
//        {
//            private View mRow;
//            private TextView mTitle = null;
//            private TextView mDetail = null;
//            private ImageView mIcon = null;
//
//            public ViewHolder( View row ) {
//                mRow = row;
//            }
//
//            public View getView() {
//                return mRow;
//            }
//
//            public TextView getName() {
//                if( null == mTitle ) {
//                    mTitle = ( TextView ) mRow.findViewById( R.id.title );
//                }
//                return mTitle;
//            }
//
//            public TextView getDetail() {
//                if( null == mDetail ) {
//                    mDetail = ( TextView ) mRow.findViewById( R.id.detail );
//                }
//                return mDetail;
//            }
//
//            public ImageView getIcon() {
//                if( null == mIcon ) {
//                    mIcon = ( ImageView ) mRow.findViewById( R.id.icon );
//                }
//                return mIcon;
//            }
//        }
//    }


    // ABOUT DIALOG ================================================================================
    public class DialogAbout extends Dialog
    {
        DialogAbout(Context context) {
            super(context);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setContentView(R.layout.dialog_about);
            setTitle(R.string.program_name);
            setCancelable(true);

            TextView tv = findViewById(R.id.textViewWebsite);
            tv.setMovementMethod(LinkMovementMethod.getInstance());

            tv = findViewById(R.id.textViewVersion);
            tv.setText(BuildConfig.VERSION_NAME);
        }
    }
}
