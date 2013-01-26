package com.kyokomi.srpgquest.scene;

import java.util.ArrayList;
import java.util.List;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.IEntityModifier;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.texture.Texture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.dialog.StringInputDialogBuilder;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.call.Callback;
import org.andengine.util.color.Color;
import org.andengine.util.modifier.IModifier;

import com.kyokomi.core.activity.MultiSceneActivity;
import com.kyokomi.core.dto.ActorPlayerDto;
import com.kyokomi.core.dto.PlayerTalkDto;
import com.kyokomi.core.dto.PlayerTalkDto.TalkDirection;
import com.kyokomi.core.scene.KeyListenScene;
import com.kyokomi.core.sprite.MenuRectangle;
import com.kyokomi.core.sprite.PlayerSprite;
import com.kyokomi.core.sprite.PlayerStatusRectangle;
import com.kyokomi.core.sprite.TalkLayer;
import com.kyokomi.core.sprite.TextButton;
import com.kyokomi.srpgquest.R;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.widget.Toast;

public class SandboxScene extends KeyListenScene 
	implements IOnSceneTouchListener{
	
	private PlayerSprite player;
	private PlayerSprite enemy;
	private PlayerStatusRectangle mPlayerStatusRectangle;
	private TalkLayer talkLayer;
	private Font mFont;
	
	public SandboxScene(MultiSceneActivity baseActivity) {
		super(baseActivity);
		init();
		
		// フォント作成
		initFont();
		
		// FPS表示
		initFps(getWindowWidth() - 100, getWindowHeight() - 20, mFont);
		
		// --- test ---
		sampleMenuRectangle();
		
		samplePlayerStatus();
	}
	
//	private FPSCounter mFpsCounter;
//	private Text mFpsText;
//	/**
//	 * FPSの画面表示.
//	 */
//	private void initFps(float x, float y, Font font) {
//		mFpsText= new Text(x, y, font, "FPS:0.000000000000000000000000", 
//				getBaseActivity().getVertexBufferObjectManager());
//		attachChild(mFpsText);
//
//		mFpsCounter = new FPSCounter();
//		registerUpdateHandler(mFpsCounter);
//		registerUpdateHandler(new TimerHandler(1/60f, true, new ITimerCallback() {
//			@Override
//			public void onTimePassed(TimerHandler pTimerHandler) {
//				if (mFpsCounter != null) {
//					mFpsText.setText("FPS:" + mFpsCounter.getFPS());
//				}
//			}
//		}));
//	}
	
	@Override
	public void init() {
		testShowGrid();
		testBtnCreate();
		
		float scale = 2.0f;
		
		// プレイヤー配置
		ActorPlayerDto actorPlayer = new ActorPlayerDto();
		actorPlayer.setPlayerId(1);
		actorPlayer.setName("アスリーン");
		actorPlayer.setImageResId(110);
		actorPlayer.setMovePoint(5);
		actorPlayer.setAttackRange(1);
		
		actorPlayer.setHitPoint(100);
		actorPlayer.setAttackPoint(60);
		actorPlayer.setDefencePoint(10);
		
		player = new PlayerSprite(actorPlayer, this, 
				0, 0, getWindowWidth(), getWindowHeight(), 
				scale,
				getBaseActivity().getVertexBufferObjectManager());
		player.setPlayerToAttackPosition();
		player.setPlayerFlippedHorizontal(true);
		player.setPlayerPosition(130, 150);
		attachChild(player);
		
		// 敵
		ActorPlayerDto actorPlayer2 = new ActorPlayerDto();
		actorPlayer2.setPlayerId(2);
		actorPlayer2.setImageResId(34);
		actorPlayer2.setMovePoint(5);
		actorPlayer2.setAttackRange(1);
		
		actorPlayer2.setHitPoint(100);
		actorPlayer2.setAttackPoint(60);
		actorPlayer2.setDefencePoint(10);
		enemy = new PlayerSprite(actorPlayer2, this, 
				0, 0, getWindowWidth(), getWindowHeight(), 
				scale,
				getBaseActivity().getVertexBufferObjectManager());
		enemy.setPlayerToAttackPosition();
		enemy.setPlayerPosition(getWindowWidth() - 100, 150);
		attachChild(enemy);

		// 会話プレイヤーリストを作成
		SparseArray<TiledSprite> playerSprite = new SparseArray<TiledSprite>();
		playerSprite.put(player.getPlayerId(), player.getPlayerFace());
		playerSprite.put(enemy.getPlayerId(), enemy.getPlayerFace());
		
		// 会話内容を作成
		List<PlayerTalkDto> talks = new ArrayList<PlayerTalkDto>();
		talks.add(new PlayerTalkDto(player.getPlayerId(), 
				player.getActorPlayer().getName(), 0, TalkDirection.TALK_DIRECT_LEFT,
				"これは、ゲームであっても、遊びではない。"));
		talks.add(new PlayerTalkDto(enemy.getPlayerId(), 
				"ラーティ・クルス", 0, TalkDirection.TALK_DIRECT_RIGHT,
				"こんにちわ。"));
		talks.add(new PlayerTalkDto(player.getPlayerId(), 
				player.getActorPlayer().getName(), 3, TalkDirection.TALK_DIRECT_LEFT,
				"言っとくが俺はソロだ。\n１日２日オレンジになるくらいどおって事ないぞ。"));
		talks.add(new PlayerTalkDto(player.getPlayerId(), 
				player.getActorPlayer().getName(), 2, TalkDirection.TALK_DIRECT_LEFT,
				"レベルなんてタダの数字だよ。\nこの世界での強さは、単なる幻想に過ぎない。\nそんなものよりもっと大事なものがある。"));
		talks.add(new PlayerTalkDto(enemy.getPlayerId(), 
				"ラーティ・クルス", 1, TalkDirection.TALK_DIRECT_RIGHT,
				"なんでや！！\n何でディアベルハンを見殺しにしたんや！"));
		
		// 会話レイヤーを生成
		talkLayer = new TalkLayer(this);
		talkLayer.initTalk(playerSprite, talks);
		attachChild(talkLayer);
		
		// Sceneのタッチリスナーを登録
		setOnSceneTouchListener(this);
	}
	
	@Override
	public void prepareSoundAndMusic() {
			
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		return false;
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		
		// タッチの座標を取得
		float x = pSceneTouchEvent.getX();
		float y = pSceneTouchEvent.getY();
		
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			if (talkLayer.contains(x, y)) {
				
				if (talkLayer.nextTalk()) {
					return true;
				}
				talkLayer.hide();
			}
		}
		return false;
	}

	// -----------------------------------------------------
	// お試し系
	
	private TextButton textButtonSprite;
	
	private void sampleMenuScene() {
		
		Text text = new Text(16, 16, mFont, 
				"-------------", 
				new TextOptions(HorizontalAlign.CENTER), 
				getBaseActivity().getVertexBufferObjectManager());
		
		textButtonSprite = new TextButton(text, 
				getWindowWidth() / 2 - text.getWidth() / 2, getWindowHeight()/ 2 - text.getHeight() / 2,
				20, 20, 
				getBaseActivity().getVertexBufferObjectManager(), 
				new TextButton.OnClickListener() {
					@Override
					public void onClick(TextButton pTextButtonSprite,
							float pTouchAreaLocalX, float pTouchAreaLocalY) {
//						Log.d("TextButtonSprite", "Touch!!!!!!!");
					}
				});
		attachChild(textButtonSprite);
		registerTouchArea(textButtonSprite);
	}
	
	private MenuRectangle mMenuRectangle;
	
	private void sampleMenuRectangle() {
		
		Font font = createFont(Typeface.DEFAULT_BOLD, 22, Color.WHITE);
		
		mMenuRectangle = new MenuRectangle(
				getWindowWidth() / 2, getWindowHeight() / 2, 
				getWindowWidth(), getWindowHeight(), 
				getBaseActivity().getVertexBufferObjectManager());
		
		for (int i = 0; i < 4; i++) {
			TextButton textButton = createTextButton(i+1, font);
			mMenuRectangle.addMenuItem(textButton.getTag(), textButton);
		}
		mMenuRectangle.create(2);
		attachChild(mMenuRectangle);
		// 非表示
		mMenuRectangle.setEnabled(false);
		mMenuRectangle.setVisible(false);
		registerTouchArea(mMenuRectangle);
	}
	
	private void samplePlayerStatus() {
		mPlayerStatusRectangle = new PlayerStatusRectangle(this, player, mFont, getWindowWidth() / 2, 0, 
				getWindowWidth() / 2, getWindowHeight() / 2, 
				getBaseActivity().getVertexBufferObjectManager());
		attachChild(mPlayerStatusRectangle);
	}
	
	private TextButton createTextButton(int id, Font pFont) {
		Text text = new Text(0, 0, pFont, 
				"-------------", 
				new TextOptions(HorizontalAlign.CENTER), 
				getBaseActivity().getVertexBufferObjectManager());
		TextButton textButton = new TextButton(text, 
				getWindowWidth() / 2 - text.getWidth() / 2, getWindowHeight()/ 2 - text.getHeight() / 2,
				5, 5, 
				getBaseActivity().getVertexBufferObjectManager(), 
				new TextButton.OnClickListener() {
					@Override
					public void onClick(TextButton pTextButtonSprite,
							float pTouchAreaLocalX, float pTouchAreaLocalY) {
//						Log.d("TextButtonSprite", "Touch!!!!!!!");
					}
				});
		textButton.setTag(id);
		
		return textButton;
	}
	
	private void initFont() {
		mFont = createFont(Typeface.DEFAULT, 16, Color.WHITE);
	}
	
	private Font createFont(Typeface typeface, int fontSize, Color color) {
		Texture texture = new BitmapTextureAtlas(
				this.getBaseActivity().getTextureManager(), 512, 512, 
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		
		Font font = new Font(this.getBaseActivity().getFontManager(), 
				texture, typeface, fontSize, true, color);
		
		this.getBaseActivity().getTextureManager().loadTexture(texture);
		this.getBaseActivity().getFontManager().loadFont(font);
		
		return font;
	}
	/**
	 * 文字列入力ダイアログ生成サンプル.
	 */
	private void sampleStringInputDialogBuilder() {
		
		// UIスレッド上でないと動きません
		getBaseActivity().runOnUiThread(new Runnable() {
		     @Override
		     public void run() {
		    	 // Dialogを生成 
		 		 StringInputDialogBuilder builder = new StringInputDialogBuilder(getBaseActivity(), 
						R.string.user_regist_title,  // タイトル文言のリソースID
						R.string.user_regist_detail, // 本文のリソースID
						R.string.user_regist_error,  // エラー時のToastで表示される文言のリソースID
						R.drawable.ic_launcher,      // タイトル横のアイコン画像のリソースID
						
						/* 
						 * OKボタンを押した時のコールバック.
						 * 引数pCallbackValueに入力した文字列が入ってくる 
						 */
						new Callback<String>() {
							@Override
							public void onCallback(String pCallbackValue) {
								Toast.makeText(getBaseActivity(), 
										"「" + pCallbackValue + "」で登録しました。", 
										Toast.LENGTH_SHORT).show();
							}
						},
						
						/*
						 * Cancelボタンを押した時のコールバック. 
						 */
						new DialogInterface.OnCancelListener() {
							@Override
							public void onCancel(DialogInterface dialog) {
								Toast.makeText(getBaseActivity(), "登録をやめました。", 
										Toast.LENGTH_SHORT).show();
							}
						});
		 		 // createで生成
		 		 final Dialog dialog = builder.create();
		 		
		 		 dialog.show();
		     }
		});
	}
	
	
	// ---- グリッド表示 ----
	
	private void testShowGrid() {
		int base = 42;
		int baseX = (int)(base * 1.5);
		int baseGrid = 400;
		
		for (int x = -10 ; x < 20; x++) {
			final Line line = new Line(baseX * x, 0, (x * baseX) + baseGrid, getWindowHeight(), getBaseActivity().getVertexBufferObjectManager());
			line.setLineWidth(1);
			line.setColor(255, 255, 255);
			attachChild(line);
		}
		
		for (int y = -10 ; y < 20; y++) {
			final Line line = new Line(0, (base * y), getWindowWidth(), (y * base) - (baseGrid / 2), getBaseActivity().getVertexBufferObjectManager());
			line.setLineWidth(1);
			line.setColor(255, 255, 255);
			attachChild(line);
		}
	}
	
	// ---- test用ボタン ----
	
	private SparseArray<Sprite> btnSpriteMap;
	
	private void testBtnCreate() {
		btnSpriteMap = new SparseArray<Sprite>();
		ButtonSprite btn1 = getResourceButtonSprite("p_ms1_0.gif", "p_ms1_1.gif");
		addMenu(btn1, 1, 0);
		ButtonSprite btn2 = getResourceButtonSprite("p_ms2_0.gif", "p_ms2_1.gif");
		addMenu(btn2, 2, getLeftWidthToX(btn1));
		ButtonSprite btn3 = getResourceButtonSprite("p_ms3_0.gif", "p_ms3_1.gif");
		addMenu(btn3, 3, getLeftWidthToX(btn2));
		ButtonSprite btn4 = getResourceButtonSprite("p_ms4_0.gif", "p_ms4_1.gif");
		addMenu(btn4, 4, getLeftWidthToX(btn3));
		ButtonSprite btn5 = getResourceButtonSprite("p_ms5_0.gif", "p_ms5_1.gif");
		addMenu(btn5, 5, getLeftWidthToX(btn4));
		ButtonSprite btn6 = getResourceButtonSprite("p_ms6_0.gif", "p_ms6_1.gif");
		addMenu(btn6, 6, getLeftWidthToX(btn5));
		ButtonSprite btn7 = getResourceButtonSprite("p_ms7_0.gif", "p_ms7_1.gif");
		addMenu(btn7, 7, getLeftWidthToX(btn6));
		ButtonSprite btn8 = getResourceButtonSprite("p_ms8_0.gif", "p_ms8_1.gif");
		addMenu(btn8, 8, getLeftWidthToX(btn7));
		ButtonSprite btn9 = getResourceButtonSprite("p_ms9_0.gif", "p_ms9_1.gif");
		addMenu(btn9, 9, getLeftWidthToX(btn8));
		ButtonSprite btn10 = getResourceButtonSprite("p_ms1_0.gif", "p_ms1_1.gif");
		addMenu(btn10, 10, getLeftWidthToX(btn9));
	}
	private float getLeftWidthToX(Sprite sprite) {
		return (0 + 2 + sprite.getX() + (sprite.getWidth()));
	}
	private void addMenu(ButtonSprite menuSprite, int tag, float x) {
		menuSprite.setTag(tag);
		menuSprite.setPosition(x, 0);
		attachChild(menuSprite);
		btnSpriteMap.put(tag, menuSprite);
		registerTouchArea(menuSprite);
		
		menuSprite.setOnClickListener(new ButtonSprite.OnClickListener() {
			
			@Override
			public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX,
					float pTouchAreaLocalY) {
				if (btnSpriteMap.indexOfKey(pButtonSprite.getTag()) >= 0) {
					final Sprite sprite = btnSpriteMap.get(pButtonSprite.getTag());
					
					switch (sprite.getTag()) {
					case 1:
						player.setPlayerToAttackPosition();
						break;
					case 2:
						player.setPlayerToJumpPositon();
						break;
					case 3:
						player.setPlayerToSlidePositon();
						break;
					case 4:
						player.attack2();
						break;
					case 5:
//						player.setPlayerToAttackPosition();
						talkLayer.resetTalk();
						talkLayer.nextTalk();
						break;
					case 6:
						enemy.attack2();
						break;
					case 7:
						player.showCutIn(2.0f, getWindowWidth(), new IEntityModifier.IEntityModifierListener() {
							@Override
							public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
							}
							@Override
							public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
								player.attack2();
							}
						});
						break;
					case 8:
						enemy.showCutIn(2.0f, getWindowWidth(), new IEntityModifier.IEntityModifierListener() {
							@Override
							public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
							}
							@Override
							public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
								enemy.attack2();
							}
						});
						break;
					case 9:
						sampleStringInputDialogBuilder();
						break;
					case 10:
						if (textButtonSprite != null) { 
							if (textButtonSprite.isEnabled()) {
								textButtonSprite.setEnabled(false);
							} else {
								textButtonSprite.setEnabled(true);
							}
						}
						if (mMenuRectangle != null) {
							if (mMenuRectangle.isVisible()) {
								mMenuRectangle.setEnabled(false);
								mMenuRectangle.setVisible(false);
							} else {
								mMenuRectangle.setEnabled(true);
								mMenuRectangle.setVisible(true);
							}
						}
						break;
					default:
						break;
					}
				}
			}
		});
	}
}
