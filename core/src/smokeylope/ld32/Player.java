package smokeylope.ld32;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

public class Player extends Character {
	private Character controlledCharacter;
	private float controlTimer;

	public Player() {
		speed = 256;
		controlledCharacter = this;
		beingControlled = true;
		controlTimer = 0.0f;

		size = new Vector2(32, 32);
		texture = new Texture(Gdx.files.internal("assets/player.png"));
		sprite = new Sprite(texture, 0, 0, 8, 8);
	}

	public void control(Character character) {
		if (controlTimer > 0.1f) {
			if (character == this) {
				requestSound("controlend");
			} else {
				requestSound("control");
			}

			controlledCharacter.setBeingControlled(false);
			controlledCharacter = character;
			controlledCharacter.setBeingControlled(true);

			controlTimer = 0.0f;

			if (controlledCharacter instanceof Enemy) {
				((Enemy) controlledCharacter).setShotTimer(1.0f);
			}
		}
	}

	public Character getControlledCharacter() {
		return controlledCharacter;
	}

	@Override
	public void tick(float delta, List<Entity> entities) {
		if (alive) {
			playAnimation(delta);

			controlTimer += delta;

			if (controlledCharacter != this && (controlTimer > 5.0f || !controlledCharacter.isAlive())) {
				if (controlledCharacter instanceof Enemy) {
					Enemy enemy = (Enemy) controlledCharacter;
					enemy.setShotTimer(0.5f);

					if (enemy.getSuicideTimer() == 0.0f) {
						control(this);
					}
				} else {
					control(this);
				}
			}

			Vector2 direction = new Vector2();

			if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
				Vector2 newPosition = new Vector2(controlledCharacter.getPosition());
				newPosition.x += controlledCharacter.getSpeed() * delta;

				Cell cellTop = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
				Cell cellBottom = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

				if (cellTop == null && cellBottom == null) {
					direction.x += 1.0f;
				}

				controlledCharacter.setAnimationPlaying(true);
			}

			if (Gdx.input.isKeyPressed(Keys.LEFT)) {
				Vector2 newPosition = new Vector2(controlledCharacter.getPosition());
				newPosition.x -= controlledCharacter.getSpeed() * delta;

				Cell cellTop = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
				Cell cellBottom = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

				if (cellTop == null && cellBottom == null) {
					direction.x -= 1.0f;
				}

				controlledCharacter.setAnimationPlaying(true);
			}

			if (Gdx.input.isKeyPressed(Keys.UP)) {
				Vector2 newPosition = new Vector2(controlledCharacter.getPosition());
				newPosition.y += controlledCharacter.getSpeed() * delta;

				Cell cellRight = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
				Cell cellLeft = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));

				if (cellRight == null && cellLeft == null) {
					direction.y += 1.0f;
				}

				controlledCharacter.setAnimationPlaying(true);
			}

			if (Gdx.input.isKeyPressed(Keys.DOWN)) {
				Vector2 newPosition = new Vector2(controlledCharacter.getPosition());
				newPosition.y -= controlledCharacter.getSpeed() * delta;

				Cell cellRight = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));
				Cell cellLeft = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

				if (cellRight == null && cellLeft == null) {
					direction.y -= 1.0f;
				}

				controlledCharacter.setAnimationPlaying(true);
			}

			if (Gdx.input.isKeyPressed(Keys.SPACE)) {
				controlledCharacter.action(delta, entities);
			}

			direction.nor();
			controlledCharacter.setSpriteDirection(direction);
			controlledCharacter.getPosition().mulAdd(direction, controlledCharacter.getSpeed() * delta);
		}
	}

	@Override
	public void action(float delta, List<Entity> entities) {
		for (Entity entity : entities) {
			if (entity instanceof Character && entity.getPosition().dst2(position) < 4096.0f &&
					entity.isAlive() && Gdx.input.isKeyJustPressed(Keys.SPACE)) {
				control((Character) entity);
			}
		}
	}

	@Override
	public void kill(Vector2 bulletDirection, List<Entity> entities) {
		if (bulletDirection.x < 0.0f) {
			sprite = new Sprite(texture, 0, 8, 16, 8);
			size.set(64, 32);
		} else if (bulletDirection.x > 0.0f) {
			sprite = new Sprite(texture, 16, 8, 16, 8);
			size.set(64, 32);
		}

		if (bulletDirection.y > 0.5f) {
			sprite = new Sprite(texture, 0, 16, 8, 16);
			size.set(32, 64);
		} else if (bulletDirection.y < -0.5f) {
			sprite = new Sprite(texture, 8, 16, 8, 16);
			size.set(32, 64);
		}

		alive = false;

		requestSound("hit");
	}
}
