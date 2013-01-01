package com.kyokomi.scrollquest;

import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;

import com.kyokomi.scrollquest.utils.ResourceUtil;
import com.kyokomi.scrollquest.R;

import android.view.KeyEvent;


/**
 * {@link BaseGameActivity}のサブクラスであり、XMLLayoutを利用してActivityを生成するクラス.
 * 逆にXMLLayoutを使わずにゲームを描画する際は、{@link SimpleBaseGameActivity}を利用する。
 * Layoutにしておくと後から広告を入れたり一部をWebViewにしたり簡単にできる。
 * 
 * @author kyokomi
 *
 */
public class MainActivity extends MultiSceneActivity {

	// 画面サイズ
	private int CAMERA_WIDTH = 800;
	private int CAMERA_HEIGHT = 480;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		// サイズを指定し描画範囲をインスタンス化
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		// ゲームのエンジンを初期化
		EngineOptions eo = new EngineOptions(
				// タイトルバー非表示モード
				true, 
				// 画面横向き
				ScreenOrientation.LANDSCAPE_FIXED,  
				// 画面（800 x 480）解像度の縦横比を保ったまま最大まで拡大
				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT),
				 // 描画範囲
				camera);
		
		return eo;
	}

	/**
	 * Sceneサブクラスを返す.
	 */
	@Override
	protected Scene onCreateScene() {
		// サウンドファイルの格納場所を指定
		SoundFactory.setAssetBasePath("mfx/");
		
		InitialScene initialScene = new InitialScene(this);
		// 遷移管理用配列に追加
		getSceneArray().add(initialScene);
		return initialScene;
	}

	/**
	 * ActivityのレイアウトのIDを返す.
	 */
	@Override
	protected int getLayoutID() {
		return R.layout.activity_main;
	}

	/**
	 * SceneがセットされるViewのIDを返す.
	 */
	@Override
	protected int getRenderSurfaceViewID() {
		return R.id.renderview;
	}

	@Override
	public void appendScene(KeyListenScene scene) {
		
	}

	@Override
	public void backToInitial() {
		
	}

	@Override
	public void refreshRunningScene(KeyListenScene scene) {
		
	}
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		// バックボタンが押された時
		if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_BACK) {

			// 起動中のSceneのdispatchEvent関数を呼び出す
			// 追加の処理が必要なときは、falseがかえってくるため処理する
			if (!getSceneArray().get(getSceneArray().size() - 1).dispatchKeyEvent(e)) {
				// Sceneが1つしか起動していないときはゲーム終了
				if (getSceneArray().size() == 1) {
					ResourceUtil.getInstance(this).resetAllTexture();
					finish();
				} else {
					getEngine().setScene(getSceneArray().get(getSceneArray().size() - 2));
					getSceneArray().remove(getSceneArray().size() - 1);
				}
			}
			return true;
		} else if (e.getAction() == KeyEvent.ACTION_DOWN && e.getKeyCode() == KeyEvent.KEYCODE_MENU) {
			getSceneArray().get(getSceneArray().size() - 1).dispatchKeyEvent(e);
			return true;
		}
		return false;
	}
}
