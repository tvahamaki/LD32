package smokeylope.ld32;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;

public class Game extends ApplicationAdapter {
	private SpriteBatch batch;
	private Player player;
	private Enemy enemy;
	private List<Entity> entities;
	private TiledMap map;
	private OrthographicCamera camera;
	private OrthogonalTiledMapRenderer renderer;
	private BitmapFont font;
	private float mapChangeTimer;
	private Map<String, Sound> sounds;
	private Music music;
	private int mapCounter;
	private boolean gameOver;

	public static int enemiesLeft;
	public static int tip;

	@Override
	public void create () {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 640, 480);

		font = new BitmapFont(Gdx.files.internal("assets/Andina.fnt"), false);

		entities = new ArrayList<Entity>();
		player = new Player();
		mapCounter = 1;
		gameOver = false;
		loadNextMap();

		mapChangeTimer = 0.0f;

		sounds = new HashMap<String, Sound>();
		sounds.put("footstep", Gdx.audio.newSound(Gdx.files.internal("assets/step.wav")));
		sounds.put("gunshot", Gdx.audio.newSound(Gdx.files.internal("assets/gunshot.wav")));
		sounds.put("hit", Gdx.audio.newSound(Gdx.files.internal("assets/hit.wav")));
		sounds.put("control", Gdx.audio.newSound(Gdx.files.internal("assets/control.wav")));
		sounds.put("controlend", Gdx.audio.newSound(Gdx.files.internal("assets/controlend.wav")));

		music = Gdx.audio.newMusic(Gdx.files.internal("assets/music.wav"));
		music.setLooping(true);
		music.setVolume(0.5f);
		music.play();
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.1f, 0.1f, 0.15f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (!gameOver) {
			camera.position.x = (int) player.getControlledCharacter().getPosition().x;
			camera.position.y = (int) player.getControlledCharacter().getPosition().y;
			camera.update();

			renderer.setView(camera);

			renderer.getSpriteBatch().begin();
			renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("Background"));
			renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("Walls"));
			renderer.getSpriteBatch().end();

			float delta = Gdx.graphics.getDeltaTime();

			List<Entity> entitiesCopy = new ArrayList<Entity>(entities);
			batch.begin();

			for (Entity entity : entities) {
				if (!entity.isAlive()) {
					Vector2 cameraPosition = new Vector2(camera.position.x - 320, camera.position.y - 240);
					Vector2 entityPosition = new Vector2(new Vector2(entity.getPosition()).sub(cameraPosition));

					batch.setColor(1.0f, 1.0f, 1.0f, entity.getAlpha());
					batch.draw(entity.getSprite(), entityPosition.x + entity.getOffset().x,
							entityPosition.y + entity.getOffset().y, entity.getSize().x, entity.getSize().y);
				}
			}

			for (Entity entity : entities) {
				entity.tick(delta, entitiesCopy);

				Vector2 cameraPosition = new Vector2(camera.position.x - 320, camera.position.y - 240);
				Vector2 entityPosition = new Vector2(new Vector2(entity.getPosition()).sub(cameraPosition));

				if (entity.isAlive()) {
					batch.setColor(1.0f, 1.0f, 1.0f, entity.getAlpha());
					batch.draw(entity.getSprite(), entityPosition.x + entity.getOffset().x,
							entityPosition.y + entity.getOffset().y, entity.getSize().x, entity.getSize().y);
				}

				Vector2 controlledCharacterPosition = new Vector2(new Vector2(player.getControlledCharacter().getPosition()).sub(cameraPosition));

				for (String soundName : entity.getRequestedSounds()) {
					if (sounds.containsKey(soundName)) {
						long id = sounds.get(soundName).play();
						sounds.get(soundName).setPan(id, (entityPosition.x / 640.0f) * 2 - 1, Math.max(0.0f,
								1.0f - entityPosition.dst2(controlledCharacterPosition) / 262144.0f));
					}
				}

				entity.clearSounds();
			}

			batch.end();
			entities = new ArrayList<Entity>(entitiesCopy);

			renderer.getSpriteBatch().begin();
			renderer.renderTileLayer((TiledMapTileLayer) map.getLayers().get("Foreground"));
			renderer.getSpriteBatch().end();

			batch.begin();
			font.drawMultiLine(batch, "ENEMIES LEFT: " + enemiesLeft, 16.0f, 464, 0.0f, BitmapFont.HAlignment.LEFT);
			batch.end();

			if (enemiesLeft == 0.0f || !player.isAlive()) {
				mapChangeTimer += delta;
			}

			if (mapChangeTimer > 2.0f) {
				if (!player.isAlive()) {
					mapCounter--;
				}

				loadNextMap();
				mapChangeTimer = 0.0f;
			}
		} else {
			batch.begin();
			font.drawMultiLine(batch, "THANK YOU FOR PLAYING\n\nPRESS SPACE TO RESTART", 320.0f, 272, 0.0f, BitmapFont.HAlignment.CENTER);
			batch.end();

			if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
				gameOver = false;
				mapCounter = 1;
				loadNextMap();
			}
		}
	}

	private void loadNextMap() {
		if (mapCounter <= 5) {
			loadMap("assets/level" + mapCounter + ".tmx");
			mapCounter++;
		} else {
			gameOver = true;
			entities.clear();
		}
	}

	private void loadMap(String path) {
		player = new Player();
		player.control(player);
		enemiesLeft = 0;
		entities.clear();
		map = new TmxMapLoader().load(path);
		renderer = new OrthogonalTiledMapRenderer(map, 4.0f);
		spawnEntities();
		player.setCollisionLayer((TiledMapTileLayer) map.getLayers().get("Walls"));
		entities.add(player);
	}

	private void spawnEntities() {
		if (map.getLayers().get("Entities") != null) {
			for (MapObject entity : map.getLayers().get("Entities").getObjects()) {
				String type = entity.getProperties().get("type", "", String.class);
				int direction = Integer.parseInt(entity.getProperties().get("direction", "", String.class));
				float x = entity.getProperties().get("x", Float.class);
				float y = entity.getProperties().get("y", Float.class);

				if (type.equals("player")) {
					player.position.set(x * 4.0f, y * 4.0f);

					if (direction == 0) {
						player.setSpriteDirection(new Vector2(1.0f, 0.0f));
					} else if (direction == 1) {
						player.setSpriteDirection(new Vector2(-1.0f, 0.0f));
					} else if (direction == 2) {
						player.setSpriteDirection(new Vector2(1.0f, 1.0f));
					} else if (direction == 3) {
						player.setSpriteDirection(new Vector2(0.0f, -1.0f));
					}
				} else if (type.equals("enemy")) {
					Enemy enemy = new Enemy();
					enemy.setPosition(x * 4.0f, y * 4.0f);
					enemy.setCollisionLayer((TiledMapTileLayer) map.getLayers().get("Walls"));
					enemy.addTarget(player);
					entities.add(enemy);

					if (direction == 0) {
						enemy.setSpriteDirection(new Vector2(1.0f, 0.0f));
					} else if (direction == 1) {
						enemy.setSpriteDirection(new Vector2(-1.0f, 0.0f));
					} else if (direction == 2) {
						enemy.setSpriteDirection(new Vector2(0.0f, 1.0f));
					} else if (direction == 3) {
						enemy.setSpriteDirection(new Vector2(0.0f, -1.0f));
					}

					enemiesLeft++;
				}
			}
		}
	}
}
