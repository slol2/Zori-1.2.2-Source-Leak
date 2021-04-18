package me.alpha432.oyvey.event.events;

import me.alpha432.oyvey.event.EventStage;

public class PlayerJumpEvent extends EventStage {
    public double motionX;
    public double motionY;

    public PlayerJumpEvent(double motionX, double motionY)
    {
        super();
        this.motionX = motionX;
        this.motionY = motionY;
    }
}