package com.tyhollan.grocerylist.model;

import java.net.URL;
import java.util.HashMap;

import android.content.Context;
import android.util.Log;

import com.tyhollan.grocerylist.R;

import foo.joeledstrom.spreadsheets.Spreadsheet;
import foo.joeledstrom.spreadsheets.SpreadsheetsService;
import foo.joeledstrom.spreadsheets.SpreadsheetsService.FeedIterator;
import foo.joeledstrom.spreadsheets.Worksheet;
import foo.joeledstrom.spreadsheets.Worksheet.ListEntry;
import foo.joeledstrom.spreadsheets.Worksheet.ListFeed;
import foo.joeledstrom.spreadsheets.WorksheetRow;

public class GoogleDocsAdapter
{
   private static final String GROCERY_LIST_DOC_NAME = "GroceryListAppData";
   private static final String tag = "SpreadsheetTestActivity";
   
   private Worksheet mWorksheet;
   private Context mCtx;

   public GoogleDocsAdapter(Context context)
   {
      mCtx = context;
      SpreadsheetsService service = new SpreadsheetsService(mCtx.getString(R.string.app_name),
            ((AppNameApplication) mCtx.getApplicationContext()).getAuthTokenSupplier());
      try
      {
         FeedIterator<Spreadsheet> spreadsheetFeed = service.getSpreadsheets(GROCERY_LIST_DOC_NAME, true);
         Spreadsheet spreadsheet = spreadsheetFeed.getNextEntry();
         FeedIterator<Worksheet> worksheetFeed = spreadsheet.getWorksheets();
         mWorksheet = worksheetFeed.getNextEntry();
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public GroceryList getGroceryList()
   {
      GroceryList list = new GroceryList();
      try
      {
         FeedIterator<WorksheetRow> rows = mWorksheet.getRows();
         for (WorksheetRow row : rows.getEntries())
         {
            Log.i(tag, "Column names: " + row.getColumnNames().toString());
            Log.i(tag, "Item name: " + row.getValue(DBAdapter.KEY_ITEMNAME));
            // TODO: Change to String constants
            GroceryItem item = new GroceryItem(row.getValue(DBAdapter.KEY_ITEMNAME), row.getValue(DBAdapter.KEY_AMOUNT), row.getValue(DBAdapter.KEY_STORE),
                  row.getValue(DBAdapter.KEY_CATEGORY));
            list.addGroceryItem(item);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return list;
   }

   public void saveGroceryItem(final GroceryItem item)
   {
      try
      {
         mWorksheet.addRow(new HashMap<String, String>() {{
            put(DBAdapter.KEY_ITEMNAME, item.getItemName());
            put(DBAdapter.KEY_AMOUNT, item.getAmount());
            put(DBAdapter.KEY_STORE, item.getStore());
            put(DBAdapter.KEY_CATEGORY, item.getCategory());
         }});
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   public void deleteGroceryItem(GroceryItem item)
   {
      
   }
   // /** Called when the activity is first created. */
   // @Override
   // public void onCreate(Bundle savedInstanceState) {
   // super.onCreate(savedInstanceState);
   // setContentView(R.layout.main);
   //
   // button1 = (Button)findViewById(R.id.button1);
   // mTextView = (TextView) findViewById(R.id.textview);
   //
   // button1.setOnClickListener(new OnClickListener() {
   //
   // @Override
   // public void onClick(View v) {
   // AccountManager.get(GoogleDocsAdapter.this)
   // .getAuthTokenByFeatures("com.google","wise", null, GoogleDocsAdapter.this,
   // null, null, doneCallback, null);
   //
   // }
   // });
   //
   // }

   // private AccountManagerCallback<Bundle> doneCallback = new
   // AccountManagerCallback<Bundle>() {
   // public void run(AccountManagerFuture<Bundle> arg0) {
   //
   // Bundle b;
   // try {
   // b = arg0.getResult();
   //
   // String name = b.getString(AccountManager.KEY_ACCOUNT_NAME);
   // String type = b.getString(AccountManager.KEY_ACCOUNT_TYPE);
   //
   // account = new Account(name, type);
   //
   // new Task().execute();
   //
   //
   // } catch (Exception e) {
   // throw new RuntimeException(e);
   // }
   //
   // }
   // };

   // class Task extends AsyncTask<Void, Void, Void> {
   // public Void doInBackground(Void... params) {
   // TokenSupplier supplier = new TokenSupplier() {
   // @Override
   // public void invalidateToken(String token) {
   // AccountManager.get(GoogleDocsAdapter.this).invalidateAuthToken("com.google",
   // token);
   // }
   // @Override
   // public String getToken(String authTokenType) {
   // try {
   // return
   // AccountManager.get(GoogleDocsAdapter.this).blockingGetAuthToken(account,
   // authTokenType, true);
   // } catch (Exception e) {
   // throw new RuntimeException(e);
   // }
   // }
   // };
   //
   //
   // // SpreadsheetsService service = new SpreadsheetsService("company-app-v2",
   // supplier);
   //
   //
   //
   //
   // try {
   // // service.createSpreadsheet("new spreadsheet 1", true);
   // // FeedIterator<Spreadsheet> spreadsheetFeed = service.getSpreadsheets();
   // // // get all spreadsheets
   // //
   // // List<Spreadsheet> spreadsheets = spreadsheetFeed.getEntries(); // reads
   // and parses the whole stream
   // // for(int i = 0; i < spreadsheets.size(); i++)
   // // {
   // // Log.i(tag, ""+ spreadsheets.get(i).getTitle());
   // // }
   // // Spreadsheet firstSpreadsheet = spreadsheets.get(0);
   // //
   // //
   // // Worksheet sheet = firstSpreadsheet.addWorksheet("Test sheet 1",
   // Arrays.asList(new String[] {"col", "date", "content", "whatever"}));
   //
   //
   // // sheet.addRow(new HashMap<String, String>() {{
   // // put("col", "A VALUE");
   // // put("content", "testing !");
   // // }});
   // //
   // // WorksheetRow row = sheet.addRow(new HashMap<String, String>() {{
   // // put("col", "another value");
   // // put("date", "43636544");
   // // }});
   // //
   // // row.setValue("content", "changed this row!");
   // //
   // // commitChanges() returns false, if it couldnt safely change the row
   // // (someone else changed the row before we commited)
   // // row.commitChanges();
   //
   //
   //
   //
   // getGroceryList();
   // Log.e(tag, "DONE");
   // } catch (Exception e) {
   // throw new RuntimeException(e);
   // }
   //
   // return null;
   // }
   // }

}
