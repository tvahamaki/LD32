package smokeylope.ld32;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;

public class Bullet extends Entity {
	private final float speed;
	private Entity shooter;
	private Vector2 direction;

	public Bullet(Entity shooter) {
		speed = 1024.0f;
		this.shooter = shooter;

		size = new Vector2(4, 4);
		texture = new Texture(Gdx.files.internal("assets/bullet.png"));
		sprite = new Sprite(texture, 0, 0, 1, 1);
	}

	public void setDirection(Vector2 direction) {
		this.direction = direction;
	}

	@Override
	public void tick(float delta, List<Entity> entities) {
		//Vector2 currentPosition = new Vector2(position);
		position.mulAdd(direction, speed * delta);

		/*while (!currentPosition.epsilonEquals(position, 0.2f)) {
			BulletTrail trail = new BulletTrail();
			trail.setPosition(currentPosition);
			entities.add(trail);
			currentPosition.add(direction);
		}*/

		Entity hit = null;

		for (Entity entity : entities) {
			if (entity != this && entity != shooter && entity.isAlive() &&
					position.x > entity.getPosition().x &&
					position.x < entity.getPosition().x + entity.getSize().x &&
					position.y > entity.getPosition().y &&
					position.y < entity.getPosition().y + entity.getSize().y) {
				hit = entity;
				break;
			}
		}

		if (hit != null) {
			hit.kill(direction, entities);
			entities.remove(this);
		} else {
			Vector2 nextPosition = new Vector2(new Vector2(position).mulAdd(direction, speed * delta));

			if (collisionLayer.getCell((int) (nextPosition.x / 32.0f), (int) (nextPosition.y / 32.0f)) != null) {
				entities.remove(this);
			}
		}
	}
}
