package com.justindevsjava.skyblock_plus.client.ui.clickgui.fubuki.nav;

public interface NavigationDestination {
    String name();

    /**
     * @return if navigation success
     */
    boolean navigate();
}
