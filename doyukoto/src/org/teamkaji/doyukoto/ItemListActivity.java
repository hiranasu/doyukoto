package org.teamkaji.doyukoto;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import jp.co.olympus.meg40.BluetoothNotEnabledException;
import jp.co.olympus.meg40.BluetoothNotFoundException;
import jp.co.olympus.meg40.Meg;
import jp.co.olympus.meg40.MegGraphics;
import jp.co.olympus.meg40.MegListener;
import jp.co.olympus.meg40.MegStatus;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends FragmentActivity implements
		ItemListFragment.Callbacks, MegListener {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	private Meg mMeg; //MEGへのコマンド送信を行うインスタンス
	private MegGraphics mMegGraphics; // グラフィック描画用
	
	private static final int VOICE_REQUEST_CODE = 123;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getSupportFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}
		
        //Bluetooth接続できるかどうかチェックする
    	//接続できなければ、アプリを終了
		if (mMeg == null)
		{
        	try {
        		// MEGはシングルトンパターン
        		// 最初のgetInstance呼び出しではインスタンス生成時に例外が投げられることがある
    			mMeg = Meg.getInstance();
        		// MEGのイベント監視のハンドラを登録
            	mMeg.registerMegListener(this);

                // MEGのグラフィックス機能を使うクラスの生成
                mMegGraphics = new MegGraphics(mMeg);
    		} catch (BluetoothNotFoundException e) {
    			Toast.makeText(this, "Bluetoothアダプターが見つかりません", Toast.LENGTH_LONG).show();
    			finish();
    		} catch (BluetoothNotEnabledException e) {
    			Toast.makeText(this, "Bluetoothが無効になっています\n有効にしてください", Toast.LENGTH_LONG).show();
    			finish();
    		}
		}
		// mMegは非null
		if (mMeg.isConnected())
		{
			// 接続済み
			Toast.makeText(this, "CONNECTED!", Toast.LENGTH_LONG).show();
		}
		else // 未接続
		{
            //Bluetooth接続できるペアリング済みデバイスのリストを表示するアクティビティ（ダイアログ）を開始する。
        	//アクティビティが終了したら、onActivityResult() に終了コードとして、REQUEST_CONNECT_DEVICEを返す。
            //MEGへの接続はonActivityResult()で実行される。
			// addressは"XX:XX:XX:XX:XX:XX"の形式
			BluetoothAdapter mBtAdapter = BluetoothAdapter.getDefaultAdapter();

	        // Get a set of currently paired devices
	        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
	        for (BluetoothDevice btd : pairedDevices) {
	        	if (btd.getName().equals("MEG4")) {
	    			Toast.makeText(this, "connect to " + btd.getAddress(), Toast.LENGTH_LONG).show();
	    			
	    			mMeg.connect(btd.getAddress());
	    			break;
	        	}
	        }
		}
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		if (mTwoPane) {
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID, id);
			ItemDetailFragment fragment = new ItemDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.item_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this, ItemDetailActivity.class);
			detailIntent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}

	@Override
	public void onMegAccelChanged(int arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegConnected() {
		Toast.makeText(this, "onMegConnected", Toast.LENGTH_SHORT).show();
		mMegGraphics.begin();
		mMegGraphics.drawString(100, 50, new String(" ")); // (100, 50)の位置に描画
		mMegGraphics.end();
		
		viewEndless();
//		voiceSearch();
	}
	
	private long lastId = 0;

	private void viewEndless() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        TalkSearcher ts = new TalkSearcher();
		for (int i = 0; i < 100; i++) {  // 暫定的に回数指定
			List<Talk> talks = ts.getTalks(lastId);
			Log.v("", "getTalks!");
			int processedTalkNum = 0;
			List<String> urls = new ArrayList<String>();
			if (talks != null ) {
				for (Talk t : talks) {
					
					// 文字を画面に流す
					Log.v("", t.getText());
//					textToMeg(t.getAccount(), t.getText(), processedTalkNum);
					textToMegWithIcon(t.getAccount(), t.getText(), processedTalkNum);
					
					if (t.getUrl() != null) {
						urls.add(t.getUrl());
					}

					// MAXのIDを保存
					if (t.getId() > lastId) {
						lastId = t.getId();
					}
					
					processedTalkNum++;
					if(processedTalkNum == talks.size()) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						// どれか画像を表示
						if (urls.size() > 0) {
							showImage(urls.get(r.nextInt(urls.size())));
						}
					}
				}
			}

			try {
				// 一定間隔ごとに実行
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
				break;
			}
		}
	}

	private void textToMegWithIcon(String account, String text, int scrollIndex) {
		try
		{
    		InputStream is = getResources().getAssets().open(account + ".png");
			Bitmap bm = BitmapFactory.decodeStream(is);
		
    		mMegGraphics.begin();
    		mMegGraphics.clearScreen();
    		mMegGraphics.registerImage(1003, resize(bm, 48, 48));
    		mMegGraphics.drawImage(1003, 20, 20, new Rect(0, 0, 48, 48));
    		mMegGraphics.setFontColor(0xffffffff);
    		mMegGraphics.drawString(20, 80, new String(text));
    		mMegGraphics.end();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Toast.makeText(this, "open asset failed", Toast.LENGTH_SHORT).show();
		}
	}
	
	private Random r = new Random(0);
//	
//	private void textToMeg(String account, String str, int scrollIndex) {
//		//色定義
//		final int RED 		= 0xffff0000;
//		final int GREEN 	= 0xff00ff00;
//		final int BLUE 		= 0xff0000ff;
//		final int YELLOW 	= 0xffffff00;
//		final int MAGENTA 	= 0xffff00ff;
//		final int CYAN 		= 0xff00ffff;
//		final int GRAY      = 0xffcccccc;
//
//		//移動量（ピクセル）
//		final int SPEED_FAST = 10;
//
//		mMegGraphics.begin();
//		
//		int[] size 	= { 30 + r.nextInt(30) };
//		int[] colors0 	= { RED, GREEN, BLUE, YELLOW, MAGENTA, CYAN, GRAY};
//		int[] color = {colors0[Math.abs(account.hashCode()) % colors0.length]};
//		String[] texts0 = {str};
//		final int startX0 = 320;
//		final int startY0 = r.nextInt(4) * 40;
//		
//		//文字列登録
//		mMegGraphics.registerText(scrollIndex, true, size, color, texts0);
//		//スクロール設定、スクロール開始
//		mMegGraphics.registerScroll(scrollIndex, startX0, startY0, SPEED_FAST, 1500, 0, 5);
//		mMegGraphics.end();
//	}
//	
//	private void stopTexts() {
//		int[] textIDs = { 0xffff, }; // 0xffffで全削除
//		
//		mMegGraphics.begin();
//		mMegGraphics.scrollStartStop(1);
//		mMegGraphics.removeText(textIDs); // 登録したテキストを全削除、registerScrollで登録したものも削除される
////		mMegGraphics.end();
//	}
	
//	private void voiceSearch() {
//        try {
//            // インテント作成
//            Intent intent = new Intent(
//                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH); // ACTION_WEB_SEARCH
//            intent.putExtra(
//                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//            intent.putExtra(
//                    RecognizerIntent.EXTRA_PROMPT,
//                    "VoiceRecognitionTest"); // お好きな文字に変更できます
//            
//            // インテント発行
//            startActivityForResult(intent, VOICE_REQUEST_CODE);
//        } catch (ActivityNotFoundException e) {
//            // このインテントに応答できるアクティビティがインストールされていない場合
//            Toast.makeText(this,
//                "ActivityNotFoundException", Toast.LENGTH_LONG).show();
//        }
//	}
//	
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // 自分が投げたインテントであれば応答する
//        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
//            
//            // 結果文字列リスト
//            ArrayList<String> results = data.getStringArrayListExtra(
//                    RecognizerIntent.EXTRA_RESULTS);
//            
//            // トーストを使って結果を表示
//            showImage(results.get(0));
//            Toast.makeText(this, results.get(0), Toast.LENGTH_LONG).show();
//        }
//        
//        super.onActivityResult(requestCode, resultCode, data);
//    }
	
	private void showImage(String query) {
		// 流れている文字を停止
//		stopTexts();
		mMegGraphics.begin();
		try
		{
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
//            ImageSearcher imageSercher = new ImageSearcher();
//            URL url = new URL(imageSercher.getImageUrl(query));
            URL url = new URL(query);
            InputStream is = url.openStream();
//			InputStream is = getResources().getAssets().open("bakusoku_title.png");
			Bitmap bm = BitmapFactory.decodeStream(is);

//    		mMegGraphics.begin();
    		mMegGraphics.registerImage(1000, resize(bm, 320, 240)); // ID=1000に登録
    		mMegGraphics.drawImage(1000, 0, 0, new Rect(0, 0, 320, 240)); // 画像の(10, 30)-(330, 270)のQVGAサイズを切り出して描画
    		mMegGraphics.end();
		}
		catch (Exception e)
		{
			Toast.makeText(this, "open asset failed", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * サイズ変換
	 * @param resizeWidth リサイズ後の幅
	 * @param resizeHeight リサイズ後の高さ
	 */
	public Bitmap resize(Bitmap bitmap, float resizeWidth, float resizeHeight){
		float resizeScaleWidth;
		float resizeScaleHeight;

		Matrix matrix = new Matrix();
		resizeScaleWidth = resizeWidth / bitmap.getWidth();
		resizeScaleHeight = resizeHeight / bitmap.getHeight();
		matrix.postScale(resizeScaleWidth, resizeScaleHeight);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	
	@Override
	public void onMegConnectionFailed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegDeleteImage(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegDirectionChanged(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegDisconnected() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegGraphicsCommandEnd(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegGraphicsCommandStart(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegKeyPush(int arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegSetContext(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegSleep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegStatusChanged(MegStatus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMegVoltageLow() {
		// TODO Auto-generated method stub
		
	}
}
