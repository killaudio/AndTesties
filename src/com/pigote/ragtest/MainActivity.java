package com.pigote.ragtest;

import java.io.IOException;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.shape.IShape;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import android.hardware.SensorManager;
import android.os.Bundle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class MainActivity extends BaseGameActivity implements
                IAccelerationListener, IOnSceneTouchListener, IOnAreaTouchListener {

	public int m_CameraWidth = 540;
    public int m_CameraHeight = 960;

    private final PhysicsWorld m_PhysicsWorld = new PhysicsWorld(new Vector2(0,
                        SensorManager.GRAVITY_EARTH), true);

    private VertexBufferObjectManager vbom;
    private Scene scene;
    
    ITextureRegion m_Head;
    ITextureRegion m_Torso1;
    ITextureRegion m_Torso2;
    ITextureRegion m_Torso3;
    ITextureRegion m_UpperArmLeft;
    ITextureRegion m_UpperArmRight;
    ITextureRegion m_LowerArmLeft;
    ITextureRegion m_LowerArmRight;
    ITextureRegion m_UpperLegLeft;
    ITextureRegion m_UpperLegRight;
    ITextureRegion m_LowerLegLeft;
    ITextureRegion m_LowerLegRight;
    ITextureRegion m_DebugTexture;

    Sprite m_HeadSprite;
    Sprite m_Torso1Sprite;
    Sprite m_Torso2Sprite;
    Sprite m_Torso3Sprite;
    Sprite m_UpperArmLeftSprite;
    Sprite m_UpperArmRightSprite;
    Sprite m_LowerArmLeftSprite;
    Sprite m_LowerArmRightSprite;
    Sprite m_UpperLegLeftSprite;
    Sprite m_UpperLegRightSprite;
    Sprite m_LowerLegLeftSprite;
    Sprite m_LowerLegRightSprite;

    Sprite m_DebugTextureSprite;
    
    public BuildableBitmapTextureAtlas gameTextureAtlas;

    @Override
    public EngineOptions onCreateEngineOptions() {

    	Camera camera = new Camera(0, 0, m_CameraWidth, m_CameraHeight);
        EngineOptions options = new EngineOptions(true,
                            ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(
                                            m_CameraWidth, m_CameraHeight), camera);

        options.getAudioOptions().setNeedsMusic(false);
        options.getAudioOptions().setNeedsSound(false);
        options.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        //options.getTouchOptions().setRunOnUpdateThread(true);
        
        return options;
    }

    @Override
    public void onCreateResources(
    			OnCreateResourcesCallback pOnCreateResourcesCallback)
    			throws IOException {
    	vbom = getVertexBufferObjectManager();
    	
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
    	gameTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.NEAREST);

        m_Head = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "head.png");
        m_Torso1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "torso1.png");
        m_Torso2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "torso2.png");
        m_Torso3 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "torso3.png");
        m_UpperArmLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "upper_arm_left.png");
        m_UpperArmRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "upper_arm_right.png");
        m_LowerArmLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                           "lower_arm_left.png");
        m_LowerArmRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "lower_arm_right.png");
        m_UpperLegLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "upper_leg_left.png");
        m_UpperLegRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "upper_leg_right.png");
        m_LowerLegLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "lower_leg_left.png");
        m_LowerLegRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this,
                            "lower_leg_right.png");

        mEngine.getTextureManager().loadTexture(gameTextureAtlas);
        this.enableAccelerationSensor(this);
    }

    	@Override
  	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
   			throws IOException {
   		scene = new Scene();
            
        scene.setOnSceneTouchListener(this);
        scene.setOnAreaTouchListener(this);

        scene.setBackground(new Background(1, 1, 1));
        scene.setBackgroundEnabled(true);

        mGroundBody = this.m_PhysicsWorld.createBody(new BodyDef());
            
        final Rectangle ground = new Rectangle(m_CameraHeight / 2, 1, m_CameraWidth, 2, vbom);
		final Rectangle roof = new Rectangle(m_CameraWidth / 2, m_CameraHeight - 1, m_CameraWidth, 2, vbom);
		final Rectangle left = new Rectangle(1, m_CameraWidth / 2, 1, m_CameraHeight, vbom);
		final Rectangle right = new Rectangle(m_CameraWidth - 1, m_CameraHeight / 2, 2, m_CameraHeight, vbom);
            
        final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1.0f,
                            0.2f, 0.1f);

        PhysicsFactory.createBoxBody(m_PhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(m_PhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(m_PhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
        PhysicsFactory.createBoxBody(m_PhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

        createRagdoll();

        scene.registerTouchArea(m_HeadSprite);
        scene.registerTouchArea(m_Torso1Sprite);
        scene.registerTouchArea(m_Torso1Sprite);
        scene.registerTouchArea(m_Torso3Sprite);
        scene.registerTouchArea(m_UpperArmLeftSprite);
        scene.registerTouchArea(m_UpperArmRightSprite);         
        scene.registerTouchArea(m_LowerArmLeftSprite);
        scene.registerTouchArea(m_LowerArmRightSprite);         
        scene.registerTouchArea(m_UpperLegLeftSprite);
        scene.registerTouchArea(m_UpperLegRightSprite);         
        scene.registerTouchArea(m_LowerLegLeftSprite);
        scene.registerTouchArea(m_LowerLegRightSprite);
            
        scene.getChildByIndex(0).attachChild(m_HeadSprite);
        scene.getChildByIndex(0).attachChild(m_Torso1Sprite);
        scene.getChildByIndex(0).attachChild(m_Torso2Sprite);
        scene.getChildByIndex(0).attachChild(m_Torso3Sprite);
        scene.getChildByIndex(0).attachChild(m_UpperArmLeftSprite);
        scene.getChildByIndex(0).attachChild(m_UpperArmRightSprite);
        scene.getChildByIndex(0).attachChild(m_LowerArmLeftSprite);
        scene.getChildByIndex(0).attachChild(m_LowerArmRightSprite);
        scene.getChildByIndex(0).attachChild(m_UpperLegLeftSprite);
        scene.getChildByIndex(0).attachChild(m_UpperLegRightSprite);
        scene.getChildByIndex(0).attachChild(m_LowerLegLeftSprite);
        scene.getChildByIndex(0).attachChild(m_LowerLegRightSprite);

        // m_DebugTextureSprite = new Sprite(0, 0, m_DebugTexture);
        // scene.getChild(0).attachChild(m_DebugTextureSprite);

        scene.registerUpdateHandler(this.m_PhysicsWorld);

    }

    	@Override
    	public void onPopulateScene(Scene pScene,
    			OnPopulateSceneCallback pOnPopulateSceneCallback)
    			throws IOException {
    		// TODO Auto-generated method stub
    		
    	}

        /** Called when the activity is first created. */
        @Override
        public void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                // setContentView(R.layout.main);
        }

        Vector2 getLeftPoint(Sprite sprite, Body body) {
                float w = sprite.getWidthScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                float h = sprite.getHeightScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                Vector2 pnt = body.getPosition();
                pnt.x -= w;
                pnt.y -= h;

                return pnt;
        }

        Vector2 getRightPoint(Sprite sprite, Body body) {
                float w = sprite.getWidthScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                float h = sprite.getHeightScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                Vector2 pnt = body.getPosition();
                pnt.x += w;
                pnt.y += h;
                return pnt;
        }

        Vector2 getTopPoint(Sprite sprite, Body body) {
                float h = sprite.getHeightScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                Vector2 pnt = body.getPosition();
                pnt.y -= h;
                return pnt;
        }

        Vector2 getBottomPoint(Sprite sprite, Body body) {
                float h = sprite.getHeightScaled() * 0.5f
                                / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
                Vector2 pnt = body.getPosition();
                pnt.y += h;
                return pnt;
        }

        private void createRagdoll() {
                // TODO Auto-generated method stub
        		
                m_HeadSprite = new Sprite(0, 0, m_Head, vbom);
                m_Torso1Sprite = new Sprite(0, 0, m_Torso1, vbom);
                m_Torso2Sprite = new Sprite(0, 0, m_Torso2, vbom);
                m_Torso3Sprite = new Sprite(0, 0, m_Torso3, vbom);
                m_UpperArmLeftSprite = new Sprite(0, 0, m_UpperArmLeft, vbom);
                m_UpperArmRightSprite = new Sprite(0, 0, m_UpperArmRight, vbom);
                m_LowerArmLeftSprite = new Sprite(0, 0, m_LowerArmLeft, vbom);
                m_LowerArmRightSprite = new Sprite(0, 0, m_LowerArmRight, vbom);
                m_UpperLegLeftSprite = new Sprite(0, 0, m_UpperLegLeft, vbom);
                m_UpperLegRightSprite = new Sprite(0, 0, m_UpperLegRight, vbom);
                m_LowerLegLeftSprite = new Sprite(0, 0, m_LowerLegLeft, vbom);
                m_LowerLegRightSprite = new Sprite(0, 0, m_LowerLegRight, vbom);

                RevoluteJointDef jd = new RevoluteJointDef();
                FixtureDef fixtureDef = new FixtureDef();

                float startX = m_CameraWidth / 2;
                float startY = m_CameraHeight / 2;

                // BODIES

                // Head
                fixtureDef.density = 1.0f;
                fixtureDef.friction = 0.1f;
                fixtureDef.restitution = 0.2f;
                m_HeadSprite.setPosition(startX, startY);

                Body head = PhysicsFactory.createCircleBody(m_PhysicsWorld,
                                m_HeadSprite, BodyType.DynamicBody, fixtureDef);                
                
                m_HeadSprite.setUserData(head);
                
                // Makes him drunk ;)
                // head.applyLinearImpulse(new Vector2(50, 50), head.getWorldCenter());

                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_HeadSprite, head));

                // Torso1
                fixtureDef.density = 1.0f;
                fixtureDef.friction = 0.0f;
                fixtureDef.restitution = 1.0f;
                m_Torso1Sprite.setPosition(startX - 12.5f, startY + 125);

                Body torso1 = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_Torso1Sprite, BodyType.DynamicBody, fixtureDef);
                
                m_Torso1Sprite.setUserData(torso1);
                
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_Torso1Sprite, torso1));

                // Torso2
                m_Torso2Sprite.setPosition(startX - 12.5f, startY + 225 - 30);

                Body torso2 = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_Torso2Sprite, BodyType.DynamicBody, fixtureDef);
                
                m_Torso2Sprite.setUserData(torso2);

                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_Torso2Sprite, torso2));

                // Torso3
                m_Torso3Sprite.setPosition(startX - 12.5f, startY + 325 - 60);

                Body torso3 = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_Torso3Sprite, BodyType.DynamicBody, fixtureDef);
                
                m_Torso3Sprite.setUserData(torso3);

                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_Torso3Sprite, torso3));

                // UpperArm

                // L
                m_UpperArmLeftSprite.setPosition(startX - 167.5f - 30f, startY + 125);

                Body upperArmL = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_UpperArmLeftSprite, BodyType.DynamicBody, fixtureDef);

                m_UpperArmLeftSprite.setUserData(upperArmL);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_UpperArmLeftSprite, upperArmL));

                // R
                m_UpperArmRightSprite.setPosition(startX + 112.5f + 30f, startY + 125);

                Body upperArmR = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_UpperArmRightSprite, BodyType.DynamicBody, fixtureDef);
                
                m_UpperArmRightSprite.setUserData(upperArmR);

                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_UpperArmRightSprite, upperArmR));

                // LowerArm

                // L
                m_LowerArmLeftSprite.setPosition(startX - 347.5f - 10f, startY + 125);
                Body lowerArmL = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_LowerArmLeftSprite, BodyType.DynamicBody, fixtureDef);
                
                m_LowerArmLeftSprite.setUserData(lowerArmL);

                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_LowerArmLeftSprite, lowerArmL));

                // R
                m_LowerArmRightSprite.setPosition(startX + 292.5f + 10f, startY + 125);
                Body lowerArmR = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_LowerArmRightSprite, BodyType.DynamicBody, fixtureDef);

                m_LowerArmRightSprite.setUserData(lowerArmR);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_LowerArmRightSprite, lowerArmR));

                // UpperLeg

                // L
                m_UpperLegLeftSprite.setPosition(startX - 12.5f, startY + 425 - 40);
                Body upperLegL = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_UpperLegLeftSprite, BodyType.DynamicBody, fixtureDef);

                m_UpperLegLeftSprite.setUserData(upperLegL);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_UpperLegLeftSprite, upperLegL));

                // R
                m_UpperLegRightSprite.setPosition(startX + 62.5f, startY + 425 - 40);
                Body upperLegR = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_UpperLegRightSprite, BodyType.DynamicBody, fixtureDef);

                m_UpperLegRightSprite.setUserData(upperLegR);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_UpperLegRightSprite, upperLegR));

                // LowerLeg

                // L
                m_LowerLegLeftSprite.setPosition(startX + 2.5f, startY + 645 - 70);
                Body lowerLegL = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_LowerLegLeftSprite, BodyType.DynamicBody, fixtureDef);

                m_LowerLegLeftSprite.setUserData(lowerLegL);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_LowerLegLeftSprite, lowerLegL));

                // R
                m_LowerLegRightSprite.setPosition(startX + 62.5f, startY + 645 - 70);
                Body lowerLegR = PhysicsFactory.createBoxBody(m_PhysicsWorld,
                                m_LowerLegRightSprite, BodyType.DynamicBody, fixtureDef);

                m_LowerLegRightSprite.setUserData(lowerLegR);
                
                m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
                                m_LowerLegRightSprite, lowerLegR));

                // JOINTS
                jd.enableLimit = true;

                // Head to shoulders
                jd.lowerAngle = (float) (-40 / (180 / Math.PI));
                jd.upperAngle = (float) (40 / (180 / Math.PI));

                // jd.initialize(torso1,
                // head,head.getWorldPoint(head.getLocalCenter()));
                jd.initialize(torso1, head, getTopPoint(m_Torso1Sprite, torso1));
                m_PhysicsWorld.createJoint(jd);

                // Upper arm to shoulders
                // L
                jd.lowerAngle = (float) (-85 / (180 / Math.PI));
                jd.upperAngle = (float) (130 / (180 / Math.PI));

                // jd.initialize(torso1, upperArmL, getLeftPoint(m_Torso1Sprite,
                // torso1));
                jd.initialize(torso1, upperArmL, getRightPoint(m_UpperArmLeftSprite,
                                upperArmL));

                m_PhysicsWorld.createJoint(jd);
                // R
                jd.lowerAngle = (float) (-130 / (180 / Math.PI));
                jd.upperAngle = (float) (85 / (180 / Math.PI));
                // jd.initialize(torso1, upperArmR, getRightPoint(m_Torso1Sprite,
                // torso1));
                jd.initialize(torso1, upperArmR, getLeftPoint(m_UpperArmRightSprite,
                                upperArmR));

                m_PhysicsWorld.createJoint(jd);

                // Lower arm to upper arm
                // L
                jd.lowerAngle = (float) (-130 / (180 / Math.PI));
                jd.upperAngle = (float) (10 / (180 / Math.PI));
                jd.initialize(upperArmL, lowerArmL, getLeftPoint(m_UpperArmLeftSprite,
                                upperArmL));
                m_PhysicsWorld.createJoint(jd);
                // R
                jd.lowerAngle = (float) (-10 / (180 / Math.PI));
                jd.upperAngle = (float) (130 / (180 / Math.PI));
                jd.initialize(upperArmR, lowerArmR, getRightPoint(
                                m_UpperArmRightSprite, upperArmR));
                m_PhysicsWorld.createJoint(jd);

                // Shoulders/stomach
                jd.lowerAngle = (float) (-15 / (180 / Math.PI));
                jd.upperAngle = (float) (15 / (180 / Math.PI));
                jd.initialize(torso1, torso2, getBottomPoint(m_Torso1Sprite, torso1));
                m_PhysicsWorld.createJoint(jd);
                // Stomach/hips
                jd.initialize(torso2, torso3, getBottomPoint(m_Torso2Sprite, torso2));
                m_PhysicsWorld.createJoint(jd);

                // Torso to upper leg
                // L
                jd.lowerAngle = (float) (-25 / (180 / Math.PI));
                jd.upperAngle = (float) (45 / (180 / Math.PI));
                jd
                                .initialize(torso3, upperLegL, getBottomPoint(m_Torso3Sprite,
                                                torso3));
                m_PhysicsWorld.createJoint(jd);
                // R
                jd.lowerAngle = (float) (-45 / (180 / Math.PI));
                jd.upperAngle = (float) (25 / (180 / Math.PI));
                jd
                                .initialize(torso3, upperLegR, getBottomPoint(m_Torso3Sprite,
                                                torso3));
                m_PhysicsWorld.createJoint(jd);

                // Upper leg to lower leg
                // L
                jd.lowerAngle = (float) (-25 / (180 / Math.PI));
                jd.upperAngle = (float) (75 / (180 / Math.PI));
                jd.initialize(upperLegL, lowerLegL, getBottomPoint(
                                m_UpperLegLeftSprite, upperLegL));
                m_PhysicsWorld.createJoint(jd);
                // R
                jd.lowerAngle = (float) (-75 / (180 / Math.PI));
                jd.upperAngle = (float) (25 / (180 / Math.PI));
                jd.initialize(upperLegR, lowerLegR, getBottomPoint(
                                m_UpperLegLeftSprite, upperLegR));
                m_PhysicsWorld.createJoint(jd);
        }

    	@Override
    	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
    		// TODO Auto-generated method stub
    		
    	}

    	@Override
   	public void onAccelerationChanged(AccelerationData pAccelerationData) {
    		m_PhysicsWorld.setGravity(new Vector2(1 * pAccelerationData.getY(),
    				1 * pAccelerationData.getX()));
   	}
        
    private MouseJoint mMouseJointActive;
    private Body mGroundBody;
     
 	@Override
 	public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 			ITouchArea pTouchArea, float pTouchAreaLocalX,
 			float pTouchAreaLocalY) {
 		 if(pSceneTouchEvent.isActionDown()) {
              final IShape face = (IShape) pTouchArea;
              /*
               * If we have a active MouseJoint, we are just moving it around
               * instead of creating a second one.
               */
              if (this.mMouseJointActive == null) {
                      this.mEngine.vibrate(100);
                      this.mMouseJointActive = this.createMouseJoint(face, pTouchAreaLocalX, pTouchAreaLocalY);
              }
              return true;
      }
      return false;
 	}
     
     @Override
     public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
             if (this.m_PhysicsWorld != null) {
                     switch(pSceneTouchEvent.getAction()) {
                          
                             case TouchEvent.ACTION_MOVE:
                                     if(this.mMouseJointActive != null) {
                                             final Vector2 vec = Vector2Pool.obtain(pSceneTouchEvent.getX() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, pSceneTouchEvent.getY() / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
                                             this.mMouseJointActive.setTarget(vec);
                                             Vector2Pool.recycle(vec);
                                     }
                                     return true;
                             case TouchEvent.ACTION_UP:
                                     if(this.mMouseJointActive != null) {
                                             this.m_PhysicsWorld.destroyJoint(this.mMouseJointActive);
                                             this.mMouseJointActive = null;
                                     }
                                     return true;
                     }
                     return false;
             }
             return false;
     }
     
     public MouseJoint createMouseJoint(final IShape pFace, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
         final Body body = (Body) pFace.getUserData();
         final MouseJointDef mouseJointDef = new MouseJointDef();

         final Vector2 localPoint = Vector2Pool.obtain((pTouchAreaLocalX - pFace.getWidth() * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, (pTouchAreaLocalY - pFace.getHeight() * 0.5f) / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
         this.mGroundBody.setTransform(localPoint, 0);
         
         mouseJointDef.bodyA = this.mGroundBody;
         mouseJointDef.bodyB = body;
         mouseJointDef.dampingRatio = 0.95f;
         mouseJointDef.frequencyHz = 30f;
         mouseJointDef.maxForce = (200.0f * body.getMass());
         mouseJointDef.collideConnected = true;

         mouseJointDef.target.set(body.getWorldPoint(localPoint));
         Vector2Pool.recycle(localPoint);

         return (MouseJoint) this.m_PhysicsWorld.createJoint(mouseJointDef);
     }

}