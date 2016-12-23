/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.message.data;

/**
 * Object representing Minecraft player.
 *
 * @author Mattias Markehed
 */
public class PlayerData {

    protected String displayName;
    protected String name;
    protected int level;
    protected int totalExperience;
    protected float experience;
    protected double health;
    protected float walkSpeed;
    protected LocationData location;

    /**
     * Get the display name of player.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get the name of player
     *
     * @return name of player.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the player level.
     *
     * @return level of player
     */
    public int getLevel() {
        return level;
    }

    /**
     * Get the total experience of player.
     *
     * @return total experience
     */
    public int getTotalExperience() {
        return totalExperience;
    }

    /**
     * Get player experiance.
     *
     * @return experiance of player
     */
    public float getExperience() {
        return experience;
    }

    /**
     * Get health of player.
     *
     * @return player health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Get the walkspeed of player
     *
     * @return walkspeed of player.
     */
    public float getWalkSpeed() {
        return walkSpeed;
    }

    /**
     * Get location of player.
     *
     * @return location of player
     */
    public LocationData getLocation() {
        return location;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PlayerData other = (PlayerData) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
