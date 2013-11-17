package com.pigote.ragtest;

import java.io.IOException;

import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
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
import org.andengine.entity.sprite.UncoloredSprite;
import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.shader.PositionTextureCoordinatesShaderProgram;
import org.andengine.opengl.shader.ShaderProgram;
import org.andengine.opengl.shader.constants.ShaderProgramConstants;
import org.andengine.opengl.shader.exception.ShaderProgramException;
import org.andengine.opengl.shader.exception.ShaderProgramLinkException;
import org.andengine.opengl.texture.PixelFormat;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.render.RenderTexture;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.opengl.vbo.attribute.VertexBufferObjectAttributes;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.adt.color.Color;
import org.andengine.util.debug.Debug;

import android.annotation.SuppressLint;
import android.opengl.GLES20;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;

public class MainActivity extends BaseGameActivity implements
                IOnSceneTouchListener, IOnAreaTouchListener {

	public int m_CameraWidth = 540;
    public int m_CameraHeight = 960;

    private FixedStepPhysicsWorld m_PhysicsWorld;

    private VertexBufferObjectManager vbom;
    
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
    
	private float mShockwaveTime = 0f;
	private Camera camera;
	
    public BuildableBitmapTextureAtlas gameTextureAtlas;

    @Override
    public EngineOptions onCreateEngineOptions() {

    	camera = new Camera(0, 0, m_CameraWidth, m_CameraHeight);
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
    public LimitedFPSEngine onCreateEngine(final EngineOptions pEngineOptions) {
        return new LimitedFPSEngine(pEngineOptions, 60) {
            private boolean mRenderTextureInitialized;
            
            private RenderTexture mRenderTexture;
            private UncoloredSprite mRenderTextureSprite;
           
            @SuppressLint("WrongCall")
			@Override
            public void onDrawFrame(GLState pGLState)
                            throws InterruptedException {
                   
                    if (mShockwaveTime > 0f && mShockwaveTime < 10f) {
                           
                            if (!mRenderTextureInitialized) {
                                    initRenderTexture(pGLState);
                                    mRenderTextureInitialized = true;
                            }
                           
                            mRenderTexture.begin(pGLState, false, true, Color.TRANSPARENT);
                            {              
                                    super.onDrawFrame(pGLState);
                            }
                            mRenderTexture.end(pGLState);
                                                           
                            pGLState.pushProjectionGLMatrix();
                            pGLState.orthoProjectionGLMatrixf(0, m_CameraWidth, m_CameraHeight, 0, -1, 1);
                            {
                                    mRenderTextureSprite.onDraw(pGLState, camera);
                            }
                            pGLState.popProjectionGLMatrix();      
                    } else {
                            super.onDrawFrame(pGLState);
                    }
            }
           
            private void initRenderTexture(GLState pGLState) {
                    mRenderTexture = new RenderTexture(getTextureManager(), m_CameraWidth, m_CameraHeight, PixelFormat.RGBA_4444);
                    mRenderTexture.init(pGLState);
                    mRenderTextureSprite = new UncoloredSprite(m_CameraWidth/2, m_CameraHeight/2, TextureRegionFactory.extractFromTexture(mRenderTexture), getVertexBufferObjectManager()) {
                            @Override
                            protected void preDraw(GLState pGLState, Camera pCamera) {
                                    super.preDraw(pGLState, pCamera);
                                    if (mShockwaveTime > 0f && mShockwaveTime < 10f) GLES20.glUniform1f(ShockwaveShaderProgram.sUniformTimeLocation, mShockwaveTime);
                            }
                    };
                    mRenderTextureSprite.setShaderProgram(ShockwaveShaderProgram.getInstance());                           
            }
        };        	

    };
	
    
    @Override
    public void onCreateResources( OnCreateResourcesCallback pOnCreateResourcesCallback)
    			throws IOException {
    	vbom = getVertexBufferObjectManager();
    	m_PhysicsWorld = new FixedStepPhysicsWorld(60, new Vector2(0,-17), true);
    	
    	BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
    	gameTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 1024, 1024, TextureOptions.NEAREST);

        m_Head = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "head.png");
        m_Torso1 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "torso1.png");
        m_Torso2 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "torso2.png");
        m_Torso3 = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "torso3.png");
        m_UpperArmLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "upper_arm_left.png");
        m_UpperArmRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "upper_arm_right.png");
        m_LowerArmLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "lower_arm_left.png");
        m_LowerArmRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "lower_arm_right.png");
        m_UpperLegLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "upper_leg_left.png");
        m_UpperLegRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "upper_leg_right.png");
        m_LowerLegLeft = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "lower_leg_left.png");
        m_LowerLegRight = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, this, "lower_leg_right.png");

        try 
    	{
    	    this.gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
    	    this.gameTextureAtlas.load();
    	} 
    	catch (final TextureAtlasBuilderException e)
    	{
    	        Debug.e(e);
    	}
        this.getShaderProgramManager().loadShaderProgram(ShockwaveShaderProgram.getInstance());
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    	@Override
  	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
   			throws IOException {
   		Scene scene = new Scene();
            
        scene.setOnSceneTouchListener(this);
        scene.setOnAreaTouchListener(this);

        scene.setBackground(new Background(0.3f, 0.3f, 0.3f));
        scene.setBackgroundEnabled(true);

        pOnCreateSceneCallback.onCreateSceneFinished(scene);

    }

    @Override
    public void onPopulateScene(final Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback)
    	throws IOException {
    		 	
    	mGroundBody = this.m_PhysicsWorld.createBody(new BodyDef());
             
    	final Rectangle ground = new Rectangle(m_CameraWidth / 2, 1, m_CameraWidth, 2, vbom);
		final Rectangle roof = new Rectangle(m_CameraWidth / 2, m_CameraHeight - 1, m_CameraWidth, 2, vbom);
		final Rectangle left = new Rectangle(1, m_CameraHeight / 2, 1, m_CameraHeight, vbom);
		final Rectangle right = new Rectangle(m_CameraWidth - 1, m_CameraHeight / 2, 2, m_CameraHeight, vbom);

		ground.setColor(Color.BLACK);
		roof.setColor(Color.BLACK);
		left.setColor(Color.BLACK);
		right.setColor(Color.BLACK);
		
    	final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(1.0f, 0.2f, 0.1f);

    	PhysicsFactory.createBoxBody(m_PhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
    	PhysicsFactory.createBoxBody(m_PhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
    	PhysicsFactory.createBoxBody(m_PhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
    	PhysicsFactory.createBoxBody(m_PhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

    	createRagdoll();

    	pScene.registerTouchArea(m_HeadSprite);
    	pScene.registerTouchArea(m_Torso1Sprite);
    	pScene.registerTouchArea(m_Torso1Sprite);
    	pScene.registerTouchArea(m_Torso3Sprite);
    	pScene.registerTouchArea(m_UpperArmLeftSprite);
    	pScene.registerTouchArea(m_UpperArmRightSprite);         
    	pScene.registerTouchArea(m_LowerArmLeftSprite);
    	pScene.registerTouchArea(m_LowerArmRightSprite);         
    	pScene.registerTouchArea(m_UpperLegLeftSprite);
    	pScene.registerTouchArea(m_UpperLegRightSprite);         
    	pScene.registerTouchArea(m_LowerLegLeftSprite);
    	pScene.registerTouchArea(m_LowerLegRightSprite);
    	            
    	pScene.attachChild(m_HeadSprite);
    	pScene.attachChild(m_Torso1Sprite);
    	pScene.attachChild(m_Torso2Sprite);
    	pScene.attachChild(m_Torso3Sprite);
    	pScene.attachChild(m_UpperArmLeftSprite);
    	pScene.attachChild(m_UpperArmRightSprite);
    	pScene.attachChild(m_LowerArmLeftSprite);
    	pScene.attachChild(m_LowerArmRightSprite);
    	pScene.attachChild(m_UpperLegLeftSprite);
    	pScene.attachChild(m_UpperLegRightSprite);
    	pScene.attachChild(m_LowerLegLeftSprite);
    	pScene.attachChild(m_LowerLegRightSprite);
    	
    	pScene.attachChild(ground);
    	pScene.attachChild(left);
    	pScene.attachChild(right);

    	// m_DebugTextureSprite = new Sprite(0, 0, m_DebugTexture);
    	// scene.getChild(0).attachChild(m_DebugTextureSprite);

    	//pScene.registerUpdateHandler(this.m_PhysicsWorld);
    	
    	pScene.registerUpdateHandler(new TimerHandler(0.08f, true, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                    mShockwaveTime += 0.02f;
                    if (mShockwaveTime > 1.2f) {
                            pScene.unregisterUpdateHandler(pTimerHandler);
                            mShockwaveTime = 0.0f;
                    }
            }
    	}));
    	
    	pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    private Vector2 getLeftPoint(Sprite sprite, Body body) {
         float w = sprite.getWidth() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
         float h = sprite.getHeight() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
         Vector2 pnt = body.getPosition();
         pnt.x -= w;
         pnt.y -= h;
         return pnt;
    }

    private Vector2 getRightPoint(Sprite sprite, Body body) {
    	float w = sprite.getWidth() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
    	float h = sprite.getHeight() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
    	Vector2 pnt = body.getPosition();
    	pnt.x += w;
    	pnt.y += h;
    	return pnt;
    }

    private Vector2 getTopPoint(Sprite sprite, Body body) {
    	float h = sprite.getHeight() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
    	Vector2 pnt = body.getPosition();
    	pnt.y -= h;
    	return pnt;
    }

    private Vector2 getBottomPoint(Sprite sprite, Body body) {
    	float h = sprite.getHeight() * 0.5f / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT;
    	Vector2 pnt = body.getPosition();
    	pnt.y += h;
    	return pnt;
    }

    private void createRagdoll() {

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
    	
    		//for head only
	    	fixtureDef.density = 1.0f;
	    	fixtureDef.friction = 0.1f;
	    	fixtureDef.restitution = 0.2f;
    	
    	// Head
    	m_HeadSprite.setPosition(startX, startY);
    	Body head = PhysicsFactory.createCircleBody(m_PhysicsWorld, m_HeadSprite, BodyType.DynamicBody, fixtureDef);                
    	m_HeadSprite.setUserData(head);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_HeadSprite, head));

	    	//for rest of body
	    	fixtureDef.density = 1.0f;
	    	fixtureDef.friction = 0.0f;
	    	fixtureDef.restitution = 1.0f;
    	
    	// Torso1    	
    	m_Torso1Sprite.setPosition(startX, startY + m_HeadSprite.getHeight());
    	Body torso1 = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_Torso1Sprite, BodyType.DynamicBody, fixtureDef);
    	m_Torso1Sprite.setUserData(torso1);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_Torso1Sprite, torso1));

    	// Torso2
    	m_Torso2Sprite.setPosition(startX, startY + 98);
    	Body torso2 = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_Torso2Sprite, BodyType.DynamicBody, fixtureDef);
    	m_Torso2Sprite.setUserData(torso2);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector( m_Torso2Sprite, torso2));

    	// Torso3
    	m_Torso3Sprite.setPosition(startX, startY + 133);
    	Body torso3 = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_Torso3Sprite, BodyType.DynamicBody, fixtureDef);
    	m_Torso3Sprite.setUserData(torso3);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_Torso3Sprite, torso3));

    	// UpperArm

    	// L
    	m_UpperArmLeftSprite.setPosition(startX - (m_Torso1Sprite.getWidth()/2 + m_UpperArmLeftSprite.getWidth()/2),
    									startY + m_HeadSprite.getHeight());
    	Body upperArmL = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_UpperArmLeftSprite, BodyType.DynamicBody, fixtureDef);
    	m_UpperArmLeftSprite.setUserData(upperArmL);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_UpperArmLeftSprite, upperArmL));

    	// R
    	m_UpperArmRightSprite.setPosition(startX + m_Torso1Sprite.getWidth()/2 + m_UpperArmLeftSprite.getWidth()/2,
    									startY + m_HeadSprite.getHeight());
    	Body upperArmR = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_UpperArmRightSprite, BodyType.DynamicBody, fixtureDef);
    	m_UpperArmRightSprite.setUserData(upperArmR);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_UpperArmRightSprite, upperArmR));

    	// LowerArm

    	// L
    	m_LowerArmLeftSprite.setPosition(startX - 173.75f - 5f, startY + 62.5f);
    	Body lowerArmL = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_LowerArmLeftSprite, BodyType.DynamicBody, fixtureDef);
    	m_LowerArmLeftSprite.setUserData(lowerArmL);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_LowerArmLeftSprite, lowerArmL));

    	// R
    	m_LowerArmRightSprite.setPosition(startX + 146.25f + 5f, startY + 62.5f);
    	Body lowerArmR = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_LowerArmRightSprite, BodyType.DynamicBody, fixtureDef);
    	m_LowerArmRightSprite.setUserData(lowerArmR);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_LowerArmRightSprite, lowerArmR));

    	// UpperLeg

    	// L
    	m_UpperLegLeftSprite.setPosition(startX - m_UpperLegLeftSprite.getWidth()/2, startY + 212.5f);
    	Body upperLegL = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_UpperLegLeftSprite, BodyType.DynamicBody, fixtureDef);
    	m_UpperLegLeftSprite.setUserData(upperLegL);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_UpperLegLeftSprite, upperLegL));

    	// R
    	m_UpperLegRightSprite.setPosition(startX + m_UpperLegRightSprite.getWidth()/2, startY + 212.5f);
    	Body upperLegR = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_UpperLegRightSprite, BodyType.DynamicBody, fixtureDef);
    	m_UpperLegRightSprite.setUserData(upperLegR);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector( m_UpperLegRightSprite, upperLegR));

    	// LowerLeg

    	// L
    	m_LowerLegLeftSprite.setPosition(startX - m_UpperLegLeftSprite.getWidth()/2, startY + 310);
    	Body lowerLegL = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_LowerLegLeftSprite, BodyType.DynamicBody, fixtureDef);
    	m_LowerLegLeftSprite.setUserData(lowerLegL);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_LowerLegLeftSprite, lowerLegL));

    	// R
    	m_LowerLegRightSprite.setPosition(startX + m_UpperLegRightSprite.getWidth()/2, startY + 310);
    	Body lowerLegR = PhysicsFactory.createBoxBody(m_PhysicsWorld, m_LowerLegRightSprite, BodyType.DynamicBody, fixtureDef);
    	m_LowerLegRightSprite.setUserData(lowerLegR);
    	m_PhysicsWorld.registerPhysicsConnector(new PhysicsConnector(m_LowerLegRightSprite, lowerLegR));

    	// JOINTS
    	jd.enableLimit = true;

    	// Head to shoulders
    	jd.lowerAngle = (float) (-40 / (180 / Math.PI));
    	jd.upperAngle = (float) (40 / (180 / Math.PI));
    	jd.initialize(torso1, head, getTopPoint(m_Torso1Sprite, torso1));
    	m_PhysicsWorld.createJoint(jd);

    	// Upper arm to shoulders
    	// L
    	jd.lowerAngle = (float) (-85 / (180 / Math.PI));
    	jd.upperAngle = (float) (130 / (180 / Math.PI));
    	jd.initialize(torso1, upperArmL, getRightPoint(m_UpperArmLeftSprite, upperArmL));
    	m_PhysicsWorld.createJoint(jd);

    	// R
    	jd.lowerAngle = (float) (-130 / (180 / Math.PI));
    	jd.upperAngle = (float) (85 / (180 / Math.PI));
    	jd.initialize(torso1, upperArmR, getLeftPoint(m_UpperArmRightSprite, upperArmR));
    	m_PhysicsWorld.createJoint(jd);

    	// Lower arm to upper arm
    	// L
    	jd.lowerAngle = (float) (-130 / (180 / Math.PI));
    	jd.upperAngle = (float) (10 / (180 / Math.PI));
    	jd.initialize(upperArmL, lowerArmL, getLeftPoint(m_UpperArmLeftSprite, upperArmL));
    	m_PhysicsWorld.createJoint(jd);
    	// R
    	jd.lowerAngle = (float) (-10 / (180 / Math.PI));
    	jd.upperAngle = (float) (130 / (180 / Math.PI));
    	jd.initialize(upperArmR, lowerArmR, getRightPoint(m_UpperArmRightSprite, upperArmR));
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
    	jd.initialize(torso3, upperLegL, getBottomPoint(m_Torso3Sprite, torso3));
    	m_PhysicsWorld.createJoint(jd);
    	// R
    	jd.lowerAngle = (float) (-45 / (180 / Math.PI));
    	jd.upperAngle = (float) (25 / (180 / Math.PI));
    	jd.initialize(torso3, upperLegR, getBottomPoint(m_Torso3Sprite, torso3));
    	m_PhysicsWorld.createJoint(jd);

    	// Upper leg to lower leg
    	// L
    	jd.lowerAngle = (float) (-25 / (180 / Math.PI));
    	jd.upperAngle = (float) (75 / (180 / Math.PI));
    	jd.initialize(upperLegL, lowerLegL, getBottomPoint(m_UpperLegLeftSprite, upperLegL));
    	m_PhysicsWorld.createJoint(jd);
    	// R
    	jd.lowerAngle = (float) (-75 / (180 / Math.PI));
    	jd.upperAngle = (float) (25 / (180 / Math.PI));
    	jd.initialize(upperLegR, lowerLegR, getBottomPoint(m_UpperLegLeftSprite, upperLegR));
    	m_PhysicsWorld.createJoint(jd);
    }

    private MouseJoint mMouseJointActive;
    private Body mGroundBody;
     
    @Override
    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, ITouchArea pTouchArea, float pTouchAreaLocalX,
    		float pTouchAreaLocalY) {
    	if(pSceneTouchEvent.isActionDown()) {
    		final IShape face = (IShape) pTouchArea;
    		 // If we have an active MouseJoint, we are just moving it around
    		 // instead of creating a second one.
    		if (this.mMouseJointActive == null) {
    			//this.mEngine.vibrate(100);
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
    
    public static class ShockwaveShaderProgram extends ShaderProgram {
    	
    	private static ShockwaveShaderProgram instance;
    	
    	public static ShockwaveShaderProgram getInstance() {
    		if (instance == null) instance = new ShockwaveShaderProgram();
    		return instance;
    	}
    			
    	public static final String FRAGMENTSHADER = 
    	"precision lowp float;\n" +

    	"uniform lowp sampler2D " + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ";\n" +
    	"varying mediump vec2 " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n" +
    	
    	"uniform vec2 center;\n" +
    	"uniform float time;\n" +
    	"const vec3 params = vec3(10.0, 0.8, 0.02);\n" +

    	"void main()	\n" +
    	"{				\n" +
    	"	mediump vec2 texCoord = " + ShaderProgramConstants.VARYING_TEXTURECOORDINATES + ";\n" +
    	"	float distance = distance(texCoord, center);\n" +
    	"	if ( (distance <= (time + params.z)) && (distance >= (time - params.z)) )\n" +
    	"	{\n" +		
    	"		float diff = (distance - time);\n" +
    	"		float powDiff = 1.0 - pow(abs(diff*params.x), params.y);\n" +
    	"		float diffTime = diff  * powDiff;\n" +
    	"		vec2 diffUV = texCoord - center;\n" +
    //	"		texCoord = texCoord + (diffUV * diffTime);\n" +
    	"	}\n" +
    	"	gl_FragColor = texture2D(" + ShaderProgramConstants.UNIFORM_TEXTURE_0 + ", texCoord);\n" +
    	"}		\n";

    	 
    	private ShockwaveShaderProgram() {
    		super(PositionTextureCoordinatesShaderProgram.VERTEXSHADER, FRAGMENTSHADER);
    	}
    	
    	public static int sUniformModelViewPositionMatrixLocation = ShaderProgramConstants.LOCATION_INVALID;
    	public static int sUniformTexture0Location = ShaderProgramConstants.LOCATION_INVALID;
    	public static int sUniformCenterLocation = ShaderProgramConstants.LOCATION_INVALID;
    	public static int sUniformTimeLocation = ShaderProgramConstants.LOCATION_INVALID;
    	
    	@Override
    	protected void link(final GLState pGLState) throws ShaderProgramLinkException {
    		GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_POSITION_LOCATION, ShaderProgramConstants.ATTRIBUTE_POSITION);
    		GLES20.glBindAttribLocation(this.mProgramID, ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES_LOCATION, ShaderProgramConstants.ATTRIBUTE_TEXTURECOORDINATES);

    		super.link(pGLState);

    		ShockwaveShaderProgram.sUniformModelViewPositionMatrixLocation = this.getUniformLocation(ShaderProgramConstants.UNIFORM_MODELVIEWPROJECTIONMATRIX);
    		ShockwaveShaderProgram.sUniformTexture0Location = this.getUniformLocation(ShaderProgramConstants.UNIFORM_TEXTURE_0);
    		ShockwaveShaderProgram.sUniformCenterLocation = this.getUniformLocation("center");
    		ShockwaveShaderProgram.sUniformTimeLocation = this.getUniformLocation("time");
    	}
    	
    	@Override
    	public void bind(final GLState pGLState, final VertexBufferObjectAttributes pVertexBufferObjectAttributes) {
    		GLES20.glDisableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);
    		super.bind(pGLState, pVertexBufferObjectAttributes);
    		GLES20.glUniformMatrix4fv(ShockwaveShaderProgram.sUniformModelViewPositionMatrixLocation, 1, false, pGLState.getModelViewProjectionGLMatrix(), 0);
    		GLES20.glUniform1i(ShockwaveShaderProgram.sUniformTexture0Location, 0);
    		GLES20.glUniform2f(ShockwaveShaderProgram.sUniformCenterLocation, -1, -1);
    	}

      
    	@Override
    	public void unbind(final GLState pGLState) throws ShaderProgramException {
    		GLES20.glEnableVertexAttribArray(ShaderProgramConstants.ATTRIBUTE_COLOR_LOCATION);
    		super.unbind(pGLState);
    	}
    }

}