package com.kyokomi.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Spriteのインスタンス化を簡単に行う為のクラス.
 * シングルトンです。
 * TextureRegionの再生性を防ぎ再利用する為、プールを保持している。
 * 
 * @author kyokomi
 *
 */
public class ResourceUtil {

	/** 自身のインスタンス. */
	private static ResourceUtil self;
	/** Context. */
	private static BaseGameActivity gameActivity;
	/** TextureRegionの無駄な生成を防ぎ、再利用する為の一時的な格納場所. */
	private static Map<String, ITextureRegion> textureRegionPool;
	/** TiledTextureRegionの無駄な生成を防ぎ、再利用する為の一時的な格納場所. */
	private static Map<String, TiledTextureRegion> tiledTextureRegionPool;
	
	private ResourceUtil() {
		
	}
	
	/**
	 * イニシャライズ.
	 * @param gameActivity
	 * @return
	 */
	public static ResourceUtil getInstance(BaseGameActivity gameActivity) {
		if (self == null) {
			self = new ResourceUtil();
			ResourceUtil.gameActivity = gameActivity;
			BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			
			textureRegionPool = new HashMap<String, ITextureRegion>();
			tiledTextureRegionPool = new HashMap<String, TiledTextureRegion>();
		}
		
		return self;
	}
	
	/**
	 * 指定ファイルのSpriteを取得.
	 * 再生性しないようにプールしている。
	 * @param fileName ファイル名
	 * @return Sprite
	 */
	public Sprite getSprite(String fileName) {
		return getSprite(fileName, 0, 0);
	}
	/**
	 * 指定ファイルのSpriteを取得.
	 * 再生性しないようにプールしている。
	 * @param fileName ファイル名
	 * @return Sprite
	 */
	public Sprite getSprite(String fileName, int column, int row) {
		// 同名のファイルからITextureRegionが生成済みであれば再利用
		if (textureRegionPool.containsKey(fileName)) {
			Sprite s = new Sprite(0, 0, textureRegionPool.get(fileName), 
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
		// サイズを自動的に取得する為にBitmapとして読み込み
		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets().open("gfx/" + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		// Bitmapのサイズを基に2のべき乗の値を取得、BitmapTextureAtlasの生成
		BitmapTextureAtlas bta = new BitmapTextureAtlas(gameActivity.getTextureManager(), 
				getTwoPowerSize(bm.getWidth()), getTwoPowerSize(bm.getHeight()),
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameActivity.getEngine().getTextureManager().loadTexture(bta);
		
		ITextureRegion btr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				bta, gameActivity, fileName, 0, 0);
		Sprite s = new Sprite(0, 0, btr, gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// 再生性を防ぐ為、プールの登録
		textureRegionPool.put(fileName, btr);
		
		return s;
	}
	
	public TiledSprite getTiledSprite(String fileName, int column, int row) {
		if (tiledTextureRegionPool.containsKey(fileName)) {
			TiledSprite s = new TiledSprite(0, 0, 
					tiledTextureRegionPool.get(fileName), 
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
		
		// サイズを自動的に取得する為にBitmapとして読み込み
		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets().open("gfx/" + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		// Bitmapのサイズを基に2のべき乗の値を取得、BitmapTextureAtlasの生成
		BitmapTextureAtlas bta = new BitmapTextureAtlas(gameActivity.getTextureManager(), 
				getTwoPowerSize(bm.getWidth()), getTwoPowerSize(bm.getHeight()),
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameActivity.getEngine().getTextureManager().loadTexture(bta);
		
		TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				bta, gameActivity, fileName, 0, 0, column, row);
		TiledSprite s = new TiledSprite(0, 0, ttr, gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// 再生性を防ぐ為、プールの登録
		tiledTextureRegionPool.put(fileName, ttr);
		
		return s;
	}
	
	public AnimatedSprite getAnimatedSprite(String fileName, int column, int row) {
		if (tiledTextureRegionPool.containsKey(fileName)) {
			AnimatedSprite s = new AnimatedSprite(0, 0, 
					tiledTextureRegionPool.get(fileName), 
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
		
		// サイズを自動的に取得する為にBitmapとして読み込み
		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets().open("gfx/" + fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		// Bitmapのサイズを基に2のべき乗の値を取得、BitmapTextureAtlasの生成
		BitmapTextureAtlas bta = new BitmapTextureAtlas(gameActivity.getTextureManager(), 
				getTwoPowerSize(bm.getWidth()), getTwoPowerSize(bm.getHeight()),
				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		gameActivity.getEngine().getTextureManager().loadTexture(bta);
		
		TiledTextureRegion ttr = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				bta, gameActivity, fileName, 0, 0, column, row);
		AnimatedSprite s = new AnimatedSprite(0, 0, ttr, gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		// 再生性を防ぐ為、プールの登録
		tiledTextureRegionPool.put(fileName, ttr);
		
		return s;
	}
	
	/**
	 * ボタン生成.
	 * @param normal
	 * @param pressed
	 * @return
	 */
	public ButtonSprite getButtonSprite(String normal ,String pressed) {
		
		if (textureRegionPool.containsKey(normal) && textureRegionPool.containsKey(pressed)) {
			ButtonSprite s = new ButtonSprite(0, 0, 
					textureRegionPool.get(normal), textureRegionPool.get(pressed),
					gameActivity.getVertexBufferObjectManager());
			s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			return s;
		}
				
		// サイズを自動的に取得する為にBitmapとして読み込み
		InputStream is = null;
		try {
			is = gameActivity.getResources().getAssets().open("gfx/" + normal);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Bitmap bm = BitmapFactory.decodeStream(is);
		
		// Bitmapのサイズを基に2のべき乗の値を取得、BitmapTextureAtlasの生成
		BuildableBitmapTextureAtlas bta = new BuildableBitmapTextureAtlas(
				gameActivity.getTextureManager(), 
				getTwoPowerSize(bm.getWidth() * 2), 
				getTwoPowerSize(bm.getHeight()));
		
		ITextureRegion trNormal = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				bta, gameActivity, normal);
		ITextureRegion trPressed = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				bta, gameActivity, pressed);
		
		try {
			bta.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			bta.load();
		} catch (TextureAtlasBuilderException e) {
			e.printStackTrace();
		}

		textureRegionPool.put(normal, trNormal);
		textureRegionPool.put(pressed, trPressed);
		
		ButtonSprite s = new ButtonSprite(0, 0, trNormal, trPressed, 
				gameActivity.getVertexBufferObjectManager());
		s.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		return s;
	}
	
	/**
	 * プールを開放、シングルトンを削除する.
	 */
	public void resetAllTexture() {
		/*
		 * Activity.finish()だけだとシングルトンなクラスがnullにならない為、明示的にnullを代入
		 */
		self = null;
		textureRegionPool.clear();
		tiledTextureRegionPool.clear();
	}
	
	/**
	 * 2のべき乗を求めて返す.
	 * @param size
	 * @return
	 */
	public int getTwoPowerSize(float size) {
		int value = (int) (size + 1);
		int pow2Value = 64;
		while (pow2Value < value) { 
			pow2Value *= 2;
		}
		return pow2Value;
	}
}
