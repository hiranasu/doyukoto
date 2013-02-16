package org.teamkaji.doyukoto;

import java.io.File;
import java.io.InputStream;
import java.util.Set;

import jp.co.olympus.meg40.BluetoothNotEnabledException;
import jp.co.olympus.meg40.BluetoothNotFoundException;
import jp.co.olympus.meg40.Meg;
import jp.co.olympus.meg40.MegGraphics;
import jp.co.olympus.meg40.MegListener;
import jp.co.olympus.meg40.MegStatus;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
		// 画像
		try
		{
			InputStream is = getResources().getAssets().open("bakusoku_title.png");
			Bitmap bm = BitmapFactory.decodeStream(is);
			
    		mMegGraphics.begin();
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
