package smokeylope.ld32;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;


public class Character extends Entity {
	protected float speed;
	protected boolean animationPlaying;
	protected float animationProgress;
	protected Sprite[] sprites;
	protected boolean beingControlled;
	protected Vector2 lastDirection;

	public Character() {
		animationPlaying = false;
		beingControlled = false;
		animationProgress = 0;
		lastDirection = new Vector2(1.0f, 0.0f);
	}

	public float getSpeed() {
		return speed;
	}

	public void setAnimationPlaying(boolean playing) {
		animationPlaying = playing;
	}

	public void setSpriteDirection(Vector2 direction) {
		if (direction.x > 0) {
			sprite = new Sprite(texture, 0, 0, 8, 8);
		} else if (direction.x < 0) {
			sprite = new Sprite(texture, 8, 0, 8, 8);
		}

		if (direction.y > 0.5) {
			sprite = new Sprite(texture, 16, 0, 8, 8);
		} else if (direction.y < -0.5) {
			sprite = new Sprite(texture, 24, 0, 8, 8);
		}

		if (direction.len2() != 0) {
			lastDirection = new Vector2(direction);
		}
	}

	public void setBeingControlled(boolean controlled) {
		beingControlled = controlled;
	}

	public void playAnimation(float delta) {
		if (animationPlaying) {
			if (animationProgress == 0.0f) {
				requestSound("footstep");
			}

			if (animationProgress < 0.1f) {
				animationProgress += delta;
				offset.y += 64 * delta;
			} else if (animationProgress < 0.2f) {
				animationProgress += delta;

				if (offset.y - 64 * delta < 0.0f) {
					offset.y = 0.0f;
				} else {
					offset.y -= 64 * delta;
				}
			} else {
				animationProgress = 0;
				offset.y = 0;
				animationPlaying = false;
			}
		}
	}

	public void action(float delta, List<Entity> entities) {

	}
}
