/**
 * Copyright 2014 Riyaz Ahamed
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package ahamed.view.utils;

import android.annotation.SuppressLint;
import android.util.FloatMath;

@SuppressLint("FloatMath")
public class Dynamics {

	/**
	 * Used to compare floats, if the difference is smaller than this, they are
	 * considered equal
	 */
	private static final float TOLERANCE = 0.01f;

	/** The position the dynamics should to be at */
	private float targetPosition;

	/** The current position of the dynamics */
	private float position;

	/** The current velocity of the dynamics */
	private float velocity;

	/** The time the last update happened */
	private long lastTime;

	/** The amount of springiness that the dynamics has */
	private float springiness;

	/** The damping that the dynamics has */
	private float damping;

	/**
	 * @author Riyaz Ahamed <br>
	 *         <p>
	 *         Classic {@link Dynamics} class used to Animate the Views.<br>
	 *         Send Feedbacks to dev.ahamed(at)outlook.com
	 *         </p>
	 */
	public Dynamics(float springiness, float dampingRatio) {
		this.springiness = springiness;
		this.damping = dampingRatio * 2 * FloatMath.sqrt(springiness);
	}

	public void setPosition(float position, long now) {
		this.position = position;
		lastTime = now;
	}

	public void setVelocity(float velocity, long now) {
		this.velocity = velocity;
		lastTime = now;
	}

	public void setTargetPosition(float targetPosition, long now) {
		this.targetPosition = targetPosition;
		lastTime = now;
	}

	public void update(long now) {
		float dt = Math.min(now - lastTime, 50) / 1000f;

		float x = position - targetPosition;
		float acceleration = -springiness * x - damping * velocity;

		velocity += acceleration * dt;
		position += velocity * dt;

		lastTime = now;
	}

	public boolean isAtRest() {
		final boolean standingStill = Math.abs(velocity) < TOLERANCE;
		final boolean isAtTarget = (targetPosition - position) < TOLERANCE;
		return standingStill && isAtTarget;
	}

	public float getPosition() {
		return position;
	}

	public float getTargetPos() {
		return targetPosition;
	}

	public float getVelocity() {
		return velocity;
	}

}
