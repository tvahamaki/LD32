package smokeylope.ld32;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

public class Entity {
	protected Vector2 position;
	protected Vector2 offset;
	protected Vector2 size;
	protected Texture texture;
	protected Sprite sprite;
	protected float alpha;
	protected TiledMapTileLayer collisionLayer;
	protected boolean alive;
	protected List<String> requestedSounds;

	public Entity() {
		position = new Vector2();
		offset = new Vector2();
		alpha = 1.0f;
		alive = true;
		requestedSounds = new ArrayList<String>();
	}

	public Vector2 getPosition() {
		return position;
	}

	public Vector2 getOffset() {
		return offset;
	}

	public Vector2 getSize() {
		return size;
	}

	public Texture getTexture() {
		return texture;
	}

	public Sprite getSprite() {
		return sprite;
	}

	public float getAlpha() {
		return alpha;
	}

	public boolean isAlive() {
		return alive;
	}

	public List<String> getRequestedSounds() {
		return requestedSounds;
	}

	public void setPosition(Vector2 position) {
		this.position = position;
	}

	public void setPosition(float x, float y) {
		position.set(x, y);
	}

	public void setCollisionLayer(TiledMapTileLayer collisionLayer) {
		this.collisionLayer = collisionLayer;
	}

	public void tick(float delta, List<Entity> entities) {

	}

	public void kill(Vector2 bulletDirection, List<Entity> entities) {

	}

	public void requestSound(String sound) {
		requestedSounds.add(sound);
	}

	public void clearSounds() {
		requestedSounds.clear();
	}
}
