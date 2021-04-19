package me.alpha432.oyvey.features.modules.render;

import me.alpha432.oyvey.features.modules.Module;
import me.alpha432.oyvey.features.setting.Setting;

public class TexturedChams extends Module {

    public static Setting<Integer> red;
    public static Setting<Integer> green;
    public static Setting<Integer> blue;
    public static Setting<Integer> alpha;

    public TexturedChams() {
        super("TexturedChams", "hi yes", Category.RENDER, true, false, true);

        red = (Setting<Integer>) register(new Setting("Red", 168, 0, 255));
        green = (Setting<Integer>) register(new Setting("Green", 0, 0, 255));
        blue = (Setting<Integer>) register(new Setting("Blue", 232, 0, 255));
        alpha = (Setting<Integer>) register(new Setting("Alpha", 150, 0, 255));
    }
}
