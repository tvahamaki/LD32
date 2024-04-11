package smokeylope.ld32;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Vector2;

public class Enemy extends Character {
	private List<Entity> targets;
	private Entity currentTarget;
	private float shotTimer;
	private float patrolTimer;
	private Vector2 patrolDirection;
	private float alertedTimer;
	private boolean alerted;
	private float suicideTimer;

	public Enemy() {
		targets = new ArrayList<Entity>();
		speed = 128.0f;
		shotTimer = 1.0f;
		patrolTimer = 0.0f;
		patrolDirection = new Vector2();
		alertedTimer = 0.0f;
		suicideTimer = 0.0f;

		size = new Vector2(32, 32);
		texture = new Texture(Gdx.files.internal("assets/enemy.png"));
		sprite = new Sprite(texture, 0, 0, 8, 8);
	}

	public boolean isAlerted() {
		return alerted;
	}

	public Vector2 getLastDirection() {
		return lastDirection;
	}

	public float getSuicideTimer() {
		return suicideTimer;
	}

	public void addTarget(Entity target) {
		this.targets.add(target);
	}

	public void setShotTimer(float timer) {
		shotTimer = timer;
	}

	@Override
	public void tick(float delta, List<Entity> entities) {
		if (alive && suicideTimer == 0.0f) {
			playAnimation(delta);

			if (!beingControlled) {
				if (currentTarget != null && !currentTarget.isAlive()) {
					currentTarget = null;
				}

				for (Entity target : targets) {
					if (target.isAlive() && (currentTarget == target || currentTarget == null)) {
						boolean lineOfSight = true;
						Vector2 currentPosition = new Vector2(position.x + 16, position.y + 16);
						Vector2 targetDirection = new Vector2(target.getPosition()).sub(getPosition()).nor();

						if (alertedTimer == 0.0f &&
								(lastDirection.x > 0.0f && targetDirection.x < 0 ||
								lastDirection.x < 0.0f && targetDirection.x > 0 ||
								lastDirection.y > 0.5f && targetDirection.y < 0 ||
								lastDirection.y < -0.5f && targetDirection.y > 0)) {
							lineOfSight = false;
						}

						if (lineOfSight) {
							while (position.dst2(currentPosition) < position.dst2(target.getPosition())) {
								if (collisionLayer.getCell((int) (currentPosition.x / 32.0f), (int) (currentPosition.y / 32.0f)) != null) {
									lineOfSight = false;
									break;
								}

								currentPosition.add(targetDirection);
							}
						}

						if (lineOfSight) {
							if (targetDirection.x > 0) {
								sprite = new Sprite(texture, 0, 0, 8, 8);
							} else if (targetDirection.x < 0) {
								sprite = new Sprite(texture, 8, 0, 8, 8);
							}

							if (targetDirection.y > 0.5f) {
								sprite = new Sprite(texture, 16, 0, 8, 8);
							} else if (targetDirection.y < -0.5f) {
								sprite = new Sprite(texture, 24, 0, 8, 8);
							}

							alertedTimer = 10.0f;

							if (shotTimer == 0.0f && entities.contains(target)) {
								Bullet bullet = new Bullet(this);
								bullet.setCollisionLayer(collisionLayer);
								bullet.setPosition(getPosition().x + 16, getPosition().y + 16);
								bullet.setDirection(targetDirection);
								entities.add(bullet);
								shotTimer = 1.0f;
								requestSound("gunshot");
							}

							currentTarget = target;
							break;
						}
					}
				}

				if (shotTimer > 0) {
					shotTimer -= delta;
				} else {
					shotTimer = 0;
				}

				if (alertedTimer > 0.0f) {
					if (alertedTimer < 10.0f) {
						if (patrolTimer < 1.0f) {
							patrol(delta);
							patrolTimer += delta;
							animationPlaying = true;
						} else {
							Random random = new Random();
							patrolDirection = new Vector2(random.nextFloat() * 2.0f - 1.0f, random.nextFloat() * 2.0f - 1.0f).nor();
							patrolTimer = 0.0f;
						}
					}

					alerted = true;
					alertedTimer -= delta;
				} else {
					shotTimer = 0.5f;
					alertedTimer = 0.0f;
					alerted = false;
				}
			} else if (shotTimer > 0) {
				shotTimer -= delta;
			} else {
				shotTimer = 0;
			}
		} else if (alive && suicideTimer > 0.0f) {
			if (suicideTimer > 1.0f) {
				requestSound("gunshot");
				kill(new Vector2(0.0f, 1.0f), entities);
			}

			suicideTimer += delta;
		}
	}

	@Override
	public void action(float delta, List<Entity> entities) {
		if (shotTimer == 0.0f) {
			if (Game.enemiesLeft > 1) {
				Bullet bullet = new Bullet(this);
				bullet.setCollisionLayer(collisionLayer);
				bullet.setPosition(getPosition().x + 16, getPosition().y + 16);
				bullet.setDirection(lastDirection);
				entities.add(bullet);
				shotTimer = 1.0f;
				requestSound("gunshot");

				aggroOthers(entities);
			} else if (Game.enemiesLeft == 1) {
				sprite = new Sprite(texture, 16, 16, 8, 8);
				suicideTimer += delta;
			}
		}
	}

	private void patrol(float delta) {
		if (patrolDirection.x > 0.0f) {
			Vector2 newPosition = new Vector2(position);
			newPosition.x += speed * delta;

			Cell cellTop = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
			Cell cellBottom = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

			if (cellTop != null || cellBottom != null) {
				patrolDirection.x = -patrolDirection.x;
			}
		}

		if (patrolDirection.x < 0.0f) {
			Vector2 newPosition = new Vector2(position);
			newPosition.x -= speed * delta;

			Cell cellTop = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
			Cell cellBottom = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

			if (cellTop != null || cellBottom != null) {
				patrolDirection.x = -patrolDirection.x;
			}
		}

		if (patrolDirection.y > 0.0f) {
			Vector2 newPosition = new Vector2(position);
			newPosition.y += speed * delta;

			Cell cellRight = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));
			Cell cellLeft = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) Math.ceil(newPosition.y / 32.0f));

			if (cellRight != null || cellLeft != null) {
				patrolDirection.y = -patrolDirection.y;
			}
		}

		if (patrolDirection.y < 0.0f) {
			Vector2 newPosition = new Vector2(position);
			newPosition.y -= speed * delta;

			Cell cellRight = collisionLayer.getCell((int) Math.ceil(newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));
			Cell cellLeft = collisionLayer.getCell((int) (newPosition.x / 32.0f), (int) (newPosition.y / 32.0f));

			if (cellRight != null || cellLeft != null) {
				patrolDirection.y = -patrolDirection.y;
			}
		}

		setSpriteDirection(patrolDirection.nor());
		position.mulAdd(patrolDirection, speed * delta);
	}

	private void alert() {
		alertedTimer = 10.0f;
	}

	private void alertOthers(List<Entity> entities) {
		for (Entity entity : entities) {
			if (entity instanceof Enemy && entity != this) {
				Enemy enemy = (Enemy) entity;

				boolean lineOfSight = true;
				Vector2 currentPosition = new Vector2(position.x + 16, position.y + 16);
				Vector2 targetDirection = new Vector2(enemy.getPosition()).sub(getPosition()).nor();

				if (!enemy.isAlerted() &&
						(enemy.getLastDirection().x > 0 && targetDirection.x > 0 ||
						enemy.getLastDirection().x < 0 && targetDirection.x < 0 ||
						enemy.getLastDirection().y > 0.5f && targetDirection.y > 0 ||
						enemy.getLastDirection().y < -0.5f && targetDirection.y < 0)) {
					lineOfSight = false;
				}

				if (lineOfSight) {
					while (position.dst2(currentPosition) < position.dst2(enemy.getPosition())) {
						if (collisionLayer.getCell((int) (currentPosition.x / 32.0f), (int) (currentPosition.y / 32.0f)) != null) {
							lineOfSight = false;
							break;
						}

						currentPosition.add(targetDirection);
					}
				}

				if (lineOfSight) {
					enemy.alert();
				}
			}
		}
	}

	private void aggroOthers(List<Entity> entities) {
		for (Entity entity : entities) {
			if (entity instanceof Enemy && entity != this) {
				Enemy enemy = (Enemy) entity;

				boolean lineOfSight = true;
				Vector2 currentPosition = new Vector2(position.x + 16, position.y + 16);
				Vector2 targetDirection = new Vector2(enemy.getPosition()).sub(getPosition()).nor();

				if (!enemy.isAlerted() &&
						(enemy.getLastDirection().x > 0 && targetDirection.x > 0 ||
						enemy.getLastDirection().x < 0 && targetDirection.x < 0 ||
						enemy.getLastDirection().y > 0.5f && targetDirection.y > 0 ||
						enemy.getLastDirection().y < -0.5f && targetDirection.y < 0)) {
					lineOfSight = false;
				}

				if (lineOfSight) {
					while (position.dst2(currentPosition) < position.dst2(enemy.getPosition())) {
						if (collisionLayer.getCell((int) (currentPosition.x / 32.0f), (int) (currentPosition.y / 32.0f)) != null) {
							lineOfSight = false;
							break;
						}

						currentPosition.add(targetDirection);
					}
				}

				if (lineOfSight) {
					enemy.addTarget(this);
					enemy.alert();
				}
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
		Game.enemiesLeft--;

		alertOthers(entities);

		requestSound("hit");
	}
}
