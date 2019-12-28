package com.mojang.ld22;

import com.mojang.ld22.screen.Menu;

public interface GameListener {
    void onMenuChange(Menu oldMenu, Menu newMenu);
    void onTitleOptionSelect(int selection);
}
