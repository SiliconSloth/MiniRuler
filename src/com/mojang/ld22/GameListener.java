package com.mojang.ld22;

import com.mojang.ld22.screen.Menu;

public interface GameListener {
    void onMenuOpen(Menu menu);
    void onMenuClose(Menu menu);
    void onTitleOptionSelect(int selection);
}
