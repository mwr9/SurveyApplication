package com.example.owner.mygridviewapplication;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.owner.mygridviewapplication.androidsurvey.SurveyActivity;
import com.opencsv.CSVWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int SURVEY_REQUEST = 1337;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }


    //items stored in ListView
    public class Item {
        Drawable ItemDrawable;
        String ItemString;

        Item(Drawable drawable, String t) {
            ItemDrawable = drawable;
            ItemString = t;
        }
    }

    //objects passed in Drag and Drop operation
    class PassObject {
        View view;
        Item item;
        List<Item> srcList;

        PassObject(View v, Item i, List<Item> s) {
            view = v;
            item = i;
            srcList = s;
        }
    }

    static class ViewHolder {
        ImageView icon;
        TextView text;
    }

    static class GridViewHolder {
        ImageView icon;
    }

    public class ItemBaseAdapter extends BaseAdapter {

        Context context;
        List<Item> list;

        ItemBaseAdapter(Context c, List<Item> l) {
            context = c;
            list = l;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public List<Item> getList() {
            return list;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            return null;
        }

    }


    public class ItemListAdapter extends ItemBaseAdapter {

        ItemListAdapter(Context c, List<Item> l) {
            super(c, l);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;

            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                rowView = inflater.inflate(R.layout.row, null);

                ViewHolder viewHolder = new ViewHolder();
                viewHolder.icon = (ImageView) rowView.findViewById(R.id.rowImageView);
                viewHolder.text = (TextView) rowView.findViewById(R.id.rowTextView);
                rowView.setTag(viewHolder);
            }

            ViewHolder holder = (ViewHolder) rowView.getTag();
            holder.icon.setImageDrawable(list.get(position).ItemDrawable);
            holder.text.setText(list.get(position).ItemString);

            rowView.setOnDragListener(new ItemOnDragListener(list.get(position)));

            return rowView;
        }

    }

    public class ItemGridAdapter extends ItemBaseAdapter {

        ItemGridAdapter(Context c, List<Item> l) {
            super(c, l);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View gridrowView = convertView;

            // reuse views
            if (gridrowView == null) {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                gridrowView = inflater.inflate(R.layout.grid_row, null);

                GridViewHolder gridviewHolder = new GridViewHolder();
                gridviewHolder.icon = (ImageView) gridrowView.findViewById(R.id.grid_row_ImageView);
                gridrowView.setTag(gridviewHolder);
            }

            GridViewHolder holder = (GridViewHolder) gridrowView.getTag();
            holder.icon.setImageDrawable(list.get(position).ItemDrawable);
            gridrowView.setOnDragListener(new ItemOnDragListener(list.get(position)));
            return gridrowView;
        }

    }

    List<Item> items1, items2, items3, items4, items5;
    ListView listView1;
    GridView gridView1, gridView2, gridView3, gridView4;
    ItemListAdapter myItemListAdapter1;
    ItemGridAdapter myItemGridAdapter1, myItemGridAdapter2, myItemGridAdapter3, myItemGridAdapter4;
    LinearLayoutAbsListView area1, area2, area3, area4, area5;
    TextView prompt;
    String fileName = "";
    //Used to resume original color in drop ended/exited
    int resumeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView1 = (ListView) findViewById(R.id.listview1);
        gridView1 = (GridView) findViewById(R.id.gridview1);
        gridView2 = (GridView) findViewById(R.id.gridview2);
        gridView3 = (GridView) findViewById(R.id.gridview3);
        gridView4 = (GridView) findViewById(R.id.gridview4);

        //Nothing fancy here. Plain old simple buttons....
        Button button_fname = (Button) findViewById(R.id.button_fname);
        Button button_survey = (Button) findViewById(R.id.button_survey);
        Button button_capture = (Button) findViewById(R.id.button_capture);

        button_fname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileName();
            }
        });

        button_survey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fileName.equals("")) {
                    Toast.makeText(MainActivity.this, "Please enter a file name", Toast.LENGTH_LONG).show();
                } else {
                    Intent i_survey = new Intent(MainActivity.this, SurveyActivity.class);
                    //you have to pass as an extra the json string.
                    i_survey.putExtra("json_survey", loadSurveyJson("survey_1.json"));
                    startActivityForResult(i_survey, SURVEY_REQUEST);
                }
            }
        });

        button_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeScreenshot();
            }
        });

        area1 = (LinearLayoutAbsListView) findViewById(R.id.pane1);
        area2 = (LinearLayoutAbsListView) findViewById(R.id.pane2);
        area3 = (LinearLayoutAbsListView) findViewById(R.id.pane3);
        area4 = (LinearLayoutAbsListView) findViewById(R.id.pane4);
        area5 = (LinearLayoutAbsListView) findViewById(R.id.pane5);

        area1.setOnDragListener(myOnDragListener);
        area2.setOnDragListener(myOnDragListener);
        area3.setOnDragListener(myOnDragListener);
        area4.setOnDragListener(myOnDragListener);
        area5.setOnDragListener(myOnDragListener);

        area1.setAbsListView(listView1);
        area2.setAbsListView(gridView1);
        area3.setAbsListView(gridView2);
        area4.setAbsListView(gridView3);
        area5.setAbsListView(gridView4);

        initItems();
        myItemListAdapter1 = new ItemListAdapter(this, items1);
        myItemGridAdapter1 = new ItemGridAdapter(this, items2);
        myItemGridAdapter2 = new ItemGridAdapter(this, items3);
        myItemGridAdapter3 = new ItemGridAdapter(this, items4);
        myItemGridAdapter4 = new ItemGridAdapter(this, items5);
        listView1.setAdapter(myItemListAdapter1);
        gridView1.setAdapter(myItemGridAdapter1);
        gridView2.setAdapter(myItemGridAdapter2);
        gridView3.setAdapter(myItemGridAdapter3);
        gridView4.setAdapter(myItemGridAdapter4);

        listView1.setOnItemClickListener(listOnItemClickListener);
        gridView1.setOnItemClickListener(listOnItemClickListener);
        gridView2.setOnItemClickListener(listOnItemClickListener);
        gridView3.setOnItemClickListener(listOnItemClickListener);
        gridView4.setOnItemClickListener(listOnItemClickListener);

        listView1.setOnItemLongClickListener(myOnItemLongClickListener);
        gridView1.setOnItemLongClickListener(myOnItemLongClickListener);
        gridView2.setOnItemLongClickListener(myOnItemLongClickListener);
        gridView3.setOnItemLongClickListener(myOnItemLongClickListener);
        gridView4.setOnItemLongClickListener(myOnItemLongClickListener);

        prompt = (TextView) findViewById(R.id.prompt);
        // make TextView scrollable
        prompt.setMovementMethod(new ScrollingMovementMethod());
        //clear prompt area if LongClick
        prompt.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                prompt.setText("");
                return true;
            }
        });

        resumeColor = ContextCompat.getColor(MainActivity.this, android.R.color.background_light);

    }

    AdapterView.OnItemLongClickListener myOnItemLongClickListener = new AdapterView.OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            Item selectedItem = (Item) (parent.getItemAtPosition(position));

            ItemBaseAdapter associatedAdapter = (ItemBaseAdapter) (parent.getAdapter());
            List<Item> associatedList = associatedAdapter.getList();

            PassObject passObj = new PassObject(view, selectedItem, associatedList);

            ClipData data = ClipData.newPlainText("", "");
            View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
            view.startDrag(data, shadowBuilder, passObj, 0);

            return true;
        }

    };

    View.OnDragListener myOnDragListener = new View.OnDragListener() {

        @Override
        public boolean onDrag(View v, DragEvent event) {
            String area;
            if (v == area1) {
                area = "area1";
            } else if (v == area2) {
                area = "area2";
            } else if (v == area3) {
                area = "area3";
            } else if (v == area4) {
                area = "area4";
            } else if (v == area5) {
                area = "area5";
            } else {
                area = "unknown";
            }

            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    prompt.append("ACTION_DRAG_STARTED: " + area + "\n");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    prompt.append("ACTION_DRAG_ENTERED: " + area + "\n");
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    prompt.append("ACTION_DRAG_EXITED: " + area + "\n");
                    break;
                case DragEvent.ACTION_DROP:
                    prompt.append("ACTION_DROP: " + area + "\n");

                    PassObject passObj = (PassObject) event.getLocalState();
                    View view = passObj.view;
                    Item passedItem = passObj.item;
                    List<Item> srcList = passObj.srcList;
                    AbsListView oldParent = (AbsListView) view.getParent();
                    ItemBaseAdapter srcAdapter = (ItemBaseAdapter) (oldParent.getAdapter());

                    LinearLayoutAbsListView newParent = (LinearLayoutAbsListView) v;
                    ItemBaseAdapter destAdapter = (ItemBaseAdapter) (newParent.absListView.getAdapter());
                    List<Item> destList = destAdapter.getList();

                    if (removeItemToList(srcList, passedItem)) {
                        addItemToList(destList, passedItem);
                    }

                    srcAdapter.notifyDataSetChanged();
                    destAdapter.notifyDataSetChanged();

                    //smooth scroll to bottom
                    newParent.absListView.smoothScrollToPosition(destAdapter.getCount() - 1);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    prompt.append("ACTION_DRAG_ENDED: " + area + "\n");
                default:
                    break;
            }

            return true;
        }

    };

    class ItemOnDragListener implements View.OnDragListener {

        Item me;

        ItemOnDragListener(Item i) {
            me = i;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    prompt.append("Item ACTION_DRAG_STARTED: " + "\n");
                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    prompt.append("Item ACTION_DRAG_ENTERED: " + "\n");
                    v.setBackgroundColor(0x30000000);
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    prompt.append("Item ACTION_DRAG_EXITED: " + "\n");
                    v.setBackgroundColor(resumeColor);
                    break;
                case DragEvent.ACTION_DROP:
                    prompt.append("Item ACTION_DROP: " + "\n");

                    PassObject passObj = (PassObject) event.getLocalState();
                    View view = passObj.view;
                    Item passedItem = passObj.item;
                    List<Item> srcList = passObj.srcList;
                    AbsListView oldParent = (AbsListView) view.getParent();
                    ItemBaseAdapter srcAdapter = (ItemBaseAdapter) (oldParent.getAdapter());

                    AbsListView newParent = (AbsListView) v.getParent();
                    ItemBaseAdapter destAdapter = (ItemBaseAdapter) (newParent.getAdapter());
                    List<Item> destList = destAdapter.getList();

                    int removeLocation = srcList.indexOf(passedItem);
                    int insertLocation = destList.indexOf(me);
    /*
     * If drag and drop on the same list, same position,
     * ignore
     */
                    if (srcList != destList || removeLocation != insertLocation) {
                        if (removeItemToList(srcList, passedItem)) {
                            destList.add(insertLocation, passedItem);
                        }

                        srcAdapter.notifyDataSetChanged();
                        destAdapter.notifyDataSetChanged();
                    }

                    v.setBackgroundColor(resumeColor);

                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    prompt.append("Item ACTION_DRAG_ENDED: " + "\n");
                    v.setBackgroundColor(resumeColor);
                default:
                    break;
            }

            return true;
        }

    }

    AdapterView.OnItemClickListener listOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Toast.makeText(MainActivity.this,
                    ((Item) (parent.getItemAtPosition(position))).ItemString,
                    Toast.LENGTH_SHORT).show();
        }

    };

    private void initItems() {
        items1 = new ArrayList<Item>();
        items2 = new ArrayList<Item>();
        items3 = new ArrayList<Item>();
        items4 = new ArrayList<Item>();
        items5 = new ArrayList<Item>();

        TypedArray arrayDrawable = getResources().obtainTypedArray(R.array.resicon);
        TypedArray arrayText = getResources().obtainTypedArray(R.array.restext);

        for (int i = 0; i < arrayDrawable.length(); i++) {
            Drawable d = arrayDrawable.getDrawable(i);
            String s = arrayText.getString(i);
            Item item = new Item(d, s);
            items1.add(item);
        }

        arrayDrawable.recycle();
        arrayText.recycle();
    }

    private boolean removeItemToList(List<Item> l, Item it) {
        boolean result = l.remove(it);
        return result;
    }

    private boolean addItemToList(List<Item> l, Item it) {
        boolean result = l.add(it);
        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SURVEY_REQUEST) {
            if (resultCode == RESULT_OK) {
                final String answers_json = data.getExtras().getString("answers");
                Log.i("JSON Answers", answers_json);
                if (fileName.equals("")) {
                    Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show();
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(answers_json);
                        saveCsv(jsonObject, fileName);
                        Toast.makeText(MainActivity.this, "Survey Answers Saved", Toast.LENGTH_SHORT).show();
                        fileName ="";
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    //json stored in the assets folder.
    private String loadSurveyJson(String filename) {
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void saveCsv(JSONObject jsonObject, String fileJson) throws IOException, JSONException {
        int noOfQuestions = 8;
        verifyStoragePermissions(this);
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        File myDir = new File(root + "/csv_responses");
        myDir.mkdirs();
        File csvFile = new File(myDir, fileJson+ ".csv");
        if (csvFile.exists())
            csvFile.delete();
        CSVWriter writer = new CSVWriter(new FileWriter(csvFile), ',');
        String[] csvArray = new String[noOfQuestions];
        csvArray[0] = (String) jsonObject.getString("What is your age group?");
        csvArray[1] = (String) jsonObject.getString("GENDER ?");
        csvArray[2] = (String) jsonObject.getString("What is your annual income?");
        csvArray[3] = (String) jsonObject.getString("What is your highest level of education?");
        csvArray[4] = (String) jsonObject.getString("Which beach/recreational area do you visit most often?");
        csvArray[5] = (String) jsonObject.getString("What is your relationship to this location?");
        csvArray[6] = (String) jsonObject.getString("How often do you visit this location?");
        csvArray[7] = (String) jsonObject.getString("I hereby give my consent for my responses to be used");
     //   for (int i = 0; i < csvArray.length; i++) {
            writer.writeNext(csvArray);
    //    }
        writer.close();
        // Tell the media scanner about the new file so that it is
        // immediately available to the user.
        MediaScannerConnection.scanFile(this, new String[]{csvFile.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Log.i("ExternalStorage", "Scanned " + path + ":");
                        Log.i("ExternalStorage", "-> uri=" + uri);
                    }
                });
    }

    private void getFileName() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        final EditText edittext = new EditText(this);
        alert.setMessage("File Name for this Survey");
        alert.setTitle("Enter Your File Name");
        alert.setView(edittext);
        alert.setPositiveButton("Save File Name", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                fileName = edittext.getText().toString();
                Toast.makeText(MainActivity.this, "File Name Saved", Toast.LENGTH_SHORT).show();
                 }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return;
            }
        });
        alert.show();

    }

    private void takeScreenshot() {
        if (fileName.equals("")) {
            Toast.makeText(this, "Please enter a file name", Toast.LENGTH_LONG).show();
        } else {
            saveImage(fileName);
            Toast.makeText(MainActivity.this, "Screen Image Saved", Toast.LENGTH_LONG).show();
        }
    }

    private void saveImage(String fileScreen) {
            verifyStoragePermissions(this);
            try     {
                String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                File myDir = new File(root + "/screen_shots");
                myDir.mkdirs();
                File imageFile = new File(myDir, fileScreen+ ".jpg");
                if (imageFile.exists())
                    imageFile.delete();
                // create bitmap screen capture
                View v1 = getWindow().getDecorView().getRootView();
                v1.setDrawingCacheEnabled(true);
                Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
                v1.setDrawingCacheEnabled(false);
                FileOutputStream outputStream = new FileOutputStream(imageFile);
                int quality = 100;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                outputStream.flush();
                outputStream.close();
                // Tell the media scanner about the new file so that it is
                // immediately available to the user.
                MediaScannerConnection.scanFile(this, new String[]{imageFile.toString()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Log.i("ExternalStorage", "Scanned " + path + ":");
                                Log.i("ExternalStorage", "-> uri=" + uri);
                            }
                        });
            } catch(Throwable e)  {
                // Several error may come out with file handling or OOM
                e.printStackTrace();
            }
    }
}
