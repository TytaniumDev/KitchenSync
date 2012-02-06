package com.tyhollan.grocerylist.model;

import foo.joeledstrom.spreadsheets.SpreadsheetsService.TokenSupplier;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Application;
import android.os.Bundle;

public class AppNameApplication extends Application
{
   private Account mAccount;
   private TokenSupplier supplier;
   
   @Override
   public void onCreate()
   {
      super.onCreate();
      AccountManager.get(this).getAuthTokenByFeatures(
            "com.google", "wise", null, null, null, null,
            doneCallback, null);
      supplier = new TokenSupplier() {
         @Override
         public void invalidateToken(String token) {
             AccountManager.get(getApplicationContext()).invalidateAuthToken("com.google", token);
         }
         @Override
         public String getToken(String authTokenType) {
             try {
                 return AccountManager.get(getApplicationContext()).blockingGetAuthToken(mAccount, authTokenType, true);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
      };
   }
   
   private AccountManagerCallback<Bundle> doneCallback = new AccountManagerCallback<Bundle>() 
   {
      public void run(AccountManagerFuture<Bundle> arg0) 
      {
         Bundle b;
         try {
            b = arg0.getResult();

            String name = b.getString(AccountManager.KEY_ACCOUNT_NAME);
            String type = b.getString(AccountManager.KEY_ACCOUNT_TYPE);
            
            if(name != null && type != null)
            {
               mAccount = new Account(name, type);
            }
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }
   };
   
   public TokenSupplier getAuthTokenSupplier()
   {
      return supplier;
   }
}
